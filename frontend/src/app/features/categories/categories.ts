import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Card } from '../../shared/card/card';
import { SettingsPage } from '../../shared/settings-page/settings-page';
import { Category } from './category.model';
import { CategoryService } from './category.service';

@Component({
  selector: 'app-categories',
  imports: [Card, RouterLink, SettingsPage],
  templateUrl: './categories.html',
  styleUrl: './categories.scss',
})
export class Categories implements OnInit {
  private readonly categoryService = inject(CategoryService);

  protected readonly categories = signal<Category[]>([]);
  protected readonly editingCategoryId = signal<string | null>(null);

  ngOnInit(): void {
    this.categoryService.getCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories.set(categories);
      },
    });
  }

  protected createCategory(nameInput: HTMLInputElement, descriptionInput: HTMLInputElement): void {
    this.categoryService
      .createCategory({
        name: nameInput.value,
        description: descriptionInput.value || null,
      })
      .subscribe({
        next: (category: Category) => {
          this.categories.update((categories) => [...categories, category]);

          nameInput.value = '';
          descriptionInput.value = '';
        },
      });
  }

  protected editCategory(categoryId: string): void {
    this.editingCategoryId.set(categoryId);
  }

  protected updateCategory(categoryId: string, name: string, description: string): void {
    this.categoryService
      .updateCategory(categoryId, {
        name,
        description: description || null,
      })
      .subscribe({
        next: (updatedCategory: Category) => {
          this.categories.update((categories) =>
            categories.map((category) =>
              category.id === updatedCategory.id ? updatedCategory : category,
            ),
          );

          this.editingCategoryId.set(null);
        },
      });
  }

  protected deleteCategory(categoryId: string): void {
    this.categoryService.deleteCategory(categoryId).subscribe({
      next: () => {
        this.categories.update((categories) =>
          categories.filter((category) => category.id !== categoryId),
        );
      },
    });
  }
}
