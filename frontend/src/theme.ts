export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'streamlit4j.theme';

export function readStoredTheme(): Theme {
  if (typeof window === 'undefined') return 'light';
  const value = window.localStorage?.getItem(STORAGE_KEY);
  return value === 'dark' ? 'dark' : 'light';
}

export function applyTheme(theme: Theme): void {
  if (typeof document === 'undefined') return;
  document.documentElement.dataset.theme = theme;
  window.localStorage?.setItem(STORAGE_KEY, theme);
}
