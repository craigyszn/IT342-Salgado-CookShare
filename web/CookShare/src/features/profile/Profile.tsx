import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChefHat, ArrowLeft, User, Mail, Save, Clock, UtensilsCrossed, Trash2, Image as ImageIcon } from 'lucide-react';
import { authService } from '../auth/authService';
import './Profile.css';

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

export function Profile() {
  const navigate = useNavigate();
  const [toast, setToast] = useState<ToastState>(null);
  const [activeTab, setActiveTab] = useState<ActiveTab>('profile');

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [bio, setBio] = useState('');
  const [location, setLocation] = useState('');
  const [favoriteFood, setFavoriteFood] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [avatarUploading, setAvatarUploading] = useState(false);
  const [saving, setSaving] = useState(false);

  const [stats, setStats] = useState({ recipesShared: 0, favorites: 0, comments: 0 });
  const [myRecipes, setMyRecipes] = useState<UserRecipe[]>([]);
  const [recipesLoading, setRecipesLoading] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const user = authService.getUser();
    if (user?.email) {
      fetch(`http://localhost:8081/api/users/stats?email=${encodeURIComponent(user.email)}`)
        .then((res) => res.json())
        .then((data) => setStats(data))
        .catch(() => {});
    }
  }, []);

  useEffect(() => {
    const user = authService.getUser();
    if (!user) { navigate('/login'); return; }
    setFirstName(user.firstName || '');
    setLastName(user.lastName || '');
    setEmail(user.email || '');
    setAvatarUrl(user.avatarUrl || '');
  }, [navigate]);

  // ── Load profile fields from backend on mount ─────────────────────────────
  useEffect(() => {
    const user = authService.getUser();
    if (!user?.email) return;
    fetch(`http://localhost:8081/api/users/profile?email=${encodeURIComponent(user.email)}`)
      .then((res) => res.json())
      .then((data) => {
        if (data.bio) setBio(data.bio);
        if (data.location) setLocation(data.location);
        if (data.favoriteFood) setFavoriteFood(data.favoriteFood);
        if (data.profilePhotoUrl) setAvatarUrl(data.profilePhotoUrl);
      })
      .catch(() => {});
  }, []);

  // ── Fetch profile photo from backend on load ──────────────────────────────
  useEffect(() => {
    const user = authService.getUser();
    if (!user?.email) return;
    fetch(`http://localhost:8081/api/users/profile-photo?email=${encodeURIComponent(user.email)}`)
      .then((res) => res.json())
      .then((data) => {
        if (data.profilePhotoUrl) setAvatarUrl(data.profilePhotoUrl);
      })
      .catch(() => {});
  }, []);

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

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const getInitials = () => {
    const f = firstName.charAt(0).toUpperCase();
    const l = lastName.charAt(0).toUpperCase();
    return (f + l) || 'U';
  };

  // ── Avatar Upload ──────────────────────────────────────────────────────────
  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const user = authService.getUser();
    if (!user?.email) return;

    setAvatarUrl(URL.createObjectURL(file));
    setAvatarUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('email', user.email);

      const response = await fetch('http://localhost:8081/api/users/upload-profile-photo', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) throw new Error('Upload failed');

      const data = await response.json();
      setAvatarUrl(data.profilePhotoUrl);

      const updated = { ...user, avatarUrl: data.profilePhotoUrl };
      localStorage.setItem('user', JSON.stringify(updated));

      showToast('Profile photo updated!', 'success');
    } catch {
      showToast('Photo upload failed. Please try again.', 'error');
      setAvatarUrl(user.avatarUrl || '');
    } finally {
      setAvatarUploading(false);
    }
  };

  // ── Save profile to backend ───────────────────────────────────────────────
  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    const user = authService.getUser();
    if (!user?.email) return;

    setSaving(true);
    try {
      const res = await fetch('http://localhost:8081/api/users/update-profile', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: user.email,
          bio,
          location,
          favoriteFood,
        }),
      });

      if (!res.ok) throw new Error('Failed to save');

      // Also update localStorage
      const updated = { ...user, email, bio, location, favoriteFood, avatarUrl };
      localStorage.setItem('user', JSON.stringify(updated));

      showToast('Profile updated successfully!', 'success');
    } catch {
      showToast('Failed to save profile. Please try again.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteRecipe = async (id: string) => {
    if (!confirm('Are you sure you want to delete this recipe?')) return;
    try {
      const res = await fetch(`http://localhost:8081/api/recipes/${id}`, { method: 'DELETE' });
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

  return (
    <div className="profile-root">
      {toast && (
        <div className={`profile-toast profile-toast--${toast.type}`}>
          {toast.message}
        </div>
      )}

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

            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              style={{ display: 'none' }}
              onChange={handleAvatarChange}
            />
            <button
              type="button"
              className="create-upload-btn"
              onClick={() => fileInputRef.current?.click()}
              disabled={avatarUploading}
              style={{ width: '100%', marginTop: '0.75rem' }}
            >
              <ImageIcon size={16} />
              {avatarUploading ? 'Uploading...' : avatarUrl ? 'Change Photo' : 'Upload Photo'}
            </button>
            <p className="profile-hint">Upload a profile photo (JPG, PNG, etc.)</p>

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
                    <button type="submit" className="profile-actions__save" disabled={saving}>
                      <Save size={15} />
                      {saving ? 'Saving...' : 'Save Changes'}
                    </button>
                  </div>
                </form>
              </>
            )}

            {activeTab === 'recipes' && (
              <div className="profile-recipes">
                <div className="profile-recipes__header">
                  <p className="profile-section__title" style={{ margin: 0 }}>My Published Recipes</p>
                  <button className="profile-recipes__create-btn" onClick={() => navigate('/create-recipe')}>
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
                <p className="profile-settings-row__desc">Manage your account preferences</p>
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
