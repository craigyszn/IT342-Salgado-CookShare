import { useNavigate } from 'react-router-dom';
import { ChefHat, Search, Users, Star, Clock, TrendingUp } from 'lucide-react';
import './Landing.css';

// ── Mock Data ─────────────────────────────────────────────────────────────────

const FEATURED = [
  {
    id: 1,
    title: 'Creamy Garlic Pasta',
    description: 'A delicious and creamy pasta dish with garlic and parmesan cheese. Perfect for a quick weeknight dinner.',
    imageUrl: 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=600&auto=format&fit=crop',
    cookTime: '10 mins',
    rating: 4.8,
    reviewCount: 127,
  },
  {
    id: 2,
    title: 'Rich Chocolate Cake',
    description: "Decadent chocolate cake with a moist crumb and rich chocolate frosting. A chocolate lover's dream!",
    imageUrl: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop',
    cookTime: '25 mins',
    rating: 4.9,
    reviewCount: 203,
  },
  {
    id: 3,
    title: 'Mediterranean Quinoa Salad',
    description: 'Fresh and healthy quinoa salad with Mediterranean flavors. Perfect for meal prep or as a light lunch.',
    imageUrl: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop',
    cookTime: '15 mins',
    rating: 4.7,
    reviewCount: 89,
  },
];

const TRENDING = [
  {
    id: 4,
    title: 'Grilled Lemon Herb Chicken',
    description: 'Juicy grilled chicken marinated in lemon and herbs. A healthy and flavorful main dish.',
    imageUrl: 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=600&auto=format&fit=crop',
    category: 'Main Course',
    difficulty: 'Easy' as const,
    author: 'David Martinez',
    rating: 4.6,
  },
  {
    id: 5,
    title: 'Homemade Sushi Rolls',
    description: 'Learn to make delicious sushi rolls at home with fresh ingredients. Easier than you think!',
    imageUrl: 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=600&auto=format&fit=crop',
    category: 'Asian',
    difficulty: 'Medium' as const,
    author: 'Yuki Tanaka',
    rating: 4.5,
  },
  {
    id: 6,
    title: 'Artisan Pizza Margherita',
    description: 'Classic Italian pizza with homemade dough, fresh tomatoes, mozzarella, and basil.',
    imageUrl: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=600&auto=format&fit=crop',
    category: 'Pizza',
    difficulty: 'Medium' as const,
    author: 'Giovanni Rossi',
    rating: 4.9,
  },
];

const DIFFICULTY_CLASS: Record<string, string> = {
  Easy: 'landing-trend-card__badge--easy',
  Medium: 'landing-trend-card__badge--medium',
  Hard: 'landing-trend-card__badge--hard',
};

// ── Component ─────────────────────────────────────────────────────────────────

export function Landing() {
  const navigate = useNavigate();

  return (
    <div className="landing-root">

      {/* Navbar */}
      <nav className="landing-nav">
        <div className="landing-nav__logo">
          <div className="landing-nav__logo-icon">
            <ChefHat size={18} color="white" />
          </div>
          <span className="landing-nav__logo-text">CookShare</span>
        </div>
        <div className="landing-nav__actions">
          <button className="landing-nav__login" onClick={() => navigate('/login')}>
            Log In
          </button>
          <button className="landing-nav__signup" onClick={() => navigate('/register')}>
            Sign Up
          </button>
        </div>
      </nav>

      {/* Hero */}
      <section className="landing-hero">
        <h1 className="landing-hero__title">Discover & Share Amazing Recipes</h1>
        <p className="landing-hero__subtitle">
          Join thousands of home cooks sharing their favorite recipes. Create, explore,
          and get inspired to cook something delicious today.
        </p>
        <div className="landing-hero__actions">
          <button className="landing-hero__btn-primary" onClick={() => navigate('/register')}>
            <ChefHat size={18} />
            Get Started Free
          </button>
          <button className="landing-hero__btn-secondary" onClick={() => navigate('/login')}>
            <Search size={16} />
            Browse Recipes
          </button>
        </div>

        {/* Stats */}
        <div className="landing-stats">
          <div>
            <p className="landing-stat__value">1,200+</p>
            <p className="landing-stat__label">Recipes</p>
          </div>
          <div>
            <p className="landing-stat__value">500+</p>
            <p className="landing-stat__label">Active Cooks</p>
          </div>
          <div>
            <p className="landing-stat__value">4.8★</p>
            <p className="landing-stat__label">Avg Rating</p>
          </div>
        </div>
      </section>

      {/* Why CookShare */}
      <section className="landing-features">
        <div className="landing-features__inner">
          <h2 className="landing-section-title">Why Choose CookShare?</h2>
          <div className="landing-features__grid">
            <div className="landing-feature-card">
              <div className="landing-feature-card__icon">
                <Users size={20} />
              </div>
              <h3 className="landing-feature-card__title">Community Driven</h3>
              <p className="landing-feature-card__desc">
                Connect with home cooks worldwide. Share your recipes and discover new favorites.
              </p>
            </div>
            <div className="landing-feature-card">
              <div className="landing-feature-card__icon">
                <Search size={20} />
              </div>
              <h3 className="landing-feature-card__title">Easy Discovery</h3>
              <p className="landing-feature-card__desc">
                Search by ingredients, cuisine, or dietary preferences to find the perfect recipe.
              </p>
            </div>
            <div className="landing-feature-card">
              <div className="landing-feature-card__icon">
                <Star size={20} />
              </div>
              <h3 className="landing-feature-card__title">Rated & Reviewed</h3>
              <p className="landing-feature-card__desc">
                Every recipe is rated and reviewed by real home cooks, ensuring quality.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Recipes */}
      <section className="landing-recipes">
        <div className="landing-recipes__inner">
          <div className="landing-recipes__header">
            <h2 className="landing-recipes__title">Featured Recipes</h2>
            <button className="landing-recipes__link" onClick={() => navigate('/login')}>
              View All <TrendingUp size={15} />
            </button>
          </div>
          <div className="landing-recipes__grid">
            {FEATURED.map((recipe) => (
              <div
                key={recipe.id}
                className="landing-recipe-card"
                onClick={() => navigate('/login')}
              >
                <div className="landing-recipe-card__image-wrapper">
                  <img src={recipe.imageUrl} alt={recipe.title} className="landing-recipe-card__image" />
                </div>
                <div className="landing-recipe-card__body">
                  <h3 className="landing-recipe-card__title">{recipe.title}</h3>
                  <p className="landing-recipe-card__desc">{recipe.description}</p>
                  <div className="landing-recipe-card__meta">
                    <div className="landing-recipe-card__time">
                      <Clock size={13} />
                      <span>{recipe.cookTime}</span>
                    </div>
                    <div className="landing-recipe-card__rating">
                      <Star size={13} fill="#facc15" color="#facc15" />
                      <span className="landing-recipe-card__rating-value">{recipe.rating}</span>
                      <span style={{ color: '#9ca3af' }}>({recipe.reviewCount})</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Trending Recipes */}
      <section className="landing-recipes landing-recipes--white">
        <div className="landing-recipes__inner">
          <div className="landing-recipes__header">
            <h2 className="landing-recipes__title">Trending Now</h2>
            <button className="landing-recipes__link" onClick={() => navigate('/login')}>
              See More <TrendingUp size={15} />
            </button>
          </div>
          <div className="landing-recipes__grid">
            {TRENDING.map((recipe) => (
              <div
                key={recipe.id}
                className="landing-trend-card"
                onClick={() => navigate('/login')}
              >
                <div className="landing-trend-card__image-wrapper">
                  <img src={recipe.imageUrl} alt={recipe.title} className="landing-trend-card__image" />
                </div>
                <div className="landing-trend-card__body">
                  <div className="landing-trend-card__badges">
                    <span className="landing-trend-card__badge">{recipe.category}</span>
                    <span className={`landing-trend-card__badge ${DIFFICULTY_CLASS[recipe.difficulty]}`}>
                      {recipe.difficulty}
                    </span>
                  </div>
                  <h3 className="landing-trend-card__title">{recipe.title}</h3>
                  <p className="landing-trend-card__desc">{recipe.description}</p>
                  <div className="landing-trend-card__footer">
                    <span>by {recipe.author}</span>
                    <div className="landing-trend-card__rating">
                      <Star size={13} fill="#facc15" color="#facc15" />
                      {recipe.rating}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="landing-cta">
        <h2 className="landing-cta__title">Ready to Start Cooking?</h2>
        <p className="landing-cta__subtitle">
          Join our community of passionate home cooks and share your culinary creations with the world.
        </p>
        <button className="landing-cta__btn" onClick={() => navigate('/register')}>
          Create Your Free Account
          <ChefHat size={18} />
        </button>
      </section>

      {/* Footer */}
      <footer className="landing-footer">
        <div className="landing-footer__inner">
          <div className="landing-footer__logo">
            <div className="landing-footer__logo-icon">
              <ChefHat size={14} color="white" />
            </div>
            CookShare
          </div>
          <div className="landing-footer__links">
            <button className="landing-footer__link">About</button>
            <button className="landing-footer__link" onClick={() => navigate('/login')}>Browse Recipes</button>
            <button className="landing-footer__link">Contact</button>
            <button className="landing-footer__link">Privacy</button>
          </div>
          <p className="landing-footer__copy">© 2026 CookShare. All rights reserved.</p>
        </div>
      </footer>

    </div>
  );
}

export default Landing;
