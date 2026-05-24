import { useNavigate } from 'react-router-dom'
import { LogOut, UserRound } from 'lucide-react'

import { routes } from '@/app/router/routes'
import { MobileNav } from '@/components/layout/MobileNav'
import { ThemeToggle } from '@/components/layout/ThemeToggle'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { useLogout } from '@/features/auth/hooks/useLogout'
import { useAuthStore } from '@/features/auth/store/useAuthStore'
import { getLastCorrelationId } from '@/lib/api/correlation-id'
import { env } from '@/lib/env/env'

export function Topbar() {
  const navigate = useNavigate()
  const logout = useLogout()
  const user = useAuthStore((state) => state.user)
  const lastCorrelationId = getLastCorrelationId()

  function handleLogout() {
    logout()
    navigate(routes.login, { replace: true })
  }

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between gap-4 border-b border-border/70 bg-background/90 px-4 backdrop-blur sm:px-6 lg:px-8">
      <div className="flex min-w-0 items-center gap-3">
        <MobileNav />
        <div className="min-w-0">
          <p className="truncate text-sm font-medium">{env.appName}</p>
          <p className="truncate text-xs text-muted-foreground">
            {lastCorrelationId ?? 'Sem correlation ID capturado'}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <div className="hidden items-center gap-2 rounded-md border border-border/70 px-2.5 py-1.5 text-sm text-muted-foreground md:flex">
          <UserRound className="h-4 w-4" />
          <span className="max-w-40 truncate">{user?.name ?? 'Usuario'}</span>
        </div>
        <Badge className="hidden border-cyan-500/30 bg-cyan-500/10 text-cyan-200 sm:inline-flex">
          {env.useDirectServices ? 'Direct services' : 'Gateway'}
        </Badge>
        <ThemeToggle />
        <Button aria-label="Sair" onClick={handleLogout} size="icon" variant="ghost">
          <LogOut className="h-4 w-4" />
        </Button>
      </div>
    </header>
  )
}
