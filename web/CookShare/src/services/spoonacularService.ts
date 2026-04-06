const API_KEY = import.meta.env.VITE_SPOONACULAR_API_KEY;
const BASE_URL = 'https://api.spoonacular.com';

// ── Cache config ──────────────────────────────────────────────────────────────
// Results are cached for 1 hour — saves API calls during development

const CACHE_TTL_MS = 60 * 60 * 1000; // 1 hour

interface CacheEntry {
  data: SpoonacularRecipe[];
  timestamp: number;
}

const getCache = (key: string): SpoonacularRecipe[] | null => {
  try {
    const raw = localStorage.getItem(`spoonacular_cache_${key}`);
    if (!raw) return null;
    const entry: CacheEntry = JSON.parse(raw);
    if (Date.now() - entry.timestamp > CACHE_TTL_MS) {
      localStorage.removeItem(`spoonacular_cache_${key}`);
      return null;
    }
    return entry.data;
  } catch {
    return null;
  }
};

const setCache = (key: string, data: SpoonacularRecipe[]): void => {
  try {
    const entry: CacheEntry = { data, timestamp: Date.now() };
    localStorage.setItem(`spoonacular_cache_${key}`, JSON.stringify(entry));
  } catch {
    // localStorage full — skip caching silently
  }
};

// ── Types ─────────────────────────────────────────────────────────────────────

export interface SpoonacularRecipe {
  id: number;
  title: string;
  image: string;
  readyInMinutes: number;
  preparationMinutes: number;
  cookingMinutes: number;
  servings: number;
  summary: string;
  dishTypes: string[];
  diets: string[];
  spoonacularScore: number;
  aggregateLikes: number;
  extendedIngredients: { original: string }[];
  analyzedInstructions: {
    steps: { number: number; step: string }[];
  }[];
  creditsText: string;
  sourceUrl: string;
}

// ── Helpers ───────────────────────────────────────────────────────────────────

export const stripHtml = (html: string): string =>
  html
    .replace(/<[^>]*>/g, '')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&nbsp;/g, ' ')
    .trim();

export const scoreToRating = (score: number): number =>
  Math.round((score / 20) * 10) / 10;

export const getDifficulty = (minutes: number): 'Easy' | 'Medium' | 'Hard' => {
  if (minutes <= 20) return 'Easy';
  if (minutes <= 45) return 'Medium';
  return 'Hard';
};

const FULL_INFO_PARAMS = [
  'addRecipeInformation=true',
  'fillIngredients=true',
  'addRecipeInstructions=true',
  'instructionsRequired=true',
].join('&');

// ── Service ───────────────────────────────────────────────────────────────────

export const spoonacularService = {

  // Get random recipes — cached under key "random"
  getRandomRecipes: async (number: number = 6): Promise<SpoonacularRecipe[]> => {
    const cacheKey = `random_${number}`;
    const cached = getCache(cacheKey);
    if (cached) return cached;

    const res = await fetch(
      `${BASE_URL}/recipes/random?number=${number}&${FULL_INFO_PARAMS}&apiKey=${API_KEY}`
    );
    if (!res.ok) throw new Error(`API error ${res.status}`);
    const data = await res.json();
    setCache(cacheKey, data.recipes);
    return data.recipes;
  },

  // Search recipes — cached under the query string
  searchRecipes: async (query: string, number: number = 9): Promise<SpoonacularRecipe[]> => {
    const cacheKey = `search_${query}_${number}`;
    const cached = getCache(cacheKey);
    if (cached) return cached;

    const res = await fetch(
      `${BASE_URL}/recipes/complexSearch?query=${encodeURIComponent(query)}&number=${number}&${FULL_INFO_PARAMS}&apiKey=${API_KEY}`
    );
    if (!res.ok) throw new Error(`API error ${res.status}`);
    const data = await res.json();
    setCache(cacheKey, data.results);
    return data.results;
  },

  // Get recipes by category — cached under the category name
  getRecipesByCategory: async (category: string, number: number = 9): Promise<SpoonacularRecipe[]> => {
    const cacheKey = `category_${category}_${number}`;
    const cached = getCache(cacheKey);
    if (cached) return cached;

    const res = await fetch(
      `${BASE_URL}/recipes/complexSearch?query=${encodeURIComponent(category)}&number=${number}&${FULL_INFO_PARAMS}&apiKey=${API_KEY}`
    );
    if (!res.ok) throw new Error(`API error ${res.status}`);
    const data = await res.json();
    setCache(cacheKey, data.results);
    return data.results;
  },

  // Clear all cached recipe data manually (useful for testing)
  clearCache: (): void => {
    Object.keys(localStorage)
      .filter((k) => k.startsWith('spoonacular_cache_'))
      .forEach((k) => localStorage.removeItem(k));
  },
};