const THEME_STORAGE_KEY = 'banking-app-theme';

export const DEFAULT_THEME = 'new';

function normalizeTheme(theme) {
  if (theme === 'classic' || theme === 'new') {
    return theme;
  }

  return DEFAULT_THEME;
}

export function readStoredTheme() {
  if (typeof window === 'undefined') {
    return DEFAULT_THEME;
  }

  const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
  return normalizeTheme(stored);
}

export function writeStoredTheme(theme) {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(THEME_STORAGE_KEY, normalizeTheme(theme));
}
