import { BarChart3, FileText, LineChart, Settings } from 'lucide-react'

import { routes } from '@/app/router/routes'

export const navItems = [
  {
    label: 'Dashboard',
    to: routes.dashboard,
    icon: BarChart3,
  },
  {
    label: 'Notes',
    to: routes.notes,
    icon: FileText,
  },
  {
    label: 'Stocks',
    to: routes.stocks,
    icon: LineChart,
  },
  {
    label: 'Settings',
    to: routes.settings,
    icon: Settings,
  },
] as const
