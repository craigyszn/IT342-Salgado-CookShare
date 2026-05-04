import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const OAuthSuccess = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const firstName   = searchParams.get('firstName') || '';
    const lastName    = searchParams.get('lastName') || '';
    const email       = searchParams.get('email') || '';
    const role        = searchParams.get('role') || 'USER';
    const accessToken = searchParams.get('accessToken') || '';

    localStorage.setItem('user', JSON.stringify({ firstName, lastName, email, role }));

    // Save JWT token from OAuth login
    if (accessToken) {
      localStorage.setItem('accessToken', accessToken);
    }

    navigate('/dashboard');
  }, []);

  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <p>Signing you in...</p>
    </div>
  );
};

export default OAuthSuccess;
