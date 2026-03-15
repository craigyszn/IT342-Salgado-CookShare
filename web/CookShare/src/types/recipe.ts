export type Difficulty = 'Easy' | 'Medium' | 'Hard';
 
export interface Recipe {
  id: number;
  title: string;
  description: string;
  tags: string[];
  rating: number;
  reviewCount: number;
  cookTime: string;
  difficulty: Difficulty;
  author: string;
  servings: number;
  imageUrl: string;
}