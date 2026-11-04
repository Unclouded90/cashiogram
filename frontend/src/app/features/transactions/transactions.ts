import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Category } from '../categories/category.model';
import { CategoryService } from '../categories/category.service';
import { Card } from '../../shared/card/card';
import { Transaction } from './transaction.model';
import { TransactionRow } from './transaction-row/transaction-row';
import { TransactionService } from './transaction.service';
import { BalanceService } from '../balance/balance.service';

@Component({
  selector: 'app-transactions',
  imports: [Card, RouterLink, TransactionRow],
  templateUrl: './transactions.html',
  styleUrl: './transactions.scss',
})
export class Transactions implements OnInit {
  private readonly transactionService = inject(TransactionService);
  private readonly categoryService = inject(CategoryService);
  private readonly balanceService = inject(BalanceService);

  protected readonly transactions = signal<Transaction[]>([]);
  protected readonly categories = signal<Category[]>([]);

  protected readonly categoryNames = computed(() => {
    return new Map(this.categories().map((category) => [category.id, category.name]));
  });

  ngOnInit(): void {
    this.transactionService.getTransactions().subscribe({
      next: (transactions: Transaction[]) => {
        this.transactions.set(transactions);
      },
    });

    this.categoryService.getCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories.set(categories);
      },
    });
  }

  protected createTransaction(
    amountInput: HTMLInputElement,
    typeInput: HTMLSelectElement,
    categoryInput: HTMLSelectElement,
    dateInput: HTMLInputElement,
    descriptionInput: HTMLInputElement,
  ): void {
    this.transactionService
      .createTransaction({
        amount: Number(amountInput.value),
        transactionType: typeInput.value as 'INCOME' | 'EXPENSE',
        categoryId: categoryInput.value,
        date: dateInput.value,
        description: descriptionInput.value || null,
      })
      .subscribe({
        next: (transaction: Transaction) => {
          this.transactions.update((transactions) => [transaction, ...transactions]);

          this.balanceService.getBalance().subscribe();

          amountInput.value = '';
          categoryInput.value = '';
          dateInput.value = '';
          descriptionInput.value = '';
        },
      });
  }
}
