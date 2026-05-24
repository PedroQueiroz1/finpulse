import { useEffect, type PropsWithChildren } from 'react'

import { resolveTheme, useUiStore } from '@/lib/ui/useUiStore'

export function ThemeProvider({ children }: PropsWithChildren) {
  const theme = useUiStore((state) => state.theme)

  useEffect(() => {
    function applyTheme() {
      const resolvedTheme = resolveTheme(theme)
      const root = document.documentElement

      root.classList.toggle('light', resolvedTheme === 'light')
      root.style.colorScheme = resolvedTheme
    }

    applyTheme()

    if (theme !== 'system') {
      return undefined
    }

    const mediaQuery = window.matchMedia('(prefers-color-scheme: light)')
    mediaQuery.addEventListener('change', applyTheme)

    return () => mediaQuery.removeEventListener('change', applyTheme)
  }, [theme])

  return children
}
