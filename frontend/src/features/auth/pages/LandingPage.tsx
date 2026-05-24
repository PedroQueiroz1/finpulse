import { Link, Navigate } from 'react-router-dom'
import { Activity, ArrowRight, BarChart3, FileText } from 'lucide-react'

import { routes } from '@/app/router/routes'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { env } from '@/lib/env/env'
import { useAuthStore } from '@/features/auth/store/useAuthStore'

const highlights = [
  {
    title: 'Notas',
    description: 'Organize decisoes, ideias e tarefas em um espaco simples.',
    icon: FileText,
  },
  {
    title: 'Mercado',
    description: 'Consulte tickers e acompanhe provider, cache e timestamp.',
    icon: BarChart3,
  },
  {
    title: 'Diagnostico',
    description: 'Rastreie chamadas com correlation IDs visiveis no app.',
    icon: Activity,
  },
]

export function LandingPage() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)

  if (isAuthenticated) {
    return <Navigate to={routes.dashboard} replace />
  }

  return (
    <main className="min-h-screen bg-background text-foreground">
      <section className="mx-auto flex min-h-screen w-full max-w-6xl flex-col justify-center gap-10 px-6 py-10">
        <div className="grid gap-8 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
          <div className="space-y-6">
            <Badge className="w-fit border-emerald-500/30 bg-emerald-500/10 text-emerald-200">
              {env.appName}
            </Badge>
            <div className="space-y-4">
              <h1 className="max-w-3xl text-4xl font-semibold tracking-normal text-foreground sm:text-5xl">
                FinPulse
              </h1>
              <p className="max-w-2xl text-base leading-7 text-muted-foreground sm:text-lg">
                Um painel financeiro e de produtividade para acompanhar notas,
                consultas de ativos e rastreabilidade das chamadas do backend.
              </p>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row">
              <Button asChild size="lg">
                <Link to={routes.register}>
                  Criar conta
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
              <Button asChild size="lg" variant="outline">
                <Link to={routes.login}>Entrar</Link>
              </Button>
            </div>
          </div>

          <Card className="border-border/70 bg-card/80 shadow-2xl shadow-black/20">
            <CardHeader>
              <CardTitle className="text-xl">Resumo da plataforma</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-4">
              {highlights.map((item) => (
                <div
                  className="flex gap-4 rounded-lg border border-border/70 bg-secondary/40 p-4"
                  key={item.title}
                >
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-300">
                    <item.icon className="h-5 w-5" />
                  </div>
                  <div className="space-y-1">
                    <h2 className="text-sm font-medium text-foreground">
                      {item.title}
                    </h2>
                    <p className="text-sm leading-6 text-muted-foreground">
                      {item.description}
                    </p>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </section>
    </main>
  )
}
