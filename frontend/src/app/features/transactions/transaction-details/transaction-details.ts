import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { Card } from '../../../shared/card/card';
import { SettingsPage } from '../../../shared/settings-page/settings-page';
import { Category } from '../../categories/category.model';
import { CategoryService } from '../../categories/category.service';
import { Subcategory } from '../../subcategories/subcategory.model';
import { SubcategoryService } from '../../subcategories/subcategory.service';
import { Transaction } from '../transaction.model';
import { TransactionService } from '../transaction.service';
import { BalanceService } from '../../balance/balance.service';

@Component({
  selector: 'app-transaction-details',
  imports: [Card, CurrencyPipe, SettingsPage],
  templateUrl: './transaction-details.html',
  styleUrl: './transaction-details.scss',
})
export class TransactionDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly transactionService = inject(TransactionService);
  private readonly categoryService = inject(CategoryService);
  private readonly subcategoryService = inject(SubcategoryService);
  private readonly balanceService = inject(BalanceService);

  private roundCurrency(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }

  protected readonly transaction = signal<Transaction | null>(null);
  protected readonly category = signal<Category | null>(null);
  protected readonly categories = signal<Category[]>([]);
  protected readonly subcategories = signal<Subcategory[]>([]);

  protected readonly subcategoryNames = computed(() => {
    return new Map(this.subcategories().map((subcategory) => [subcategory.id, subcategory.name]));
  });
  protected readonly editingTransaction = signal(false);

  protected readonly addingSplit = signal(false);

  protected readonly editingSplitId = signal<string | null>(null);

  ngOnInit(): void {
    const transactionId = this.route.snapshot.paramMap.get('transactionId');

    if (!transactionId) {
      return;
    }

    this.categoryService.getCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories.set(categories);
      },
    });

    this.transactionService.getTransaction(transactionId).subscribe({
      next: (transaction: Transaction) => {
        this.transaction.set(transaction);

        this.categoryService.getCategory(transaction.categoryId).subscribe({
          next: (category: Category) => {
            this.category.set(category);
          },
        });

        this.subcategoryService.getSubcategoriesByCategory(transaction.categoryId).subscribe({
          next: (subcategories: Subcategory[]) => {
            this.subcategories.set(subcategories);
          },
        });
      },
    });
  }

  protected editTransaction(): void {
    this.editingTransaction.set(true);
  }

  protected cancelTransactionEdit(): void {
    this.editingTransaction.set(false);
  }

  protected updateTransaction(
    amountInput: HTMLInputElement,
    typeInput: HTMLSelectElement,
    categoryInput: HTMLSelectElement,
    dateInput: HTMLInputElement,
    descriptionInput: HTMLInputElement,
  ): void {
    const transaction = this.transaction();

    if (!transaction) {
      return;
    }

    const amount = Number(amountInput.value);
    const categoryId = categoryInput.value;

    if (amount < this.allocatedAmount()) {
      window.alert(
        `Transaction amount cannot be lower than the allocated split amount (${this.allocatedAmount().toFixed(2)} €).`,
      );
      return;
    }

    if (categoryId !== transaction.categoryId && transaction.splits.length > 0) {
      window.alert(
        'The category cannot be changed while this transaction has splits. Delete the splits first.',
      );
      return;
    }

    this.transactionService
      .updateTransaction(transaction.id, {
        amount,
        transactionType: typeInput.value as 'INCOME' | 'EXPENSE',
        categoryId,
        date: dateInput.value,
        description: descriptionInput.value,
      })
      .subscribe({
        next: (updatedTransaction: Transaction) => {
          this.transaction.set(updatedTransaction);

          this.categoryService.getCategory(updatedTransaction.categoryId).subscribe({
            next: (category: Category) => {
              this.category.set(category);
            },
          });

          this.subcategoryService
            .getSubcategoriesByCategory(updatedTransaction.categoryId)
            .subscribe({
              next: (subcategories) => {
                this.subcategories.set(subcategories);
              },
            });

          this.editingTransaction.set(false);
          this.balanceService.getBalance().subscribe();
        },
      });
  }

  protected deleteTransaction(): void {
    const transactionId = this.transaction()?.id;

    if (!transactionId) {
      return;
    }

    const confirmed = window.confirm('Are you sure you want to delete this transaction?');

    if (!confirmed) {
      return;
    }

    this.transactionService.deleteTransaction(transactionId).subscribe({
      next: () => {
        this.balanceService.getBalance().subscribe();
        this.router.navigate(['/']);
      },
    });
  }

  protected cancelSplitForm(): void {
    this.addingSplit.set(false);
  }

  protected createSplit(subcategoryInput: HTMLSelectElement, amountInput: HTMLInputElement): void {
    const transaction = this.transaction();

    if (!transaction) {
      return;
    }

    const amount = Number(amountInput.value);

    if (!subcategoryInput.value) {
      window.alert('Please select a subcategory.');
      return;
    }

    if (amount <= 0) {
      window.alert('Split amount must be greater than zero.');
      return;
    }

    if (amount > this.unallocatedAmount()) {
      window.alert(
        `Split amount cannot exceed the remaining ${this.unallocatedAmount().toFixed(2)} €.`,
      );
      return;
    }

    this.transactionService
      .createTransactionSplit(transaction.id, {
        subcategoryId: subcategoryInput.value,
        amount,
      })
      .subscribe({
        next: (split) => {
          this.transaction.update((currentTransaction) => {
            if (!currentTransaction) {
              return currentTransaction;
            }

            return {
              ...currentTransaction,
              splits: [...currentTransaction.splits, split],
            };
          });

          this.addingSplit.set(false);
        },
      });
  }

  protected readonly availableSubcategories = computed(() => {
    const transaction = this.transaction();

    if (!transaction) {
      return [];
    }

    const usedSubcategoryIds = new Set(transaction.splits.map((split) => split.subcategoryId));

    return this.subcategories().filter((subcategory) => !usedSubcategoryIds.has(subcategory.id));
  });

  protected readonly allocatedAmount = computed(() => {
    const transaction = this.transaction();

    if (!transaction) {
      return 0;
    }

    const total = transaction.splits.reduce((sum, split) => sum + split.amount, 0);

    return this.roundCurrency(total);
  });

  protected readonly unallocatedAmount = computed(() => {
    const transaction = this.transaction();

    if (!transaction) {
      return 0;
    }

    return this.roundCurrency(transaction.amount - this.allocatedAmount());
  });

  protected editSplit(splitId: string): void {
    if (this.addingSplit()) {
      const confirmed = window.confirm(
        'You have an unsaved new split. If you continue, it will be lost. Do you want to edit this split anyway?',
      );

      if (!confirmed) {
        return;
      }

      this.addingSplit.set(false);
    }

    this.editingSplitId.set(splitId);
  }

  protected cancelSplitEdit(): void {
    this.editingSplitId.set(null);
  }

  protected showSplitForm(): void {
    if (this.editingSplitId()) {
      const confirmed = window.confirm(
        'You have unsaved changes. If you continue, they will be lost. Do you want to add a new split anyway?',
      );

      if (!confirmed) {
        return;
      }

      this.editingSplitId.set(null);
    }

    this.addingSplit.set(true);
  }

  protected updateSplit(
    splitId: string,
    currentAmount: number,
    amountInput: HTMLInputElement,
  ): void {
    const transaction = this.transaction();

    if (!transaction) {
      return;
    }

    const amount = Number(amountInput.value);
    const maximumAmount = this.unallocatedAmount() + currentAmount;

    if (amount <= 0) {
      window.alert('Split amount must be greater than zero.');
      return;
    }

    if (amount > maximumAmount) {
      window.alert(`Split amount cannot exceed ${maximumAmount.toFixed(2)} €.`);
      return;
    }

    this.transactionService
      .updateTransactionSplit(transaction.id, splitId, {
        amount,
      })
      .subscribe({
        next: (updatedSplit) => {
          this.transaction.update((currentTransaction) => {
            if (!currentTransaction) {
              return currentTransaction;
            }

            return {
              ...currentTransaction,
              splits: currentTransaction.splits.map((split) =>
                split.id === updatedSplit.id ? updatedSplit : split,
              ),
            };
          });

          this.editingSplitId.set(null);
        },
      });
  }

  protected deleteSplit(splitId: string): void {
    const transaction = this.transaction();

    if (!transaction) {
      return;
    }

    const confirmed = window.confirm('Are you sure you want to delete this split?');

    if (!confirmed) {
      return;
    }

    this.transactionService.deleteTransactionSplit(transaction.id, splitId).subscribe({
      next: () => {
        this.transaction.update((currentTransaction) => {
          if (!currentTransaction) {
            return currentTransaction;
          }

          return {
            ...currentTransaction,
            splits: currentTransaction.splits.filter((split) => split.id !== splitId),
          };
        });

        this.editingSplitId.set(null);
      },
    });
  }
}
