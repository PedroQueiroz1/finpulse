import { Activity, Monitor, Moon, Server, Sun, UserRound } from 'lucide-react'

import { PageHeader } from '@/components/layout/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { env } from '@/lib/env/env'
import { useUiStore, type ThemeMode } from '@/lib/ui/useUiStore'

const diagnostics = [
  {
    label: 'API base',
    value: env.apiBaseUrl,
    icon: Server,
  },
  {
    label: 'Modo direto',
    value: env.useDirectServices ? 'Ativo' : 'Inativo',
    icon: Activity,
  },
  {
    label: 'Sessao',
    value: 'Token local',
    icon: UserRound,
  },
]

const themeOptions: Array<{
  label: string
  value: ThemeMode
  icon: typeof Sun
}> = [
  { label: 'Claro', value: 'light', icon: Sun },
  { label: 'Escuro', value: 'dark', icon: Moon },
  { label: 'Sistema', value: 'system', icon: Monitor },
]

export function SettingsPage() {
  const theme = useUiStore((state) => state.theme)
  const setTheme = useUiStore((state) => state.setTheme)

  return (
    <div className="space-y-6">
      <PageHeader
        description="Perfil, tema e diagnostico da aplicacao."
        eyebrow="Preferencias"
        title="Settings"
      />

      <div className="grid gap-4 lg:grid-cols-3">
        {diagnostics.map((item) => (
          <Card className="border-border/70 bg-card/80" key={item.label}>
            <CardHeader className="flex flex-row items-center justify-between gap-4 space-y-0">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {item.label}
              </CardTitle>
              <item.icon className="h-4 w-4 text-cyan-300" />
            </CardHeader>
            <CardContent>
              <p className="break-all text-sm font-medium">{item.value}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card className="border-border/70 bg-card/80">
        <CardHeader>
          <CardTitle>Tema</CardTitle>
          <p className="text-sm text-muted-foreground">
            Escolha a aparencia local da aplicacao.
          </p>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 sm:flex-row">
          {themeOptions.map((option) => (
            <Button
              className="justify-start"
              key={option.value}
              onClick={() => setTheme(option.value)}
              variant={theme === option.value ? 'secondary' : 'outline'}
            >
              <option.icon className="mr-2 h-4 w-4" />
              {option.label}
            </Button>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
