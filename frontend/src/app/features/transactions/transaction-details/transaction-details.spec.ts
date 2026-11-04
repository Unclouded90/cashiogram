import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { BalanceService } from '../../balance/balance.service';
import { CategoryService } from '../../categories/category.service';
import { SubcategoryService } from '../../subcategories/subcategory.service';
import { Transaction } from '../transaction.model';
import { TransactionService } from '../transaction.service';
import { TransactionDetails } from './transaction-details';

describe('TransactionDetails', () => {
  let component: TransactionDetails;
  let fixture: ComponentFixture<TransactionDetails>;

  const category = {
    id: 'category-1',
    name: 'Groceries',
    description: 'General purchases',
    createdAt: '2026-11-04T17:00:00Z',
  };

  const otherCategory = {
    id: 'category-2',
    name: 'Transport',
    description: 'Transport expenses',
    createdAt: '2026-11-04T17:00:00Z',
  };

  const subcategories = [
    {
      id: 'subcategory-1',
      name: 'Food',
      description: 'Food purchases',
      categoryId: 'category-1',
      createdAt: '2026-11-04T17:30:00Z',
    },
    {
      id: 'subcategory-2',
      name: 'Cat Products',
      description: 'Cat purchases',
      categoryId: 'category-1',
      createdAt: '2026-11-04T17:30:00Z',
    },
  ];

  const transaction: Transaction = {
    id: 'transaction-1',
    amount: 100,
    transactionType: 'EXPENSE',
    categoryId: 'category-1',
    date: '2026-11-04',
    description: 'Groceries',
    splits: [
      {
        id: 'split-1',
        subcategoryId: 'subcategory-1',
        amount: 30,
      },
    ],
    createdAt: '2026-11-04T18:00:00Z',
  };

  const transactionServiceMock = {
    getTransaction: vi.fn(),
    updateTransaction: vi.fn(),
    deleteTransaction: vi.fn(),
    createTransactionSplit: vi.fn(),
    updateTransactionSplit: vi.fn(),
    deleteTransactionSplit: vi.fn(),
  };

  const categoryServiceMock = {
    getCategories: vi.fn(),
    getCategory: vi.fn(),
  };

  const subcategoryServiceMock = {
    getSubcategoriesByCategory: vi.fn(),
  };

  const balanceServiceMock = {
    getBalance: vi.fn(),
  };

  const routerMock = {
    navigate: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    categoryServiceMock.getCategories.mockReturnValue(of([category, otherCategory]));

    categoryServiceMock.getCategory.mockReturnValue(of(category));

    subcategoryServiceMock.getSubcategoriesByCategory.mockReturnValue(of(subcategories));

    balanceServiceMock.getBalance.mockReturnValue(
      of({
        initialBalance: 1000,
        totalIncome: 0,
        totalExpenses: 100,
        currentBalance: 900,
      }),
    );

    await TestBed.configureTestingModule({
      imports: [TransactionDetails],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({
                transactionId: 'transaction-1',
              }),
            },
          },
        },
        {
          provide: Router,
          useValue: routerMock,
        },
        {
          provide: TransactionService,
          useValue: transactionServiceMock,
        },
        {
          provide: CategoryService,
          useValue: categoryServiceMock,
        },
        {
          provide: SubcategoryService,
          useValue: subcategoryServiceMock,
        },
        {
          provide: BalanceService,
          useValue: balanceServiceMock,
        },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  async function createComponent(transactionValue: Transaction = transaction): Promise<void> {
    transactionServiceMock.getTransaction.mockReturnValue(of(transactionValue));

    fixture = TestBed.createComponent(TransactionDetails);
    component = fixture.componentInstance;

    fixture.detectChanges();

    await fixture.whenStable();

    fixture.detectChanges();
  }

  function findButton(text: string): HTMLButtonElement | undefined {
    const compiled = fixture.nativeElement as HTMLElement;

    return Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find((button) =>
      button.textContent?.trim().includes(text),
    );
  }

  it('should create', async () => {
    await createComponent();

    expect(component).toBeTruthy();
  });

  it('should load and display transaction details', async () => {
    await createComponent();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(transactionServiceMock.getTransaction).toHaveBeenCalledWith('transaction-1');

    expect(categoryServiceMock.getCategories).toHaveBeenCalled();

    expect(categoryServiceMock.getCategory).toHaveBeenCalledWith('category-1');

    expect(subcategoryServiceMock.getSubcategoriesByCategory).toHaveBeenCalledWith('category-1');

    expect(compiled.textContent).toContain('Groceries');
    expect(compiled.textContent).toContain('100');
    expect(compiled.textContent).toContain('EXPENSE');
    expect(compiled.textContent).toContain('2026-11-04');
    expect(compiled.textContent).toContain('Food');
  });

  it('should update a transaction and refresh the balance', async () => {
    await createComponent();

    const updatedTransaction: Transaction = {
      ...transaction,
      amount: 120,
      description: 'Updated groceries',
    };

    transactionServiceMock.updateTransaction.mockReturnValue(of(updatedTransaction));

    findButton('Edit')?.click();

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    const amountInput = compiled.querySelector('input[type="number"]') as HTMLInputElement;

    const descriptionInput = compiled.querySelector('input[type="text"]') as HTMLInputElement;

    const dateInput = compiled.querySelector('input[type="date"]') as HTMLInputElement;

    const selects = compiled.querySelectorAll('select');

    const typeInput = selects[0] as HTMLSelectElement;
    const categoryInput = selects[1] as HTMLSelectElement;

    amountInput.value = '120';
    typeInput.value = 'EXPENSE';
    categoryInput.value = 'category-1';
    dateInput.value = '2026-11-04';
    descriptionInput.value = 'Updated groceries';

    findButton('Save')?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(transactionServiceMock.updateTransaction).toHaveBeenCalledWith('transaction-1', {
      amount: 120,
      transactionType: 'EXPENSE',
      categoryId: 'category-1',
      date: '2026-11-04',
      description: 'Updated groceries',
    });

    expect(balanceServiceMock.getBalance).toHaveBeenCalled();

    expect(compiled.textContent).toContain('Updated groceries');
  });

  it('should not update amount below the allocated split amount', async () => {
    await createComponent();

    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

    findButton('Edit')?.click();

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    const amountInput = compiled.querySelector('input[type="number"]') as HTMLInputElement;

    amountInput.value = '20';

    findButton('Save')?.click();

    expect(alertSpy).toHaveBeenCalled();

    expect(transactionServiceMock.updateTransaction).not.toHaveBeenCalled();
  });

  it('should not change category while transaction has splits', async () => {
    await createComponent();

    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

    findButton('Edit')?.click();

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    const selects = compiled.querySelectorAll('select');

    const categoryInput = selects[1] as HTMLSelectElement;

    categoryInput.value = 'category-2';

    findButton('Save')?.click();

    expect(alertSpy).toHaveBeenCalled();

    expect(transactionServiceMock.updateTransaction).not.toHaveBeenCalled();
  });

  it('should delete a transaction, refresh the balance and navigate back', async () => {
    await createComponent();

    vi.spyOn(window, 'confirm').mockReturnValue(true);

    transactionServiceMock.deleteTransaction.mockReturnValue(of(undefined));

    findButton('Delete')?.click();

    await fixture.whenStable();

    expect(transactionServiceMock.deleteTransaction).toHaveBeenCalledWith('transaction-1');

    expect(balanceServiceMock.getBalance).toHaveBeenCalled();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should create a split', async () => {
    await createComponent();

    const createdSplit = {
      id: 'split-2',
      subcategoryId: 'subcategory-2',
      amount: 20,
    };

    transactionServiceMock.createTransactionSplit.mockReturnValue(of(createdSplit));

    const compiled = fixture.nativeElement as HTMLElement;

    const addSplitButton = compiled.querySelector('.add-split-card') as HTMLButtonElement;

    addSplitButton.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.split-form-card') as HTMLElement;

    const subcategoryInput = form.querySelector('select') as HTMLSelectElement;

    const amountInput = form.querySelector('input[type="number"]') as HTMLInputElement;

    subcategoryInput.value = 'subcategory-2';
    amountInput.value = '20';

    const saveButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.includes('Save') || button.textContent?.includes('Add'),
    );

    saveButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(transactionServiceMock.createTransactionSplit).toHaveBeenCalledWith('transaction-1', {
      subcategoryId: 'subcategory-2',
      amount: 20,
    });

    expect(compiled.textContent).toContain('Cat Products');
  });

  it('should not allow a split above the unallocated amount', async () => {
    await createComponent();

    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

    const compiled = fixture.nativeElement as HTMLElement;

    const addSplitButton = compiled.querySelector('.add-split-card') as HTMLButtonElement;

    addSplitButton.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.split-form-card') as HTMLElement;

    const subcategoryInput = form.querySelector('select') as HTMLSelectElement;

    const amountInput = form.querySelector('input[type="number"]') as HTMLInputElement;

    subcategoryInput.value = 'subcategory-2';
    amountInput.value = '80';

    const saveButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.includes('Save') || button.textContent?.includes('Add'),
    );

    saveButton?.click();

    expect(alertSpy).toHaveBeenCalled();

    expect(transactionServiceMock.createTransactionSplit).not.toHaveBeenCalled();
  });

  it('should update a split', async () => {
    await createComponent();

    const updatedSplit = {
      id: 'split-1',
      subcategoryId: 'subcategory-1',
      amount: 40,
    };

    transactionServiceMock.updateTransactionSplit.mockReturnValue(of(updatedSplit));

    const compiled = fixture.nativeElement as HTMLElement;

    const splitCard = compiled.querySelector('.split-card') as HTMLButtonElement;

    splitCard.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.split-form-card') as HTMLElement;

    const amountInput = form.querySelector('input[type="number"]') as HTMLInputElement;

    amountInput.value = '40';

    const saveButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Save',
    );

    saveButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(transactionServiceMock.updateTransactionSplit).toHaveBeenCalledWith(
      'transaction-1',
      'split-1',
      {
        amount: 40,
      },
    );

    expect(compiled.textContent).toContain('40');
  });

  it('should delete a split', async () => {
    await createComponent();

    vi.spyOn(window, 'confirm').mockReturnValue(true);

    transactionServiceMock.deleteTransactionSplit.mockReturnValue(of(undefined));

    const compiled = fixture.nativeElement as HTMLElement;

    const splitCard = compiled.querySelector('.split-card') as HTMLButtonElement;

    splitCard.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.split-form-card') as HTMLElement;

    const deleteButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Delete',
    );

    deleteButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(transactionServiceMock.deleteTransactionSplit).toHaveBeenCalledWith(
      'transaction-1',
      'split-1',
    );

    expect(compiled.textContent).not.toContain('Food');
  });

  it('should hide add split when transaction is fully allocated', async () => {
    const fullyAllocatedTransaction: Transaction = {
      ...transaction,
      splits: [
        {
          id: 'split-1',
          subcategoryId: 'subcategory-1',
          amount: 100,
        },
      ],
    };

    await createComponent(fullyAllocatedTransaction);

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.add-split-card')).toBeNull();
  });

  it('should not offer subcategories already used by another split', async () => {
    await createComponent();

    const compiled = fixture.nativeElement as HTMLElement;

    const addSplitButton = compiled.querySelector('.add-split-card') as HTMLButtonElement;

    addSplitButton.click();

    fixture.detectChanges();

    const select = compiled.querySelector('.split-form-card select') as HTMLSelectElement;

    const values = Array.from(select.options).map((option) => option.value);

    expect(values).not.toContain('subcategory-1');
    expect(values).toContain('subcategory-2');
  });
});
