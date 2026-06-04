export const THEME_STORAGE_KEY = 'banking-app-theme';
export const DEFAULT_THEME = 'new';

export function normalizeTheme(theme) {
  return theme === 'classic' ? 'classic' : DEFAULT_THEME;
}

export function readStoredTheme() {
  if (typeof window === 'undefined') {
    return DEFAULT_THEME;
  }

  return normalizeTheme(window.localStorage.getItem(THEME_STORAGE_KEY));
}

export function writeStoredTheme(theme) {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(THEME_STORAGE_KEY, normalizeTheme(theme));
}
