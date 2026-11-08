import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { ThemeService } from '../../core/theme/theme.service';
import { Options } from './options';

describe('Options', () => {
  let component: Options;
  let fixture: ComponentFixture<Options>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Options],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Options);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle theme through the Options menu', () => {
    const service = TestBed.inject(ThemeService);

    const optionsButton = fixture.nativeElement.querySelector(
      '.options-button',
    ) as HTMLButtonElement;

    optionsButton.click();
    fixture.detectChanges();

    const themeButton = fixture.nativeElement.querySelector('.theme-button') as HTMLButtonElement;

    const initialTheme = service.theme();

    themeButton.click();
    fixture.detectChanges();

    const expectedTheme = initialTheme === 'light' ? 'dark' : 'light';

    expect(service.theme()).toBe(expectedTheme);
    expect(document.documentElement.getAttribute('data-theme')).toBe(expectedTheme);
    expect(localStorage.getItem('cashiogram-theme')).toBe(expectedTheme);
  });
});
