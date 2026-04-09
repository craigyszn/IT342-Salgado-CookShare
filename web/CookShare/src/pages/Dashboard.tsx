import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ChefHat, Star, Users, Clock, Plus, Search, Shield,
  LogOut, Settings, User, Heart, Share2, X,
  Timer, UtensilsCrossed, MessageCircle, RefreshCw,
} from 'lucide-react';
import type { Recipe, Difficulty, Comment } from '../types/recipe';
import { authService } from '../services/authService';
import {
  spoonacularService,
  stripHtml,
  scoreToRating,
  getDifficulty,
  type SpoonacularRecipe,
} from '../services/spoonacularService';
import '../styles/Dashboard.css';

const CATEGORIES = ['All', 'Pasta', 'Dessert', 'Salad', 'Main Course', 'Asian', 'Pizza'];

const CATEGORY_MAP: Record<string, string> = {
  Pasta: 'pasta',
  Dessert: 'dessert cake cookies',
  Salad: 'salad',
  'Main Course': 'chicken beef dinner',
  Asian: 'asian stir fry noodles',
  Pizza: 'pizza',
};

const mapRecipe = (r: SpoonacularRecipe): Recipe => ({
  id: r.id,
  title: r.title,
  description: stripHtml(r.summary).slice(0, 180) + '...',
  tags: [
    ...r.dishTypes.slice(0, 2).map((t) => t.charAt(0).toUpperCase() + t.slice(1)),
    ...r.diets.slice(0, 1).map((d) => d.charAt(0).toUpperCase() + d.slice(1)),
  ],
  rating: scoreToRating(r.spoonacularScore),
  reviewCount: r.aggregateLikes,
  prepTime: r.preparationMinutes > 0 ? `${r.preparationMinutes} mins` : 'N/A',
  cookTime: r.cookingMinutes > 0 ? `${r.cookingMinutes} mins` : `${r.readyInMinutes} mins`,
  difficulty: getDifficulty(r.readyInMinutes),
  author: r.creditsText || 'Spoonacular',
  servings: r.servings,
  imageUrl: r.image,
  ingredients: r.extendedIngredients?.map((i) => i.original) ?? [],
  instructions:
    r.analyzedInstructions?.[0]?.steps.map((s) => s.step) ?? ['See full recipe for instructions.'],
  postedDate: new Date().toISOString().split('T')[0],
  comments: [],
});

const mapDbRecipe = (r: any): Recipe => ({
  id: r.id,
  title: r.title,
  description: r.description || '',
  tags: r.tags || [],
  rating: r.rating || 0,
  reviewCount: r.reviewCount || 0,
  prepTime: r.prepTime || 'N/A',
  cookTime: r.cookTime || 'N/A',
  difficulty: (r.difficulty as Difficulty) || 'Easy',
  author: r.author || 'CookShare User',
  servings: r.servings || 4,
  imageUrl: r.image || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1080',
  ingredients: r.ingredients || [],
  instructions: r.instructions || [],
  postedDate: r.createdAt || new Date().toISOString().split('T')[0],
  comments: [],
});

const DifficultyBadge = ({ difficulty }: { difficulty: Difficulty }) => {
  const classMap: Record<Difficulty, string> = {
    Easy: 'badge badge--easy',
    Medium: 'badge badge--medium',
    Hard: 'badge badge--hard',
  };
  return <span className={classMap[difficulty]}>{difficulty}</span>;
};

const RecipeCard = ({ recipe, onClick }: { recipe: Recipe; onClick: () => void }) => (
  <div className="recipe-card" onClick={onClick}>
    <div className="recipe-card__image-wrapper">
      <img src={recipe.imageUrl} alt={recipe.title} className="recipe-card__image" />
    </div>
    <div className="recipe-card__body">
      <div className="recipe-card__top">
        <h3 className="recipe-card__title">{recipe.title}</h3>
        <DifficultyBadge difficulty={recipe.difficulty} />
      </div>
      <p className="recipe-card__description">{recipe.description}</p>
      <div className="recipe-card__tags">
        {recipe.tags.map((tag) => (
          <span key={tag} className="recipe-card__tag">{tag}</span>
        ))}
      </div>
      <div className="recipe-card__meta">
        <div className="recipe-card__rating">
          <Star size={14} fill="#facc15" color="#facc15" />
          <span className="recipe-card__rating-value">{recipe.rating}</span>
          <span>({recipe.reviewCount})</span>
        </div>
        <div className="recipe-card__time">
          <Clock size={14} color="#9ca3af" />
          <span>{recipe.cookTime}</span>
        </div>
      </div>
      <div className="recipe-card__footer">
        <span>by {recipe.author}</span>
        <div className="recipe-card__servings">
          <Users size={14} color="#9ca3af" />
          <span>{recipe.servings} servings</span>
        </div>
      </div>
    </div>
  </div>
);

// ── Recipe Modal ──────────────────────────────────────────────────────────────

const RecipeModal = ({ recipe, onClose }: { recipe: Recipe; onClose: () => void }) => {
  const [userRating, setUserRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [hasRated, setHasRated] = useState(false);
  const [liveRating, setLiveRating] = useState(recipe.rating);
  const [liveReviewCount, setLiveReviewCount] = useState(recipe.reviewCount);
  const [commentText, setCommentText] = useState('');
  const [comments, setComments] = useState<Comment[]>([]);
  const [isFavorited, setIsFavorited] = useState(false);
  const [loadingComments, setLoadingComments] = useState(true);

  const user = authService.getUser();
  const recipeId = String(recipe.id);

  useEffect(() => {
    const load = async () => {
      // Load comments
      try {
        const res = await fetch(`http://localhost:8081/api/comments?recipeId=${recipeId}`);
        if (res.ok) {
          const data = await res.json();
          const mapped: Comment[] = data.map((c: any) => ({
            id: c.id,
            author: c.authorName,
            text: c.text,
            date: c.createdAt ? new Date(c.createdAt).toLocaleDateString() : 'Just now',
          }));
          setComments(mapped);
        }
      } catch { /* ignore */ }

      if (user?.email) {
        // Check favorites
        try {
          const res = await fetch(
            `http://localhost:8081/api/favorites/check?email=${encodeURIComponent(user.email)}&recipeId=${recipeId}`
          );
          if (res.ok) {
            const data = await res.json();
            setIsFavorited(data.favorited);
          }
        } catch { /* ignore */ }

        // ── Check if user already rated this recipe ───────────────────────
        try {
          const res = await fetch(
            `http://localhost:8081/api/recipes/${recipeId}/my-rating?email=${encodeURIComponent(user.email)}`
          );
          if (res.ok) {
            const data = await res.json();
            if (data.rated) {
              setHasRated(true);
              setUserRating(data.stars);
            }
          }
        } catch { /* ignore */ }
      }

      setLoadingComments(false);
    };
    load();
  }, [recipeId]);

  const handleToggleFavorite = async () => {
    if (!user?.email) return;
    if (isFavorited) {
      await fetch(
        `http://localhost:8081/api/favorites?email=${encodeURIComponent(user.email)}&recipeId=${recipeId}`,
        { method: 'DELETE' }
      );
      setIsFavorited(false);
    } else {
      await fetch('http://localhost:8081/api/favorites', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: user.email,
          recipeId,
          recipeTitle: recipe.title,
          recipeImage: recipe.imageUrl,
        }),
      });
      setIsFavorited(true);
    }
  };

  const handlePostComment = async () => {
    if (!commentText.trim()) return;
    const authorName = user ? `${user.firstName} ${user.lastName}` : 'Anonymous';
    try {
      const res = await fetch('http://localhost:8081/api/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: user?.email ?? '',
          authorName,
          recipeId,
          text: commentText.trim(),
        }),
      });
      if (res.ok) {
        const saved = await res.json();
        const newComment: Comment = {
          id: saved.id,
          author: saved.authorName,
          text: saved.text,
          date: 'Just now',
        };
        setComments([newComment, ...comments]);
        setCommentText('');
      }
    } catch { /* ignore */ }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal__image-wrapper">
          <img src={recipe.imageUrl} alt={recipe.title} className="modal__image" />
          <div className="modal__image-actions">
            <button
              className={`modal__icon-btn${isFavorited ? ' modal__icon-btn--active' : ''}`}
              onClick={(e) => { e.stopPropagation(); handleToggleFavorite(); }}
              title={isFavorited ? 'Remove from favorites' : 'Add to favorites'}
            >
              <Heart size={16} fill={isFavorited ? '#f97316' : 'none'} color={isFavorited ? '#f97316' : 'currentColor'} />
            </button>
            <button className="modal__icon-btn"><Share2 size={16} /></button>
          </div>
        </div>

        <button className="modal__close" onClick={onClose}>
          <X size={16} />
        </button>

        <div className="modal__body">
          <div className="modal__header">
            <h2 className="modal__title">{recipe.title}</h2>
            <DifficultyBadge difficulty={recipe.difficulty} />
          </div>
          <p className="modal__description">{recipe.description}</p>

          <div className="modal__meta">
            <div className="modal__meta-item">
              <Timer size={18} color="#f97316" />
              <div>
                <span className="modal__meta-label">Prep Time</span>
                <span className="modal__meta-value">{recipe.prepTime}</span>
              </div>
            </div>
            <div className="modal__meta-item">
              <UtensilsCrossed size={18} color="#f97316" />
              <div>
                <span className="modal__meta-label">Cook Time</span>
                <span className="modal__meta-value">{recipe.cookTime}</span>
              </div>
            </div>
            <div className="modal__meta-item">
              <Users size={18} color="#f97316" />
              <div>
                <span className="modal__meta-label">Servings</span>
                <span className="modal__meta-value">{recipe.servings}</span>
              </div>
            </div>
            <div className="modal__meta-item">
              <Star size={18} color="#f97316" fill="#f97316" />
              <div>
                <span className="modal__meta-label">Rating</span>
                <span className="modal__meta-value">{liveRating} ({liveReviewCount})</span>
              </div>
            </div>
          </div>

          <div className="modal__tags">
            {recipe.tags.map((tag) => (
              <span key={tag} className="modal__tag">{tag}</span>
            ))}
          </div>

          <div className="modal__divider" />

          <h3 className="modal__section-title">Ingredients</h3>
          <ul className="modal__ingredient-list">
            {recipe.ingredients.map((ing, i) => (
              <li key={i} className="modal__ingredient-item">
                <span className="modal__ingredient-dot" />
                {ing}
              </li>
            ))}
          </ul>

          <div className="modal__divider" />

          <h3 className="modal__section-title">Instructions</h3>
          <ol className="modal__instruction-list">
            {recipe.instructions.map((step, i) => (
              <li key={i} className="modal__instruction-item">
                <span className="modal__step-number">{i + 1}</span>
                {step}
              </li>
            ))}
          </ol>

          <div className="modal__divider" />

          <h3 className="modal__section-title">Rate this recipe</h3>
          <div className="modal__rating-display">
            <Star size={16} fill="#facc15" color="#facc15" />
            <span className="modal__rating-avg">{liveRating}</span>
            <span className="modal__rating-count">({liveReviewCount} {liveReviewCount === 1 ? 'rating' : 'ratings'})</span>
          </div>
          {hasRated ? (
            <p className="modal__rated-msg">
              You rated this recipe {userRating} star{userRating > 1 ? 's' : ''}! Thank you.
            </p>
          ) : (
            <div className="modal__stars">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  className={`modal__star${(hoverRating || userRating) >= star ? ' modal__star--active' : ''}`}
                  onClick={async () => {
                    setUserRating(star);
                    setHasRated(true);
                    try {
                      const res = await fetch(`http://localhost:8081/api/recipes/${recipeId}/rate`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ stars: star, userEmail: user?.email ?? '' }),
                      });
                      if (res.ok) {
                        const updated = await res.json();
                        setLiveRating(updated.rating);
                        setLiveReviewCount(updated.reviewCount);
                      }
                    } catch { /* ignore */ }
                  }}
                  onMouseEnter={() => setHoverRating(star)}
                  onMouseLeave={() => setHoverRating(0)}
                >
                  <Star size={28} fill={(hoverRating || userRating) >= star ? '#f97316' : 'none'} />
                </button>
              ))}
            </div>
          )}

          <div className="modal__divider" />

          <h3 className="modal__section-title">
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <MessageCircle size={20} />
              Comments ({comments.length})
            </span>
          </h3>
          <div className="modal__comment-input-row">
            <textarea
              className="modal__comment-input"
              placeholder="Share your thoughts about this recipe..."
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              rows={3}
            />
            <button className="modal__comment-submit" onClick={handlePostComment}>
              Post Comment
            </button>
          </div>
          {comments.length > 0 && (
            <div className="modal__comment-list">
              {comments.map((c) => (
                <div key={c.id} className="modal__comment">
                  <div className="modal__comment-header">
                    <span className="modal__comment-author">{c.author}</span>
                    <span className="modal__comment-date">{c.date}</span>
                  </div>
                  <p className="modal__comment-text">{c.text}</p>
                </div>
              ))}
            </div>
          )}

          <div className="modal__footer">
            Recipe by <strong>{recipe.author}</strong> • Posted on {recipe.postedDate}
          </div>
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ iconClass, icon, value, label }: {
  iconClass: string; icon: React.ReactNode; value: string; label: string;
}) => (
  <div className="stat-card">
    <div className={`stat-card__icon ${iconClass}`}>{icon}</div>
    <div>
      <p className="stat-card__value">{value}</p>
      <p className="stat-card__label">{label}</p>
    </div>
  </div>
);

// ── Main Dashboard ────────────────────────────────────────────────────────────

const Dashboard = () => {
  const navigate = useNavigate();
  const [activeCategory, setActiveCategory] = useState('All');
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [noResults, setNoResults] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState<Recipe | null>(null);
  const [recipes, setRecipes] = useState<Recipe[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [userCount, setUserCount] = useState('...');

  useEffect(() => {
    fetch('http://localhost:8081/api/users/count')
      .then((res) => res.json())
      .then((data) => setUserCount(String(data.count)))
      .catch(() => setUserCount('—'));
  }, []);

  const user = authService.getUser();
  const isAdmin = authService.isAdmin();
  const initials = user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U';
  const avatarUrl = user?.avatarUrl || '';
  const fullName = user ? `${user.firstName} ${user.lastName}` : 'User';
  const email = user?.email ?? '';

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  const fetchDbRecipes = async (): Promise<Recipe[]> => {
    try {
      const res = await fetch('http://localhost:8081/api/recipes');
      if (!res.ok) return [];
      const data = await res.json();
      return data.map(mapDbRecipe);
    } catch {
      return [];
    }
  };

  const fetchRecipes = useCallback(async () => {
    setLoading(true);
    setError('');
    setNoResults(false);
    try {
      const dbRecipes = await fetchDbRecipes();

      if (search.trim()) {
        const dbMatches = dbRecipes.filter((r) =>
          r.title.toLowerCase().includes(search.toLowerCase()) ||
          r.tags.some((t) => t.toLowerCase().includes(search.toLowerCase())) ||
          r.author.toLowerCase().includes(search.toLowerCase())
        );

        let raw: SpoonacularRecipe[] = [];
        try {
          raw = await spoonacularService.searchRecipes(search.trim(), 6);
        } catch { raw = []; }

        const merged = [...dbMatches, ...raw.map(mapRecipe)];

        if (merged.length === 0) {
          setNoResults(true);
          const fallback = await spoonacularService.getRandomRecipes(6).catch(() => []);
          setRecipes([...dbRecipes, ...fallback.map(mapRecipe)]);
        } else {
          setRecipes(merged);
        }
        return;
      }

      if (activeCategory !== 'All') {
        const dbCategoryMatches = dbRecipes.filter((r) =>
          r.tags.some((t) => t.toLowerCase() === activeCategory.toLowerCase()) ||
          (r as any).category?.toLowerCase() === activeCategory.toLowerCase()
        );

        let raw: SpoonacularRecipe[] = [];
        try {
          raw = await spoonacularService.getRecipesByCategory(CATEGORY_MAP[activeCategory], 6);
        } catch { raw = []; }

        if (raw.length === 0 && dbCategoryMatches.length === 0) {
          const fallback = await spoonacularService.getRandomRecipes(6).catch(() => []);
          setRecipes([...dbRecipes, ...fallback.map(mapRecipe)]);
        } else {
          setRecipes([...dbCategoryMatches, ...raw.map(mapRecipe)]);
        }
        return;
      }

      let raw: SpoonacularRecipe[] = [];
      try {
        raw = await spoonacularService.getRandomRecipes(6);
      } catch { raw = []; }

      setRecipes([...dbRecipes, ...raw.map(mapRecipe)]);

    } catch {
      setError('Failed to load recipes. Please check your API key or try again.');
    } finally {
      setLoading(false);
    }
  }, [search, activeCategory]);

  useEffect(() => {
    fetchRecipes();
  }, [fetchRecipes]);

  useEffect(() => {
    const timer = setTimeout(() => setSearch(searchInput), 600);
    return () => clearTimeout(timer);
  }, [searchInput]);

  return (
    <div className="dashboard-root" onClick={() => setDropdownOpen(false)}>
      {selectedRecipe && (
        <RecipeModal recipe={selectedRecipe} onClose={() => setSelectedRecipe(null)} />
      )}

      {/* Navbar */}
      <nav className="dashboard-nav">
        <div className="dashboard-nav__logo">
          <div className="dashboard-nav__logo-icon">
            <ChefHat size={18} color="white" />
          </div>
          <span className="dashboard-nav__logo-text">CookShare</span>
        </div>

        <div className="dashboard-nav__center">
          {isAdmin && (
            <button className="dashboard-nav__admin-btn" onClick={() => navigate('/admin')}>
              <Shield size={15} />
              Admin Access
            </button>
          )}
        </div>

        {/* ── Avatar: shows photo if available, otherwise initials ── */}
        <div className="dashboard-nav__user" onClick={(e) => e.stopPropagation()}>
          <button className="dashboard-nav__avatar" onClick={() => setDropdownOpen((o) => !o)}>
            {avatarUrl ? (
              <img
                src={avatarUrl}
                alt={fullName}
                style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%' }}
              />
            ) : (
              initials
            )}
          </button>
          {dropdownOpen && (
            <div className="dropdown">
              <div className="dropdown__header">
                <p className="dropdown__name">{fullName}</p>
                <p className="dropdown__email">{email}</p>
              </div>
              <div className="dropdown__divider" />
              <button className="dropdown__item" onClick={() => navigate('/profile')}>
                <User size={16} color="#6b7280" />
                Profile
              </button>
              <button className="dropdown__item">
                <Settings size={16} color="#6b7280" />
                Settings
              </button>
              <div className="dropdown__divider" />
              <button className="dropdown__item dropdown__item--danger" onClick={handleLogout}>
                <LogOut size={16} />
                Logout
              </button>
            </div>
          )}
        </div>
      </nav>

      {/* Main */}
      <main className="dashboard-main">
        <div className="dashboard-header">
          <div>
            <h1 className="dashboard-header__title">Discover Delicious Recipes</h1>
            <p className="dashboard-header__subtitle">Explore thousands of recipes shared by our community</p>
          </div>
          <button className="dashboard-header__btn" onClick={() => navigate('/create-recipe')}>
            <Plus size={16} />
            Create Recipe
          </button>
        </div>

        <div className="dashboard-search">
          <Search size={16} color="#9ca3af" className="dashboard-search__icon" />
          <input
            type="text"
            placeholder="Search recipes, ingredients, or tags..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="dashboard-search__input"
          />
        </div>

        <div className="dashboard-filters">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => { setActiveCategory(cat); setSearchInput(''); setSearch(''); setNoResults(false); }}
              className={`dashboard-filters__btn${activeCategory === cat ? ' dashboard-filters__btn--active' : ''}`}
            >
              {cat}
            </button>
          ))}
        </div>

        <div className="dashboard-stats">
          <StatCard
            iconClass="stat-card__icon--orange"
            value={String(recipes.length)}
            label="Total Recipes"
            icon={<ChefHat size={20} />}
          />
          <StatCard
            iconClass="stat-card__icon--blue"
            value={recipes.length > 0
              ? (recipes.reduce((s, r) => s + r.rating, 0) / recipes.length).toFixed(1)
              : '—'}
            label="Avg Rating"
            icon={<Star size={20} />}
          />
          <StatCard
            iconClass="stat-card__icon--green"
            value={userCount}
            label="Active Users"
            icon={<Users size={20} />}
          />
        </div>

        {noResults && !loading && (
          <div className="dashboard-no-results">
            <p className="dashboard-no-results__text">
              No recipes found for <strong>"{searchInput}"</strong> — showing random recipes instead.
            </p>
          </div>
        )}

        {loading ? (
          <div className="dashboard-loading">
            <div className="dashboard-loading__spinner" />
            <p className="dashboard-loading__text">Loading recipes...</p>
          </div>
        ) : error ? (
          <div className="dashboard-error">
            <p className="dashboard-error__title">Something went wrong</p>
            <p>{error}</p>
            <button className="dashboard-error__retry" onClick={fetchRecipes}>
              <RefreshCw size={14} style={{ display: 'inline', marginRight: '0.375rem' }} />
              Try Again
            </button>
          </div>
        ) : (
          <div className="recipe-grid">
            {recipes.map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} onClick={() => setSelectedRecipe(recipe)} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;