import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from './authService';
import './Auth.css';

// ── Password strength checker ─────────────────────────────────────────────────

type StrengthLevel = 'weak' | 'fair' | 'good' | 'strong';

interface StrengthResult {
  level: StrengthLevel;
  score: number; // 0–4
  label: string;
  color: string;
  checks: {
    length: boolean;
    uppercase: boolean;
    lowercase: boolean;
    number: boolean;
    special: boolean;
  };
}

const checkPasswordStrength = (password: string): StrengthResult => {
  const checks = {
    length:    password.length >= 8,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number:    /[0-9]/.test(password),
    special:   /[^A-Za-z0-9]/.test(password),
  };

  const score = Object.values(checks).filter(Boolean).length;

  let level: StrengthLevel;
  let label: string;
  let color: string;

  if (score <= 1) {
    level = 'weak';   label = 'Weak';   color = '#dc2626';
  } else if (score === 2) {
    level = 'fair';   label = 'Fair';   color = '#f97316';
  } else if (score === 3) {
    level = 'good';   label = 'Good';   color = '#eab308';
  } else {
    level = 'strong'; label = 'Strong'; color = '#16a34a';
  }

  return { level, score, label, color, checks };
};

// ── Component ─────────────────────────────────────────────────────────────────

function Register() {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const strength = checkPasswordStrength(formData.password);
  const showStrength = formData.password.length > 0;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Password strength gate — must be at least "good"
    if (showStrength && (strength.level === 'weak' || strength.level === 'fair')) {
      setError('Please use a stronger password before continuing.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    setLoading(true);

    try {
      const result = await authService.register(
        formData.firstName,
        formData.lastName,
        formData.email,
        formData.password
      );

      if (!result.ok) {
        setError(result.message || 'Registration failed');
        return;
      }

      navigate('/login', { state: { message: 'Registration successful! Please log in.' } });
    } catch (err) {
      setError('An error occurred. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignUp = () => {
    window.location.href = authService.getGoogleAuthUrl();
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="logo">👨‍🍳</div>
          <h1>Join CookShare</h1>
          <p>Create an account to share your recipes</p>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleRegister} className="auth-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="firstName">First Name</label>
              <input
                id="firstName"
                type="text"
                name="firstName"
                placeholder="John"
                value={formData.firstName}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lastName">Last Name</label>
              <input
                id="lastName"
                type="text"
                name="lastName"
                placeholder="Doe"
                value={formData.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              name="email"
              placeholder="you@example.com"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              name="password"
              placeholder="At least 8 characters"
              value={formData.password}
              onChange={handleChange}
              required
            />

            {/* ── Strength Bar ── */}
            {showStrength && (
              <div className="password-strength">
                <div className="password-strength__bars">
                  {[1, 2, 3, 4].map((bar) => (
                    <div
                      key={bar}
                      className="password-strength__bar"
                      style={{
                        backgroundColor: strength.score >= bar ? strength.color : '#e5e7eb',
                        transition: 'background-color 0.2s',
                      }}
                    />
                  ))}
                </div>
                <span
                  className="password-strength__label"
                  style={{ color: strength.color }}
                >
                  {strength.label}
                </span>
              </div>
            )}

            {/* ── Requirements Checklist ── */}
            {showStrength && (
              <ul className="password-checks">
                <li className={strength.checks.length    ? 'check--pass' : 'check--fail'}>
                  {strength.checks.length    ? '✓' : '✗'} At least 8 characters
                </li>
                <li className={strength.checks.uppercase ? 'check--pass' : 'check--fail'}>
                  {strength.checks.uppercase ? '✓' : '✗'} One uppercase letter
                </li>
                <li className={strength.checks.lowercase ? 'check--pass' : 'check--fail'}>
                  {strength.checks.lowercase ? '✓' : '✗'} One lowercase letter
                </li>
                <li className={strength.checks.number    ? 'check--pass' : 'check--fail'}>
                  {strength.checks.number    ? '✓' : '✗'} One number
                </li>
                <li className={strength.checks.special   ? 'check--pass' : 'check--fail'}>
                  {strength.checks.special   ? '✓' : '✗'} One special character (!@#$...)
                </li>
              </ul>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              name="confirmPassword"
              placeholder="Confirm your password"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
            {/* Confirm match indicator */}
            {formData.confirmPassword.length > 0 && (
              <p className={
                formData.password === formData.confirmPassword
                  ? 'password-match password-match--ok'
                  : 'password-match password-match--no'
              }>
                {formData.password === formData.confirmPassword
                  ? '✓ Passwords match'
                  : '✗ Passwords do not match'}
              </p>
            )}
          </div>

          <button
            type="submit"
            className="auth-button"
            disabled={loading || (showStrength && (strength.level === 'weak' || strength.level === 'fair'))}
          >
            {loading ? 'Creating account...' : 'Sign Up'}
          </button>
        </form>

        <div className="divider">OR</div>

        <button type="button" className="google-button" onClick={handleGoogleSignUp}>
          <svg className="google-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
          </svg>
          Sign up with Google
        </button>

        <div className="auth-footer">
          <p>Already have an account? <a href="/login">Sign in</a></p>
        </div>
      </div>
    </div>
  );
}

export default Register;
