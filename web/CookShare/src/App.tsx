import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Landing from './features/landing/Landing';
import Login from './features/auth/Login';
import Register from './features/auth/Register';
import Dashboard from './features/dashboard/Dashboard';
import CreateRecipe from './features/recipe/CreateRecipe';
import Profile from './features/profile/Profile';
import OAuthSuccess from './features/auth/OAuthSuccessPage';
import AdminPage from './features/admin/AdminPage';
import './App.css';
 
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/create-recipe" element={<CreateRecipe />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/oauth-success" element={<OAuthSuccess />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
    </Router>
  );
}
 
export default App;