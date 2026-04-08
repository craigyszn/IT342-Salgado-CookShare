import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChefHat, ArrowLeft, User, Mail, Save, Clock, UtensilsCrossed, Trash2 } from 'lucide-react';
import { authService } from '../services/authService';
import '../styles/Profile.css';

// ── Types ─────────────────────────────────────────────────────────────────────

type ToastState = { message: string; type: 'success' | 'error' } | null;
type ActiveTab = 'profile' | 'recipes';

interface UserRecipe {
  id: string;
  title: string;
  description: string;
  image: string;
  difficulty: string;
  cookTime: string;
  prepTime: string;
  servings: number;
  category: string;
  createdAt: string;
}

// ── Main Component ────────────────────────────────────────────────────────────

export function Profile() {
  const navigate = useNavigate();
  const [toast, setToast] = useState<ToastState>(null);
  const [activeTab, setActiveTab] = useState<ActiveTab>('profile');

  // Read-only fields (from registration)
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  // Editable fields
  const [email, setEmail] = useState('');
  const [bio, setBio] = useState('');
  const [location, setLocation] = useState('');
  const [favoriteFood, setFavoriteFood] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');

  // Stats
  const [stats, setStats] = useState({ recipesShared: 0, favorites: 0, comments: 0 });

  // My Recipes
  const [myRecipes, setMyRecipes] = useState<UserRecipe[]>([]);
  const [recipesLoading, setRecipesLoading] = useState(false);

  // Fetch real stats from backend
  useEffect(() => {
    const user = authService.getUser();
    if (user?.email) {
      fetch(`http://localhost:8081/api/users/stats?email=${encodeURIComponent(user.email)}`)
        .then((res) => res.json())
        .then((data) => setStats(data))
        .catch(() => {});
    }
  }, []);

  // Redirect if not logged in & pre-fill from localStorage
  useEffect(() => {
    const user = authService.getUser();
    if (!user) {
      navigate('/login');
      return;
    }
    setFirstName(user.firstName || '');
    setLastName(user.lastName || '');
    setEmail(user.email || '');
    setBio(user.bio || '');
    setLocation(user.location || '');
    setFavoriteFood(user.favoriteFood || '');
    setAvatarUrl(user.avatarUrl || '');
  }, [navigate]);

  // Fetch user's recipes when My Recipes tab is opened
  useEffect(() => {
    if (activeTab !== 'recipes') return;
    const user = authService.getUser();
    if (!user?.email) return;

    setRecipesLoading(true);
    fetch(`http://localhost:8081/api/recipes/user?email=${encodeURIComponent(user.email)}`)
      .then((res) => res.json())
      .then((data) => setMyRecipes(data))
      .catch(() => setMyRecipes([]))
      .finally(() => setRecipesLoading(false));
  }, [activeTab]);

  // ── Helpers ────────────────────────────────────────────────────────────────

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const getInitials = () => {
    const f = firstName.charAt(0).toUpperCase();
    const l = lastName.charAt(0).toUpperCase();
    return (f + l) || 'U';
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const current = authService.getUser();
    const updated = { ...current, email, bio, location, favoriteFood, avatarUrl };
    localStorage.setItem('user', JSON.stringify(updated));
    showToast('Profile updated successfully!', 'success');
  };

  const handleDeleteRecipe = async (id: string) => {
    if (!confirm('Are you sure you want to delete this recipe?')) return;
    try {
      const res = await fetch(`http://localhost:8081/api/recipes/${id}`, {
        method: 'DELETE',
      });
      if (res.ok) {
        setMyRecipes(myRecipes.filter((r) => r.id !== id));
        setStats((prev) => ({ ...prev, recipesShared: prev.recipesShared - 1 }));
        showToast('Recipe deleted successfully', 'success');
      }
    } catch {
      showToast('Failed to delete recipe', 'error');
    }
  };

  const getDifficultyClass = (difficulty: string) => {
    switch (difficulty?.toLowerCase()) {
      case 'easy':   return 'profile-recipe-card__badge--easy';
      case 'medium': return 'profile-recipe-card__badge--medium';
      case 'hard':   return 'profile-recipe-card__badge--hard';
      default:       return 'profile-recipe-card__badge--easy';
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="profile-root">
      {/* Toast */}
      {toast && (
        <div className={`profile-toast profile-toast--${toast.type}`}>
          {toast.message}
        </div>
      )}

      {/* Navbar */}
      <nav className="profile-nav">
          <div className="profile-nav__logo" onClick={() => navigate('/dashboard')} style={{ cursor: 'pointer' }}>
            <div className="profile-nav__logo-icon">
              <ChefHat size={18} color="white" />
            </div>
            <span className="profile-nav__logo-text">CookShare</span>
          </div>
        <button className="profile-nav__back" onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={16} />
          Back to Dashboard
        </button>
      </nav>

      {/* Main */}
      <main className="profile-main">
        <div className="profile-page-header">
          <h2 className="profile-page-header__title">My Profile</h2>
          <p className="profile-page-header__subtitle">Manage your account settings and preferences</p>
        </div>

        <div className="profile-grid">
          {/* Left — Avatar + Stats */}
          <div className="profile-section profile-avatar-col">
            <p className="profile-section__title">Profile Picture</p>
            <p className="profile-section__subtitle">Your public avatar</p>

            <div className="profile-avatar">
              {avatarUrl ? (
                <img src={avatarUrl} alt={`${firstName} ${lastName}`} />
              ) : (
                getInitials()
              )}
            </div>

            <div className="profile-field" style={{ width: '100%' }}>
              <label className="profile-label">Avatar URL</label>
              <input
                className="profile-input"
                type="url"
                placeholder="https://example.com/avatar.jpg"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
              />
              <p className="profile-hint">Enter a URL to an image for your profile picture</p>
            </div>

            <div className="profile-stats">
              <p className="profile-stats__title">Account Stats</p>
              <div className="profile-stats__row">
                <span className="profile-stats__row-label">Recipes Shared</span>
                <span className="profile-stats__row-value">{stats.recipesShared}</span>
              </div>
              <div className="profile-stats__row">
                <span className="profile-stats__row-label">Favorites</span>
                <span className="profile-stats__row-value">{stats.favorites}</span>
              </div>
              <div className="profile-stats__row">
                <span className="profile-stats__row-label">Comments</span>
                <span className="profile-stats__row-value">{stats.comments}</span>
              </div>
            </div>
          </div>

          {/* Right — Tabs */}
          <div className="profile-section">

            {/* Tab Headers */}
            <div className="profile-tabs">
              <button
                className={`profile-tab${activeTab === 'profile' ? ' profile-tab--active' : ''}`}
                onClick={() => setActiveTab('profile')}
              >
                <User size={15} />
                Profile
              </button>
              <button
                className={`profile-tab${activeTab === 'recipes' ? ' profile-tab--active' : ''}`}
                onClick={() => setActiveTab('recipes')}
              >
                <UtensilsCrossed size={15} />
                My Recipes
                {stats.recipesShared > 0 && (
                  <span className="profile-tab__count">{stats.recipesShared}</span>
                )}
              </button>
            </div>

            {/* ── Profile Tab ── */}
            {activeTab === 'profile' && (
              <>
                <p className="profile-section__title" style={{ marginTop: '1.25rem' }}>Personal Information</p>
                <p className="profile-section__subtitle">Update your personal details</p>

                <form onSubmit={handleSave}>
                  <div className="profile-field">
                    <label className="profile-label"><User size={15} /> First Name</label>
                    <input className="profile-input profile-input--readonly" type="text" value={firstName} readOnly />
                    <p className="profile-hint">Name cannot be changed after registration</p>
                  </div>

                  <div className="profile-field">
                    <label className="profile-label"><User size={15} /> Last Name</label>
                    <input className="profile-input profile-input--readonly" type="text" value={lastName} readOnly />
                  </div>

                  <div className="profile-field">
                    <label className="profile-label"><Mail size={15} /> Email</label>
                    <input
                      className="profile-input"
                      type="email"
                      placeholder="you@example.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>

                  <div className="profile-field">
                    <label className="profile-label">Location</label>
                    <input
                      className="profile-input"
                      type="text"
                      placeholder="San Francisco, CA"
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                    />
                  </div>

                  <div className="profile-field">
                    <label className="profile-label">Favorite Food</label>
                    <input
                      className="profile-input"
                      type="text"
                      placeholder="Pizza, Sushi, Pasta..."
                      value={favoriteFood}
                      onChange={(e) => setFavoriteFood(e.target.value)}
                    />
                  </div>

                  <div className="profile-field">
                    <label className="profile-label">Bio</label>
                    <textarea
                      className="profile-textarea"
                      placeholder="Tell us a bit about yourself and your cooking journey..."
                      value={bio}
                      onChange={(e) => setBio(e.target.value)}
                      rows={4}
                      maxLength={500}
                    />
                    <p className="profile-hint">{bio.length}/500 characters</p>
                  </div>

                  <div className="profile-actions">
                    <button type="button" className="profile-actions__cancel" onClick={() => navigate('/dashboard')}>
                      Cancel
                    </button>
                    <button type="submit" className="profile-actions__save">
                      <Save size={15} />
                      Save Changes
                    </button>
                  </div>
                </form>
              </>
            )}

            {/* ── My Recipes Tab ── */}
            {activeTab === 'recipes' && (
              <div className="profile-recipes">
                <div className="profile-recipes__header">
                  <p className="profile-section__title" style={{ margin: 0 }}>My Published Recipes</p>
                  <button
                    className="profile-recipes__create-btn"
                    onClick={() => navigate('/create-recipe')}
                  >
                    + New Recipe
                  </button>
                </div>

                {recipesLoading ? (
                  <div className="profile-recipes__loading">Loading your recipes...</div>
                ) : myRecipes.length === 0 ? (
                  <div className="profile-recipes__empty">
                    <UtensilsCrossed size={40} color="#d1d5db" />
                    <p className="profile-recipes__empty-title">No recipes yet</p>
                    <p className="profile-recipes__empty-sub">Share your first recipe with the community!</p>
                    <button
                      className="profile-recipes__create-btn profile-recipes__create-btn--lg"
                      onClick={() => navigate('/create-recipe')}
                    >
                      Create Recipe
                    </button>
                  </div>
                ) : (
                  <div className="profile-recipe-list">
                    {myRecipes.map((recipe) => (
                      <div key={recipe.id} className="profile-recipe-card">
                        <div className="profile-recipe-card__image-wrapper">
                          <img
                            src={recipe.image || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400'}
                            alt={recipe.title}
                            className="profile-recipe-card__image"
                          />
                        </div>
                        <div className="profile-recipe-card__body">
                          <div className="profile-recipe-card__top">
                            <h3 className="profile-recipe-card__title">{recipe.title}</h3>
                            <span className={`profile-recipe-card__badge ${getDifficultyClass(recipe.difficulty)}`}>
                              {recipe.difficulty}
                            </span>
                          </div>
                          <p className="profile-recipe-card__desc">
                            {recipe.description?.slice(0, 100)}{recipe.description?.length > 100 ? '...' : ''}
                          </p>
                          <div className="profile-recipe-card__meta">
                            <span className="profile-recipe-card__meta-item">
                              <Clock size={13} color="#9ca3af" />
                              {recipe.cookTime || 'N/A'}
                            </span>
                            <span className="profile-recipe-card__meta-item">
                              <UtensilsCrossed size={13} color="#9ca3af" />
                              {recipe.servings} servings
                            </span>
                            {recipe.category && (
                              <span className="profile-recipe-card__category">{recipe.category}</span>
                            )}
                          </div>
                          <div className="profile-recipe-card__footer">
                            <span className="profile-recipe-card__date">
                              Posted {recipe.createdAt || 'recently'}
                            </span>
                            <button
                              className="profile-recipe-card__delete"
                              onClick={() => handleDeleteRecipe(recipe.id)}
                              title="Delete recipe"
                            >
                              <Trash2 size={14} />
                              Delete
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Account Settings — only show on profile tab */}
        {activeTab === 'profile' && (
          <div className="profile-section">
            <p className="profile-section__title">Account Settings</p>
            <p className="profile-section__subtitle">Manage your account preferences</p>

            <div className="profile-settings-row">
              <div>
                <p className="profile-settings-row__title">Email Notifications</p>
                <p className="profile-settings-row__desc">Receive updates about new recipes and comments</p>
              </div>
              <button className="profile-settings-btn">Configure</button>
            </div>

            <div className="profile-settings-row">
              <div>
                <p className="profile-settings-row__title">Privacy Settings</p>
                <p className="profile-settings-row__desc">Control who can see your profile and recipes</p>
              </div>
              <button className="profile-settings-btn">Manage</button>
            </div>

            <div className="profile-settings-row profile-settings-row--danger">
              <div>
                <p className="profile-settings-row__title">Delete Account</p>
                <p className="profile-settings-row__desc">Permanently delete your account and all data</p>
              </div>
              <button className="profile-settings-btn profile-settings-btn--danger">Delete</button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default Profile;