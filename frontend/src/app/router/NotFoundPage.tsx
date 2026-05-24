import { Link } from 'react-router-dom'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { routes } from '@/app/router/routes'

export function NotFoundPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-background p-6">
      <Card className="w-full max-w-md border-border/70 bg-card/80 text-center">
        <CardHeader>
          <CardTitle>Pagina nao encontrada</CardTitle>
        </CardHeader>
        <CardContent className="space-y-5">
          <p className="text-sm text-muted-foreground">
            O endereco informado nao corresponde a uma rota do FinPulse.
          </p>
          <Button asChild>
            <Link to={routes.home}>Voltar ao inicio</Link>
          </Button>
        </CardContent>
      </Card>
    </main>
  )
}
