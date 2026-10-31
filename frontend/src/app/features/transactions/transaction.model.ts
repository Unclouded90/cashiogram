export interface TransactionSplit {
  id: string;
  subcategoryId: string;
  amount: number;
}

export interface Transaction {
  id: string;
  amount: number;
  transactionType: 'INCOME' | 'EXPENSE';
  categoryId: string;
  date: string;
  description: string | null;
  splits: TransactionSplit[];
  createdAt: string;
}

export interface CreateTransaction {
  amount: number;
  transactionType: 'INCOME' | 'EXPENSE';
  categoryId: string;
  date: string;
  description: string | null;
}

export interface UpdateTransaction {
  amount?: number;
  transactionType?: 'INCOME' | 'EXPENSE';
  categoryId?: string;
  date?: string;
  description?: string | null;
}
