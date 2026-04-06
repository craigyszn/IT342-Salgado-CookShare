export type Difficulty = 'Easy' | 'Medium' | 'Hard';

export interface Comment {
  id: number;
  author: string;
  text: string;
  date: string;
}

export interface Recipe {
  id: number;
  title: string;
  description: string;
  tags: string[];
  rating: number;
  reviewCount: number;
  prepTime: string;
  cookTime: string;
  difficulty: Difficulty;
  author: string;
  servings: number;
  imageUrl: string;
  ingredients: string[];
  instructions: string[];
  postedDate: string;
  comments: Comment[];
}