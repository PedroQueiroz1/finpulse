import { lazy, Suspense, type ReactNode } from 'react'
import { createBrowserRouter, Navigate } from 'react-router-dom'

import { ProtectedRoute } from '@/app/router/ProtectedRoute'
import { RouteFallback } from '@/app/router/RouteFallback'
import { routes } from '@/app/router/routes'

const AppShell = lazy(() =>
  import('@/components/layout/AppShell').then((module) => ({
    default: module.AppShell,
  })),
)
const NotFoundPage = lazy(() =>
  import('@/app/router/NotFoundPage').then((module) => ({
    default: module.NotFoundPage,
  })),
)
const LandingPage = lazy(() =>
  import('@/features/auth/pages/LandingPage').then((module) => ({
    default: module.LandingPage,
  })),
)
const LoginPage = lazy(() =>
  import('@/features/auth/pages/LoginPage').then((module) => ({
    default: module.LoginPage,
  })),
)
const RegisterPage = lazy(() =>
  import('@/features/auth/pages/RegisterPage').then((module) => ({
    default: module.RegisterPage,
  })),
)
const DashboardPage = lazy(() =>
  import('@/features/dashboard/pages/DashboardPage').then((module) => ({
    default: module.DashboardPage,
  })),
)
const NotesPage = lazy(() =>
  import('@/features/notes/pages/NotesPage').then((module) => ({
    default: module.NotesPage,
  })),
)
const StocksPage = lazy(() =>
  import('@/features/stocks/pages/StocksPage').then((module) => ({
    default: module.StocksPage,
  })),
)
const SettingsPage = lazy(() =>
  import('@/features/settings/pages/SettingsPage').then((module) => ({
    default: module.SettingsPage,
  })),
)

function withSuspense(element: ReactNode) {
  return <Suspense fallback={<RouteFallback />}>{element}</Suspense>
}

export const router = createBrowserRouter([
  {
    path: routes.home,
    element: withSuspense(<LandingPage />),
  },
  {
    path: routes.login,
    element: withSuspense(<LoginPage />),
  },
  {
    path: routes.register,
    element: withSuspense(<RegisterPage />),
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: withSuspense(<AppShell />),
        children: [
          {
            path: routes.dashboard,
            element: withSuspense(<DashboardPage />),
          },
          {
            path: routes.notes,
            element: withSuspense(<NotesPage />),
          },
          {
            path: routes.stocks,
            element: withSuspense(<StocksPage />),
          },
          {
            path: routes.settings,
            element: withSuspense(<SettingsPage />),
          },
        ],
      },
    ],
  },
  {
    path: '/app',
    element: <Navigate to={routes.dashboard} replace />,
  },
  {
    path: '*',
    element: withSuspense(<NotFoundPage />),
  },
])
