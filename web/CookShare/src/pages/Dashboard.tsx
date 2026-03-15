import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ChefHat,
  Star,
  Users,
  Clock,
  Plus,
  Search,
  LogOut,
  Settings,
  User,
} from 'lucide-react';
import type { Recipe, Difficulty } from '../types/recipe';
import { authService } from '../services/authService';
import '../styles/Dashboard.css';

// ── Mock Data ─────────────────────────────────────────────────────────────────

const RECIPES: Recipe[] = [
  {
    id: 1,
    title: 'Creamy Garlic Pasta',
    description: 'A delicious and creamy pasta dish with garlic and parmesan cheese. Perfect for a quick weeknight dinner.',
    tags: ['Italian', 'Quick', 'Vegetarian'],
    rating: 4.8,
    reviewCount: 127,
    cookTime: '10 mins',
    difficulty: 'Easy',
    author: 'Sarah Johnson',
    servings: 4,
    imageUrl: 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=600&auto=format&fit=crop',
  },
  {
    id: 2,
    title: 'Rich Chocolate Cake',
    description: "Decadent chocolate cake with a moist crumb and rich chocolate frosting. A chocolate lover's dream!",
    tags: ['Chocolate', 'Baking', 'Party'],
    rating: 4.9,
    reviewCount: 203,
    cookTime: '25 mins',
    difficulty: 'Medium',
    author: 'Michael Chen',
    servings: 8,
    imageUrl: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop',
  },
  {
    id: 3,
    title: 'Mediterranean Quinoa Salad',
    description: 'Fresh and healthy quinoa salad with Mediterranean flavors. Perfect for meal prep or as a light lunch.',
    tags: ['Healthy', 'Vegan', 'Mediterranean'],
    rating: 4.7,
    reviewCount: 89,
    cookTime: '15 mins',
    difficulty: 'Easy',
    author: 'Emma Wilson',
    servings: 6,
    imageUrl: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop',
  },
  {
    id: 4,
    title: 'Grilled Lemon Herb Chicken',
    description: 'Juicy grilled chicken marinated in lemon and herbs. A healthy and flavorful main dish.',
    tags: ['Grilled', 'Healthy', 'Protein'],
    rating: 4.6,
    reviewCount: 156,
    cookTime: '15 mins',
    difficulty: 'Easy',
    author: 'David Martinez',
    servings: 4,
    imageUrl: 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=600&auto=format&fit=crop',
  },
  {
    id: 5,
    title: 'Homemade Sushi Rolls',
    description: 'Learn to make delicious sushi rolls at home with fresh ingredients. Easier than you think!',
    tags: ['Japanese', 'Sushi', 'Seafood'],
    rating: 4.5,
    reviewCount: 94,
    cookTime: '30 mins',
    difficulty: 'Medium',
    author: 'Yuki Tanaka',
    servings: 4,
    imageUrl: 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=600&auto=format&fit=crop',
  },
  {
    id: 6,
    title: 'Artisan Pizza Margherita',
    description: 'Classic Italian pizza with homemade dough, fresh tomatoes, mozzarella, and basil.',
    tags: ['Italian', 'Homemade', 'Vegetarian'],
    rating: 4.9,
    reviewCount: 178,
    cookTime: '90 mins',
    difficulty: 'Medium',
    author: 'Giovanni Rossi',
    servings: 2,
    imageUrl: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=600&auto=format&fit=crop',
  },
];

const CATEGORIES = ['All', 'Pasta', 'Dessert', 'Salad', 'Main Course', 'Asian', 'Pizza'];

// ── Sub-components ────────────────────────────────────────────────────────────

const DifficultyBadge = ({ difficulty }: { difficulty: Difficulty }) => {
  const classMap: Record<Difficulty, string> = {
    Easy: 'badge badge--easy',
    Medium: 'badge badge--medium',
    Hard: 'badge badge--hard',
  };
  return <span className={classMap[difficulty]}>{difficulty}</span>;
};

const RecipeCard = ({ recipe }: { recipe: Recipe }) => (
  <div className="recipe-card">
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

const StatCard = ({
  iconClass,
  icon,
  value,
  label,
}: {
  iconClass: string;
  icon: React.ReactNode;
  value: string;
  label: string;
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
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const user = authService.getUser();
  const initials = user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U';
  const fullName = user ? `${user.firstName} ${user.lastName}` : 'User';
  const email = user?.email ?? '';

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const filtered = RECIPES.filter((r) => {
    const matchesCategory =
      activeCategory === 'All' ||
      r.tags.some((t) => t.toLowerCase().includes(activeCategory.toLowerCase())) ||
      r.title.toLowerCase().includes(activeCategory.toLowerCase());
    const matchesSearch =
      search.trim() === '' ||
      r.title.toLowerCase().includes(search.toLowerCase()) ||
      r.tags.some((t) => t.toLowerCase().includes(search.toLowerCase()));
    return matchesCategory && matchesSearch;
  });

  const avgRating = (RECIPES.reduce((sum, r) => sum + r.rating, 0) / RECIPES.length).toFixed(1);

  return (
    <div className="dashboard-root" onClick={() => setDropdownOpen(false)}>
      {/* Navbar */}
      <nav className="dashboard-nav">
        <div className="dashboard-nav__logo">
          <div className="dashboard-nav__logo-icon">
            <ChefHat size={18} color="white" />
          </div>
          <span className="dashboard-nav__logo-text">CookShare</span>
        </div>

        {/* Avatar + Dropdown */}
        <div className="dashboard-nav__user" onClick={(e) => e.stopPropagation()}>
          <button
            className="dashboard-nav__avatar"
            onClick={() => setDropdownOpen((o) => !o)}
          >
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
        {/* Header */}
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
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="dashboard-search__input"
          />
        </div>

        {/* Filters */}
        <div className="dashboard-filters">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              className={`dashboard-filters__btn${activeCategory === cat ? ' dashboard-filters__btn--active' : ''}`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Stats */}
        <div className="dashboard-stats">
          <StatCard
            iconClass="stat-card__icon--orange"
            value={String(RECIPES.length)}
            label="Total Recipes"
            icon={<ChefHat size={20} />}
          />
          <StatCard
            iconClass="stat-card__icon--blue"
            value={avgRating}
            label="Avg Rating"
            icon={<Star size={20} />}
          />
          <StatCard
            iconClass="stat-card__icon--green"
            value="1.2k+"
            label="Active Users"
            icon={<Users size={20} />}
          />
        </div>

        {/* Recipe Grid */}
        {filtered.length > 0 ? (
          <div className="recipe-grid">
            {filtered.map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} />
            ))}
          </div>
        ) : (
          <div className="recipe-grid__empty">
            <p className="recipe-grid__empty-title">No recipes found</p>
            <p className="recipe-grid__empty-sub">Try a different search or category</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;