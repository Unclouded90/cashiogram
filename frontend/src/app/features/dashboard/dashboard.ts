import { Component } from '@angular/core';
import { Balance } from '../balance/balance';
import { Transactions } from '../transactions/transactions';
import { Card } from '../../shared/card/card';
import { RouterLink } from '@angular/router';

@Component({
  imports: [Balance, Transactions, Card, RouterLink],
  selector: 'app-dashboard',
  styleUrl: './dashboard.scss',
  templateUrl: './dashboard.html',
})
export class Dashboard {}
