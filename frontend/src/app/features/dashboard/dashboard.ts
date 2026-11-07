import { Component } from '@angular/core';
import { Balance } from '../balance/balance';
import { Transactions } from '../transactions/transactions';
import { Card } from '../../shared/card/card';
import { Options } from '../options/options';
import { RouterLink } from '@angular/router';

@Component({
  imports: [Balance, Transactions, Card, Options, RouterLink],
  selector: 'app-dashboard',
  styleUrl: './dashboard.scss',
  templateUrl: './dashboard.html',
})
export class Dashboard {}
