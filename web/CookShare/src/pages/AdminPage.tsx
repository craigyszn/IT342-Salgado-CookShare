import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChefHat, Users, ArrowLeft, Trash2, Edit2, ShieldCheck, ShieldOff, Check, X } from 'lucide-react';
import { authService } from '../services/authService';
import '../styles/AdminPage.css';

const API_BASE_URL = 'http://localhost:8081';

// ── Types ─────────────────────────────────────────────────────────────────────

interface AdminUser {
  id: number;
  firstname: string;
  lastname: string;
  email: string;
  role: string;
  createdAt: string;
}

interface AdminRecipe {
  id: number;
  title: string;
  description: string;
  author: string;
  difficulty: string;
  createdAt: string;
}

// ── Helper ────────────────────────────────────────────────────────────────────

const getAuthHeaders = () => {
  const user = authService.getUser();
  return {
    'Content-Type': 'application/json',
    'X-User-Email': user?.email ?? '',
  };
};

// ── Main Component ────────────────────────────────────────────────────────────

const AdminPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'users' | 'recipes'>('users');
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [recipes, setRecipes] = useState<AdminRecipe[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingUser, setEditingUser] = useState<AdminUser | null>(null);
  const [editForm, setEditForm] = useState({ firstname: '', lastname: '', email: '', role: '' });
  const [toast, setToast] = useState('');

  useEffect(() => {
    if (!authService.isAdmin()) navigate('/dashboard');
  }, []);

  useEffect(() => {
    fetchUsers();
    fetchRecipes();
  }, []);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/api/admin/users`, { headers: getAuthHeaders() });
      if (!res.ok) throw new Error('Failed to fetch users');
      setUsers(await res.json());
    } catch {
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  };

  const fetchRecipes = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/admin/recipes`, { headers: getAuthHeaders() });
      if (!res.ok) throw new Error('Failed to fetch recipes');
      setRecipes(await res.json());
    } catch {
      // fail silently
    }
  };

  const handleDeleteUser = async (id: number) => {
    if (!confirm('Are you sure you want to delete this user?')) return;
    const res = await fetch(`${API_BASE_URL}/api/admin/users/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (res.ok) {
      setUsers(users.filter((u) => u.id !== id));
      showToast('User deleted successfully');
    }
  };

  const handlePromote = async (id: number) => {
    const res = await fetch(`${API_BASE_URL}/api/admin/users/${id}/promote`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });
    if (res.ok) {
      setUsers(users.map((u) => u.id === id ? { ...u, role: 'ADMIN' } : u));
      showToast('User promoted to Admin');
    }
  };

  const handleDemote = async (id: number) => {
    const res = await fetch(`${API_BASE_URL}/api/admin/users/${id}/demote`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });
    if (res.ok) {
      setUsers(users.map((u) => u.id === id ? { ...u, role: 'USER' } : u));
      showToast('User demoted to User');
    }
  };

  const handleEditUser = (user: AdminUser) => {
    setEditingUser(user);
    setEditForm({
      firstname: user.firstname,
      lastname: user.lastname,
      email: user.email,
      role: user.role,
    });
  };

  const handleSaveEdit = async () => {
    if (!editingUser) return;
    const res = await fetch(`${API_BASE_URL}/api/admin/users/${editingUser.id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(editForm),
    });
    if (res.ok) {
      setUsers(users.map((u) => u.id === editingUser.id ? { ...u, ...editForm } : u));
      setEditingUser(null);
      showToast('User updated successfully');
    }
  };

  const handleDeleteRecipe = async (id: number) => {
    if (!confirm('Are you sure you want to delete this recipe?')) return;
    const res = await fetch(`${API_BASE_URL}/api/admin/recipes/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    if (res.ok) {
      setRecipes(recipes.filter((r) => r.id !== id));
      showToast('Recipe deleted successfully');
    }
  };

  const currentUser = authService.getUser();

  return (
    <div className="admin-root">

      {/* Toast */}
      {toast && <div className="admin-toast">{toast}</div>}

      {/* Edit Modal */}
      {editingUser && (
        <div className="admin-modal-overlay" onClick={() => setEditingUser(null)}>
          <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
            <h3 className="admin-modal__title">Edit User</h3>
            <div className="admin-modal__field">
              <label>First Name</label>
              <input className="admin-input" value={editForm.firstname}
                onChange={(e) => setEditForm({ ...editForm, firstname: e.target.value })} />
            </div>
            <div className="admin-modal__field">
              <label>Last Name</label>
              <input className="admin-input" value={editForm.lastname}
                onChange={(e) => setEditForm({ ...editForm, lastname: e.target.value })} />
            </div>
            <div className="admin-modal__field">
              <label>Email</label>
              <input className="admin-input" type="email" value={editForm.email}
                onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} />
            </div>
            <div className="admin-modal__field">
              <label>Role</label>
              <select className="admin-select" value={editForm.role}
                onChange={(e) => setEditForm({ ...editForm, role: e.target.value })}>
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <div className="admin-modal__actions">
              <button className="admin-btn admin-btn--cancel" onClick={() => setEditingUser(null)}>
                <X size={15} /> Cancel
              </button>
              <button className="admin-btn admin-btn--save" onClick={handleSaveEdit}>
                <Check size={15} /> Save Changes
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Navbar */}
      <nav className="admin-nav">
        <div className="admin-nav__logo">
          <div className="admin-nav__logo-icon">
            <ChefHat size={18} color="white" />
          </div>
          <span className="admin-nav__logo-text">CookShare</span>
          <span className="admin-nav__badge">Admin</span>
        </div>
        <div className="admin-nav__right">
          <span className="admin-nav__user">{currentUser?.firstName} {currentUser?.lastName}</span>
          <button className="admin-nav__back" onClick={() => navigate('/dashboard')}>
            <ArrowLeft size={15} />
            Back to Dashboard
          </button>
        </div>
      </nav>

      {/* Main */}
      <main className="admin-main">

        <div className="admin-header">
          <h1 className="admin-header__title">Admin Panel</h1>
          <p className="admin-header__subtitle">Manage users and recipes across CookShare</p>
        </div>

        {/* Stats */}
        <div className="admin-stats">
          <div className="admin-stat-card">
            <Users size={22} color="#f97316" />
            <div>
              <p className="admin-stat-card__value">{users.length}</p>
              <p className="admin-stat-card__label">Total Users</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <ShieldCheck size={22} color="#3b82f6" />
            <div>
              <p className="admin-stat-card__value">{users.filter(u => u.role === 'ADMIN').length}</p>
              <p className="admin-stat-card__label">Admins</p>
            </div>
          </div>
          <div className="admin-stat-card">
            <ChefHat size={22} color="#22c55e" />
            <div>
              <p className="admin-stat-card__value">{recipes.length}</p>
              <p className="admin-stat-card__label">Total Recipes</p>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="admin-tabs">
          <button
            className={`admin-tab${activeTab === 'users' ? ' admin-tab--active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            <Users size={15} /> Users ({users.length})
          </button>
          <button
            className={`admin-tab${activeTab === 'recipes' ? ' admin-tab--active' : ''}`}
            onClick={() => setActiveTab('recipes')}
          >
            <ChefHat size={15} /> Recipes ({recipes.length})
          </button>
        </div>

        {/* Content */}
        {loading ? (
          <div className="admin-loading">Loading...</div>
        ) : error ? (
          <div className="admin-error">{error}</div>
        ) : activeTab === 'users' ? (

          /* ── Users Table ── */
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Joined</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td className="admin-table__id">#{user.id}</td>
                    <td className="admin-table__name">
                      <div className="admin-table__avatar">
                        {(user.firstname?.charAt(0) ?? '?').toUpperCase()}
                      </div>
                      {user.firstname} {user.lastname}
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`admin-role-badge ${user.role === 'ADMIN' ? 'admin-role-badge--admin' : 'admin-role-badge--user'}`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="admin-table__date">
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '—'}
                    </td>
                    <td>
                      <div className="admin-table__actions">
                        <button className="admin-action-btn admin-action-btn--edit" onClick={() => handleEditUser(user)} title="Edit">
                          <Edit2 size={14} />
                        </button>
                        {user.role === 'USER' ? (
                          <button className="admin-action-btn admin-action-btn--promote" onClick={() => handlePromote(user.id)} title="Promote to Admin">
                            <ShieldCheck size={14} />
                          </button>
                        ) : (
                          <button className="admin-action-btn admin-action-btn--demote" onClick={() => handleDemote(user.id)} title="Demote to User">
                            <ShieldOff size={14} />
                          </button>
                        )}
                        <button className="admin-action-btn admin-action-btn--delete" onClick={() => handleDeleteUser(user.id)} title="Delete">
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

        ) : (

          /* ── Recipes Table ── */
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Author</th>
                  <th>Difficulty</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {recipes.map((recipe) => (
                  <tr key={recipe.id}>
                    <td className="admin-table__id">#{recipe.id}</td>
                    {/* ← removed admin-table__name class here — it caused misalignment */}
                    <td className="admin-table__recipe-title">{recipe.title}</td>
                    <td>{recipe.author}</td>
                    <td>
                      <span className={`admin-difficulty-badge admin-difficulty-badge--${recipe.difficulty?.toLowerCase()}`}>
                        {recipe.difficulty}
                      </span>
                    </td>
                    <td className="admin-table__date">
                      {recipe.createdAt ? new Date(recipe.createdAt).toLocaleDateString() : '—'}
                    </td>
                    <td>
                      <div className="admin-table__actions">
                        <button className="admin-action-btn admin-action-btn--delete" onClick={() => handleDeleteRecipe(recipe.id)} title="Delete">
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
};

export default AdminPage;