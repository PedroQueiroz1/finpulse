import type { ReactNode } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { SkeletonCard } from '@/components/feedback/SkeletonCard'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useAuthStore } from '@/features/auth/store/useAuthStore'
import type { ApiError } from '@/lib/api/api-types'

type ProtectedRouteProps = {
  children?: ReactNode
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const location = useLocation()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const currentUserQuery = useCurrentUser()
  const errorStatus = (currentUserQuery.error as Partial<ApiError> | null)?.status

  if (!isAuthenticated || errorStatus === 401 || errorStatus === 403) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (currentUserQuery.isLoading) {
    return (
      <main className="min-h-screen bg-background p-6 text-foreground">
        <div className="mx-auto w-full max-w-3xl">
          <SkeletonCard rows={4} />
        </div>
      </main>
    )
  }

  return children ?? <Outlet />
}
