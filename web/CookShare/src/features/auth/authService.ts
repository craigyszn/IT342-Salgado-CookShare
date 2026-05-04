const API_BASE_URL = import.meta.env.VITE_API_URL;

export const authService = {
  login: async (email: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();
    const ok = data.message === 'Login successful';

    if (ok) {
      localStorage.setItem('user', JSON.stringify({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        role: data.role,
      }));
      // Save JWT token
      localStorage.setItem('accessToken', data.accessToken || '');
    }

    return { ok, status: ok ? 200 : 401, message: data.message };
  },

  logout: () => {
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken');
  },

  getUser: () => {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  },

  isAdmin: () => {
    const raw = localStorage.getItem('user');
    if (!raw) return false;
    const user = JSON.parse(raw);
    return user?.role === 'ADMIN';
  },

  getToken: () => {
    return localStorage.getItem('accessToken') || '';
  },

  register: async (firstName: string, lastName: string, email: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ firstName, lastName, email, password }),
    });

    const data = await response.text();
    return {
      ok: data === 'User registered successfully',
      status: data === 'User registered successfully' ? 200 : 400,
      message: data,
    };
  },

  getGoogleAuthUrl: () => {
    return `${API_BASE_URL}/oauth2/authorization/google`;
  },
};
