import { create } from 'zustand'

export type ThemeMode = 'light' | 'dark' | 'system'

type UiState = {
  theme: ThemeMode
  setTheme: (theme: ThemeMode) => void
}

const THEME_STORAGE_KEY = 'finpulse.theme'

function readInitialTheme(): ThemeMode {
  if (typeof window === 'undefined') {
    return 'dark'
  }

  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY)

  if (
    storedTheme === 'light' ||
    storedTheme === 'dark' ||
    storedTheme === 'system'
  ) {
    return storedTheme
  }

  return 'dark'
}

export function resolveTheme(theme: ThemeMode) {
  if (theme !== 'system') {
    return theme
  }

  if (
    typeof window !== 'undefined' &&
    window.matchMedia('(prefers-color-scheme: light)').matches
  ) {
    return 'light'
  }

  return 'dark'
}

export const useUiStore = create<UiState>((set) => ({
  theme: readInitialTheme(),
  setTheme: (theme) => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(THEME_STORAGE_KEY, theme)
    }

    set({ theme })
  },
}))
