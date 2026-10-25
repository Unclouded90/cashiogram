import { Component } from '@angular/core';
import { TransactionRow } from './transaction-row/transaction-row';
import { Card } from '../../shared/card/card';

@Component({
  imports: [Card, TransactionRow],
  selector: 'app-transactions',
  styleUrl: './transactions.scss',
  templateUrl: './transactions.html',
})
export class Transactions {
  protected readonly transactions = [
    {
      id: '1',
      type: 'INCOME' as const,
      date: '2026-10-25',
      category: 'Salary',
      amount: 1750,
    },
    {
      id: '2',
      type: 'EXPENSE' as const,
      date: '2026-10-24',
      category: 'Groceries',
      amount: 68.42,
    },
    {
      id: '3',
      type: 'EXPENSE' as const,
      date: '2026-10-23',
      category: 'Fuel',
      amount: 24.9,
    },
  ];
}
