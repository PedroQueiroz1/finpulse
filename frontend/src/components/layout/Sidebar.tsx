import { NavLink } from 'react-router-dom'
import { Activity } from 'lucide-react'

import { navItems } from '@/components/layout/nav-items'
import { cn } from '@/lib/utils'

export function Sidebar() {
  return (
    <aside className="fixed inset-y-0 left-0 z-40 hidden w-72 border-r border-border/70 bg-card/80 backdrop-blur lg:flex lg:flex-col">
      <div className="flex h-16 items-center gap-3 border-b border-border/70 px-6">
        <div className="flex h-9 w-9 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-300">
          <Activity className="h-5 w-5" />
        </div>
        <div>
          <p className="text-sm font-semibold">FinPulse</p>
          <p className="text-xs text-muted-foreground">Frontend dashboard</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map((item) => (
          <NavLink
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-secondary/70 hover:text-foreground',
                isActive && 'bg-secondary text-foreground',
              )
            }
            key={item.to}
            to={item.to}
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
