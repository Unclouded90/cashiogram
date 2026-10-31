import { Component, input } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-transaction-row',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './transaction-row.html',
  styleUrl: './transaction-row.scss',
})
export class TransactionRow {
  type = input.required<'INCOME' | 'EXPENSE'>();
  date = input.required<string>();
  category = input.required<string>();
  description = input<string | null>(null);
  amount = input.required<number>();
}
