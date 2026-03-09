const API_BASE_URL = 'http://localhost:8080';

export const authService = {
  login: async (email: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.text();

    return {
      ok: data === 'Login successful',
      status: data === 'Login successful' ? 200 : 401,
      message: data,
    };
  },

  register: async (firstName: string, lastName: string, email: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
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
