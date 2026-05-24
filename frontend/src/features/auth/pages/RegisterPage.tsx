import { zodResolver } from '@hookform/resolvers/zod'
import { CheckCircle2, Loader2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { routes } from '@/app/router/routes'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useRegister } from '@/features/auth/hooks/useRegister'
import {
  registerSchema,
  type RegisterFormValues,
} from '@/features/auth/schemas/auth-schemas'
import { useAuthStore } from '@/features/auth/store/useAuthStore'
import { getErrorMessage } from '@/lib/errors/get-error-message'

export function RegisterPage() {
  const navigate = useNavigate()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const register = useRegister()
  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
    },
  })
  const password = form.watch('password')
  const hasMinimumPassword = password.length >= 8

  async function onSubmit(values: RegisterFormValues) {
    try {
      await register.mutateAsync(values)
      toast.success('Conta criada com sucesso.')
      navigate(routes.dashboard, { replace: true })
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
            <CardTitle>Criar conta</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-5" onSubmit={form.handleSubmit(onSubmit)}>
            <div className="space-y-2">
              <Label htmlFor="register-name">Nome</Label>
              <Input
                autoComplete="name"
                id="register-name"
                placeholder="Pedro Silva"
                {...form.register('name')}
              />
              {form.formState.errors.name ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.name.message}
                </p>
              ) : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="register-email">Email</Label>
              <Input
                autoComplete="email"
                id="register-email"
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
              <Label htmlFor="register-password">Senha</Label>
              <Input
                autoComplete="new-password"
                id="register-password"
                placeholder="Minimo de 8 caracteres"
                type="password"
                {...form.register('password')}
              />
              {form.formState.errors.password ? (
                <p className="text-sm text-destructive">
                  {form.formState.errors.password.message}
                </p>
              ) : null}
              <p className="flex items-center gap-2 text-xs text-muted-foreground">
                <CheckCircle2
                  className={
                    hasMinimumPassword
                      ? 'h-4 w-4 text-emerald-300'
                      : 'h-4 w-4 text-muted-foreground'
                  }
                />
                Pelo menos 8 caracteres
              </p>
            </div>
            {form.formState.errors.root ? (
              <p className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                {form.formState.errors.root.message}
              </p>
            ) : null}
            <Button className="w-full" disabled={register.isPending} type="submit">
              {register.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : null}
              Criar conta
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Ja tem conta?{' '}
              <Link className="text-foreground underline" to={routes.login}>
                Entrar
              </Link>
            </p>
            </form>
          </CardContent>
        </Card>
      </section>

      <section className="hidden border-l border-border/70 bg-secondary/30 p-10 lg:flex lg:items-end">
        <div className="max-w-xl space-y-4">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-cyan-300">
            Workspace financeiro
          </p>
          <h1 className="text-4xl font-semibold tracking-normal">
            Uma experiencia de demo clara para backend, notas e stocks.
          </h1>
        </div>
      </section>
    </main>
  )
}
