export interface Subcategory {
  id: string;
  name: string;
  description: string | null;
  categoryId: string;
  createdAt: string;
}

export interface CreateSubcategory {
  name: string;
  description: string | null;
}

export interface UpdateSubcategory {
  name?: string;
  description?: string | null;
}
