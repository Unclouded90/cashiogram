import { TestBed } from '@angular/core/testing';

import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.removeItem('cashiogram-theme');
    document.documentElement.removeAttribute('data-theme');

    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    localStorage.removeItem('cashiogram-theme');
    document.documentElement.removeAttribute('data-theme');
  });

  it('should default to light theme', () => {
    const service = TestBed.inject(ThemeService);

    expect(service.theme()).toBe('light');
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
  });

  it('should toggle from light to dark', () => {
    const service = TestBed.inject(ThemeService);

    service.toggleTheme();

    expect(service.theme()).toBe('dark');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(localStorage.getItem('cashiogram-theme')).toBe('dark');
  });

  it('should toggle back to light', () => {
    const service = TestBed.inject(ThemeService);

    service.toggleTheme();
    service.toggleTheme();

    expect(service.theme()).toBe('light');
    expect(localStorage.getItem('cashiogram-theme')).toBe('light');
  });

  it('should restore the saved theme', () => {
    localStorage.setItem('cashiogram-theme', 'dark');

    const service = TestBed.inject(ThemeService);

    expect(service.theme()).toBe('dark');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
  });
});
