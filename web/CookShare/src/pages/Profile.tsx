import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChefHat, ArrowLeft, User, Mail, Save } from 'lucide-react';
import { authService } from '../services/authService';
import '../styles/Profile.css';

// ── Types ─────────────────────────────────────────────────────────────────────

type ToastState = { message: string; type: 'success' | 'error' } | null;

// ── Main Component ────────────────────────────────────────────────────────────

export function Profile() {
  const navigate = useNavigate();
  const [toast, setToast] = useState<ToastState>(null);

  // Read-only fields (from registration)
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  // Editable fields
  const [email, setEmail] = useState('');
  const [bio, setBio] = useState('');
  const [location, setLocation] = useState('');
  const [favoriteFood, setFavoriteFood] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');

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

  // ── Save (firstName & lastName intentionally excluded — read-only) ─────────

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();

    const current = authService.getUser();
    const updated = {
      ...current,
      email,
      bio,
      location,
      favoriteFood,
      avatarUrl,
    };

    localStorage.setItem('user', JSON.stringify(updated));
    showToast('Profile updated successfully!', 'success');
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
        <div className="profile-nav__logo">
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
                <span className="profile-stats__row-value">0</span>
              </div>
              <div className="profile-stats__row">
                <span className="profile-stats__row-label">Favorites</span>
                <span className="profile-stats__row-value">0</span>
              </div>
              <div className="profile-stats__row">
                <span className="profile-stats__row-label">Comments</span>
                <span className="profile-stats__row-value">0</span>
              </div>
            </div>
          </div>

          {/* Right — Personal Info Form */}
          <div className="profile-section">
            <p className="profile-section__title">Personal Information</p>
            <p className="profile-section__subtitle">Update your personal details</p>

            <form onSubmit={handleSave}>

              {/* First Name — read-only */}
              <div className="profile-field">
                <label className="profile-label">
                  <User size={15} />
                  First Name
                </label>
                <input
                  className="profile-input profile-input--readonly"
                  type="text"
                  value={firstName}
                  readOnly
                />
                <p className="profile-hint">Name cannot be changed after registration</p>
              </div>

              {/* Last Name — read-only */}
              <div className="profile-field">
                <label className="profile-label">
                  <User size={15} />
                  Last Name
                </label>
                <input
                  className="profile-input profile-input--readonly"
                  type="text"
                  value={lastName}
                  readOnly
                />
              </div>

              {/* Email */}
              <div className="profile-field">
                <label className="profile-label">
                  <Mail size={15} />
                  Email
                </label>
                <input
                  className="profile-input"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              {/* Location */}
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

              {/* Favorite Food */}
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

              {/* Bio */}
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
                <button
                  type="button"
                  className="profile-actions__cancel"
                  onClick={() => navigate('/dashboard')}
                >
                  Cancel
                </button>
                <button type="submit" className="profile-actions__save">
                  <Save size={15} />
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Account Settings */}
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
      </main>
    </div>
  );
}

export default Profile;