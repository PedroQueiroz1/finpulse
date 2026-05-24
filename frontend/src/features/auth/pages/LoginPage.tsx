import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { routes } from '@/app/router/routes'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useLogin } from '@/features/auth/hooks/useLogin'
import {
  loginSchema,
  type LoginFormValues,
} from '@/features/auth/schemas/auth-schemas'
import { useAuthStore } from '@/features/auth/store/useAuthStore'
import { getErrorMessage } from '@/lib/errors/get-error-message'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const login = useLogin()
  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })
  const from =
    (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ??
    routes.dashboard

  async function onSubmit(values: LoginFormValues) {
    try {
      await login.mutateAsync(values)
      toast.success('Login realizado com sucesso.')
      navigate(from, { replace: true })
    } catch (error) {
      form.setError('root', {
        message: getErrorMessage(error),
      })
    }
  }

  if (isAuthenticated) {
    return <Navigate to={routes.dashboard} replace />
  }

  return (
    <main className="grid min-h-screen bg-background text-foreground lg:grid-cols-[0.95fr_1.05fr]">
      <section className="flex items-center justify-center px-6 py-10">
        <Card className="w-full max-w-md border-border/70 bg-card/80">
          <CardHeader>
            <CardTitle>Acessar FinPulse</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-5" onSubmit={form.handleSubmit(onSubmit)}>
            <div className="space-y-2">
              <Label htmlFor="login-email">Email</Label>
              <Input
                autoComplete="email"
                id="login-email"
                placeholder="voce@email.com"
                type="email"
                {...form.register('email')}
              />
              {form.formState.errors.email ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.email.message}
                </p>
              ) : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="login-password">Senha</Label>
              <Input
                autoComplete="current-password"
                id="login-password"
                placeholder="Sua senha"
                type="password"
                {...form.register('password')}
              />
              {form.formState.errors.password ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.password.message}
                </p>
              ) : null}
            </div>
            {form.formState.errors.root ? (
              <p className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                {form.formState.errors.root.message}
              </p>
            ) : null}
            <Button className="w-full" disabled={login.isPending} type="submit">
              {login.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : null}
              Entrar
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Nao tem conta?{' '}
              <Link className="text-foreground underline" to={routes.register}>
                Criar conta
              </Link>
            </p>
            </form>
          </CardContent>
        </Card>
      </section>

      <section className="hidden border-l border-border/70 bg-secondary/30 p-10 lg:flex lg:items-end">
        <div className="max-w-xl space-y-4">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-emerald-300">
            FinPulse
          </p>
          <h1 className="text-4xl font-semibold tracking-normal">
            Controle notas, ativos e diagnostico em um unico painel.
          </h1>
        </div>
      </section>
    </main>
  )
}
