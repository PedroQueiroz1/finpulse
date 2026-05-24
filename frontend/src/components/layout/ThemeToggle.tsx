import { Monitor, Moon, Sun } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useUiStore, type ThemeMode } from '@/lib/ui/useUiStore'

const themeOptions: Array<{
  label: string
  value: ThemeMode
  icon: typeof Sun
}> = [
  { label: 'Claro', value: 'light', icon: Sun },
  { label: 'Escuro', value: 'dark', icon: Moon },
  { label: 'Sistema', value: 'system', icon: Monitor },
]

export function ThemeToggle() {
  const theme = useUiStore((state) => state.theme)
  const setTheme = useUiStore((state) => state.setTheme)
  const ActiveIcon =
    themeOptions.find((option) => option.value === theme)?.icon ?? Moon

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button aria-label="Alterar tema" size="icon" variant="ghost">
          <ActiveIcon className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        {themeOptions.map((option) => (
          <DropdownMenuItem
            key={option.value}
            onClick={() => setTheme(option.value)}
          >
            <option.icon className="mr-2 h-4 w-4" />
            {option.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
