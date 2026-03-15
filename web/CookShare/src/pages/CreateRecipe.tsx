import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChefHat, ArrowLeft, Plus, X, Save, Image as ImageIcon } from 'lucide-react';
import { authService } from '../services/authService';
import '../styles/CreateRecipe.css';

// ── Types ─────────────────────────────────────────────────────────────────────

type ToastState = { message: string; type: 'success' | 'error' } | null;

// ── Main Component ────────────────────────────────────────────────────────────

export function CreateRecipe() {
  const navigate = useNavigate();
  const [toast, setToast] = useState<ToastState>(null);

  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [prepTime, setPrepTime] = useState('');
  const [cookTime, setCookTime] = useState('');
  const [servings, setServings] = useState('');
  const [difficulty, setDifficulty] = useState<'Easy' | 'Medium' | 'Hard'>('Easy');
  const [category, setCategory] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState('');
  const [ingredients, setIngredients] = useState<string[]>(['']);
  const [instructions, setInstructions] = useState<string[]>(['']);

  // Redirect if not logged in
  useEffect(() => {
    const user = authService.getUser();
    if (!user) navigate('/login');
  }, [navigate]);

  const user = authService.getUser();

  // ── Toast helper ───────────────────────────────────────────────────────────

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  // ── Tag handlers ───────────────────────────────────────────────────────────

  const addTag = () => {
    if (tagInput.trim() && !tags.includes(tagInput.trim())) {
      setTags([...tags, tagInput.trim()]);
      setTagInput('');
    }
  };

  const removeTag = (tag: string) => setTags(tags.filter((t) => t !== tag));

  // ── Ingredient handlers ────────────────────────────────────────────────────

  const addIngredient = () => setIngredients([...ingredients, '']);
  const removeIngredient = (i: number) => setIngredients(ingredients.filter((_, idx) => idx !== i));
  const updateIngredient = (i: number, value: string) => {
    const updated = [...ingredients];
    updated[i] = value;
    setIngredients(updated);
  };

  // ── Instruction handlers ───────────────────────────────────────────────────

  const addInstruction = () => setInstructions([...instructions, '']);
  const removeInstruction = (i: number) => setInstructions(instructions.filter((_, idx) => idx !== i));
  const updateInstruction = (i: number, value: string) => {
    const updated = [...instructions];
    updated[i] = value;
    setInstructions(updated);
  };

  // ── Submit ─────────────────────────────────────────────────────────────────

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!title || !description || !category) {
      showToast('Please fill in all required fields', 'error');
      return;
    }
    if (ingredients.filter((i) => i.trim()).length === 0) {
      showToast('Please add at least one ingredient', 'error');
      return;
    }
    if (instructions.filter((i) => i.trim()).length === 0) {
      showToast('Please add at least one instruction', 'error');
      return;
    }

    const newRecipe = {
      id: Date.now().toString(),
      title,
      author: user ? `${user.firstName} ${user.lastName}` : 'Unknown',
      description,
      image: imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1080',
      prepTime,
      cookTime,
      servings: parseInt(servings) || 4,
      difficulty,
      rating: 0,
      reviewCount: 0,
      category,
      tags,
      ingredients: ingredients.filter((i) => i.trim()),
      instructions: instructions.filter((i) => i.trim()),
      createdAt: new Date().toISOString().split('T')[0],
    };

    const existing = JSON.parse(localStorage.getItem('user_recipes') || '[]');
    existing.push(newRecipe);
    localStorage.setItem('user_recipes', JSON.stringify(existing));

    showToast('Recipe created successfully!', 'success');
    setTimeout(() => navigate('/dashboard'), 1000);
  };

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="create-root">
      {/* Toast */}
      {toast && (
        <div className={`create-toast create-toast--${toast.type}`}>
          {toast.message}
        </div>
      )}

      {/* Navbar */}
      <nav className="create-nav">
        <div className="create-nav__logo">
          <div className="create-nav__logo-icon">
            <ChefHat size={18} color="white" />
          </div>
          <span className="create-nav__logo-text">CookShare</span>
        </div>
        <button className="create-nav__back" onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={16} />
          Back to Dashboard
        </button>
      </nav>

      {/* Main */}
      <main className="create-main">
        <div className="create-page-header">
          <h2 className="create-page-header__title">Create New Recipe</h2>
          <p className="create-page-header__subtitle">Share your delicious recipe with the community</p>
        </div>

        <form onSubmit={handleSubmit}>

          {/* Basic Information */}
          <div className="create-section">
            <p className="create-section__title">Basic Information</p>
            <p className="create-section__subtitle">Tell us about your recipe</p>

            <div className="create-field">
              <label className="create-label">Recipe Title *</label>
              <input
                className="create-input"
                type="text"
                placeholder="e.g., Mom's Famous Chocolate Chip Cookies"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            <div className="create-field">
              <label className="create-label">Description *</label>
              <textarea
                className="create-textarea"
                placeholder="Describe your recipe and what makes it special..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                required
              />
            </div>

            <div className="create-field">
              <label className="create-label">
                <ImageIcon size={16} />
                Recipe Image URL
              </label>
              <input
                className="create-input"
                type="url"
                placeholder="https://example.com/recipe-image.jpg"
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
              />
              <p className="create-hint">Provide a URL to an image of your finished dish</p>
            </div>
          </div>

          {/* Recipe Details */}
          <div className="create-section">
            <p className="create-section__title">Recipe Details</p>
            <p className="create-section__subtitle">Time, servings, and difficulty</p>

            <div className="create-grid-3">
              <div className="create-field">
                <label className="create-label">Prep Time *</label>
                <input
                  className="create-input"
                  type="text"
                  placeholder="e.g., 15 mins"
                  value={prepTime}
                  onChange={(e) => setPrepTime(e.target.value)}
                  required
                />
              </div>
              <div className="create-field">
                <label className="create-label">Cook Time *</label>
                <input
                  className="create-input"
                  type="text"
                  placeholder="e.g., 30 mins"
                  value={cookTime}
                  onChange={(e) => setCookTime(e.target.value)}
                  required
                />
              </div>
              <div className="create-field">
                <label className="create-label">Servings *</label>
                <input
                  className="create-input"
                  type="number"
                  placeholder="4"
                  value={servings}
                  onChange={(e) => setServings(e.target.value)}
                  min="1"
                  required
                />
              </div>
            </div>

            <div className="create-grid-2">
              <div className="create-field">
                <label className="create-label">Difficulty *</label>
                <select
                  className="create-select"
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value as 'Easy' | 'Medium' | 'Hard')}
                >
                  <option value="Easy">Easy</option>
                  <option value="Medium">Medium</option>
                  <option value="Hard">Hard</option>
                </select>
              </div>
              <div className="create-field">
                <label className="create-label">Category *</label>
                <select
                  className="create-select"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  required
                >
                  <option value="" disabled>Select a category</option>
                  <option value="Pasta">Pasta</option>
                  <option value="Dessert">Dessert</option>
                  <option value="Salad">Salad</option>
                  <option value="Main Course">Main Course</option>
                  <option value="Asian">Asian</option>
                  <option value="Pizza">Pizza</option>
                  <option value="Breakfast">Breakfast</option>
                  <option value="Appetizer">Appetizer</option>
                  <option value="Soup">Soup</option>
                </select>
              </div>
            </div>

            <div className="create-field">
              <label className="create-label">Tags</label>
              <div className="create-tag-row">
                <input
                  className="create-input"
                  type="text"
                  placeholder="Add a tag (e.g., Vegetarian, Quick, Healthy)"
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') { e.preventDefault(); addTag(); }
                  }}
                />
                <button type="button" className="create-tag-add" onClick={addTag}>
                  <Plus size={16} />
                </button>
              </div>
              {tags.length > 0 && (
                <div className="create-tags-list">
                  {tags.map((tag) => (
                    <span key={tag} className="create-tag">
                      {tag}
                      <button type="button" className="create-tag__remove" onClick={() => removeTag(tag)}>
                        <X size={12} />
                      </button>
                    </span>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Ingredients */}
          <div className="create-section">
            <p className="create-section__title">Ingredients</p>
            <p className="create-section__subtitle">List all the ingredients needed</p>

            {ingredients.map((ingredient, i) => (
              <div key={i} className="create-item-row">
                <input
                  className="create-input"
                  type="text"
                  placeholder={`Ingredient ${i + 1}`}
                  value={ingredient}
                  onChange={(e) => updateIngredient(i, e.target.value)}
                />
                {ingredients.length > 1 && (
                  <button type="button" className="create-item-remove" onClick={() => removeIngredient(i)}>
                    <X size={16} />
                  </button>
                )}
              </div>
            ))}
            <button type="button" className="create-add-btn" onClick={addIngredient}>
              <Plus size={16} />
              Add Ingredient
            </button>
          </div>

          {/* Instructions */}
          <div className="create-section">
            <p className="create-section__title">Instructions</p>
            <p className="create-section__subtitle">Step-by-step cooking instructions</p>

            {instructions.map((instruction, i) => (
              <div key={i} className="create-item-row">
                <div className="create-step-number">{i + 1}</div>
                <textarea
                  className="create-textarea"
                  placeholder={`Step ${i + 1}`}
                  value={instruction}
                  onChange={(e) => updateInstruction(i, e.target.value)}
                  rows={2}
                />
                {instructions.length > 1 && (
                  <button type="button" className="create-item-remove" onClick={() => removeInstruction(i)}>
                    <X size={16} />
                  </button>
                )}
              </div>
            ))}
            <button type="button" className="create-add-btn" onClick={addInstruction}>
              <Plus size={16} />
              Add Instruction
            </button>
          </div>

          {/* Actions */}
          <div className="create-actions">
            <button type="button" className="create-actions__cancel" onClick={() => navigate('/dashboard')}>
              Cancel
            </button>
            <button type="submit" className="create-actions__submit">
              <Save size={16} />
              Publish Recipe
            </button>
          </div>

        </form>
      </main>
    </div>
  );
}

export default CreateRecipe;