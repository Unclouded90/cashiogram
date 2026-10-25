import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionRow } from './transaction-row';

describe('TransactionRow', () => {
  let component: TransactionRow;
  let fixture: ComponentFixture<TransactionRow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionRow],
    }).compileComponents();

    fixture = TestBed.createComponent(TransactionRow);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('type', 'INCOME');
    fixture.componentRef.setInput('date', '2026-10-25');
    fixture.componentRef.setInput('category', 'Salary');
    fixture.componentRef.setInput('amount', 1350);

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
