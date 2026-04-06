import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ChefHat, Star, Users, Clock, Plus, Search,
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

// ── Category map ──────────────────────────────────────────────────────────────

const CATEGORIES = ['All', 'Pasta', 'Dessert', 'Salad', 'Main Course', 'Asian', 'Pizza'];

const CATEGORY_MAP: Record<string, string> = {
  Pasta: 'pasta',
  Dessert: 'dessert cake cookies',
  Salad: 'salad',
  'Main Course': 'chicken beef dinner',
  Asian: 'asian stir fry noodles',
  Pizza: 'pizza',
};

// ── Mapper: Spoonacular → our Recipe type ─────────────────────────────────────

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

// ── Sub-components ────────────────────────────────────────────────────────────

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
  const [commentText, setCommentText] = useState('');
  const [comments, setComments] = useState<Comment[]>(recipe.comments);

  const handlePostComment = () => {
    if (!commentText.trim()) return;
    const user = authService.getUser();
    const newComment: Comment = {
      id: Date.now(),
      author: user ? `${user.firstName} ${user.lastName}` : 'Anonymous',
      text: commentText.trim(),
      date: 'Just now',
    };
    setComments([newComment, ...comments]);
    setCommentText('');
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        {/* Image */}
        <div className="modal__image-wrapper">
          <img src={recipe.imageUrl} alt={recipe.title} className="modal__image" />
          <div className="modal__image-actions">
            <button className="modal__icon-btn"><Heart size={16} /></button>
            <button className="modal__icon-btn"><Share2 size={16} /></button>
          </div>
        </div>

        {/* Close */}
        <button className="modal__close" onClick={onClose}>
          <X size={16} />
        </button>

        {/* Body */}
        <div className="modal__body">
          <div className="modal__header">
            <h2 className="modal__title">{recipe.title}</h2>
            <DifficultyBadge difficulty={recipe.difficulty} />
          </div>
          <p className="modal__description">{recipe.description}</p>

          {/* Meta */}
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
                <span className="modal__meta-value">{recipe.rating} ({recipe.reviewCount})</span>
              </div>
            </div>
          </div>

          {/* Tags */}
          <div className="modal__tags">
            {recipe.tags.map((tag) => (
              <span key={tag} className="modal__tag">{tag}</span>
            ))}
          </div>

          <div className="modal__divider" />

          {/* Ingredients */}
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

          {/* Instructions */}
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

          {/* Rate */}
          <h3 className="modal__section-title">Rate this recipe</h3>
          <div className="modal__stars">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                className={`modal__star${(hoverRating || userRating) >= star ? ' modal__star--active' : ''}`}
                onClick={() => setUserRating(star)}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
              >
                <Star size={28} fill={(hoverRating || userRating) >= star ? '#f97316' : 'none'} />
              </button>
            ))}
          </div>

          <div className="modal__divider" />

          {/* Comments */}
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

          {/* Footer */}
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

  const user = authService.getUser();
  const initials = user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U';
  const fullName = user ? `${user.firstName} ${user.lastName}` : 'User';
  const email = user?.email ?? '';

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  // ── Fetch recipes ──────────────────────────────────────────────────────────

  const fetchRecipes = useCallback(async () => {
    setLoading(true);
    setError('');
    setNoResults(false);
    try {
      let raw: SpoonacularRecipe[];

      if (search.trim()) {
        raw = await spoonacularService.searchRecipes(search.trim(), 9);
        if (raw.length === 0) {
          // No results for this search — show message, fallback to random
          setNoResults(true);
          raw = await spoonacularService.getRandomRecipes(6);
        }
      } else if (activeCategory !== 'All') {
        raw = await spoonacularService.getRecipesByCategory(CATEGORY_MAP[activeCategory], 9);
        if (raw.length === 0) {
          // Category returned nothing — silently fallback to random
          raw = await spoonacularService.getRandomRecipes(6);
        }
      } else {
        raw = await spoonacularService.getRandomRecipes(6);
      }

      setRecipes(raw.map(mapRecipe));
    } catch {
      setError('Failed to load recipes. Please check your API key or try again.');
    } finally {
      setLoading(false);
    }
  }, [search, activeCategory]);

  useEffect(() => {
    fetchRecipes();
  }, [fetchRecipes]);

  // Debounce search input
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

        <div className="dashboard-nav__user" onClick={(e) => e.stopPropagation()}>
          <button className="dashboard-nav__avatar" onClick={() => setDropdownOpen((o) => !o)}>
            {initials}
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

        {/* Search */}
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

        {/* Filters */}
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

        {/* Stats */}
        <div className="dashboard-stats">
          <StatCard iconClass="stat-card__icon--orange" value={String(recipes.length)} label="Total Recipes" icon={<ChefHat size={20} />} />
          <StatCard
            iconClass="stat-card__icon--blue"
            value={recipes.length > 0 ? (recipes.reduce((s, r) => s + r.rating, 0) / recipes.length).toFixed(1) : '—'}
            label="Avg Rating"
            icon={<Star size={20} />}
          />
          <StatCard iconClass="stat-card__icon--green" value="1.2k+" label="Active Users" icon={<Users size={20} />} />
        </div>

        {/* No results banner */}
        {noResults && !loading && (
          <div className="dashboard-no-results">
            <p className="dashboard-no-results__text">
              No recipes found for <strong>"{searchInput}"</strong> — showing random recipes instead.
            </p>
          </div>
        )}

        {/* Content */}
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