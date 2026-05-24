import { NavLink } from 'react-router-dom'
import { Activity, Menu } from 'lucide-react'

import { navItems } from '@/components/layout/nav-items'
import { Button } from '@/components/ui/button'
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet'
import { cn } from '@/lib/utils'

export function MobileNav() {
  return (
    <Sheet>
      <SheetTrigger asChild>
        <Button aria-label="Abrir menu" className="lg:hidden" size="icon" variant="ghost">
          <Menu className="h-5 w-5" />
        </Button>
      </SheetTrigger>
      <SheetContent className="w-80 border-border/70 bg-background" side="left">
        <SheetHeader className="mb-6 text-left">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-300">
              <Activity className="h-5 w-5" />
            </div>
            <div>
              <SheetTitle>FinPulse</SheetTitle>
              <SheetDescription>Navegacao principal</SheetDescription>
            </div>
          </div>
        </SheetHeader>

        <nav className="space-y-1">
          {navItems.map((item) => (
            <SheetClose asChild key={item.to}>
              <NavLink
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-secondary/70 hover:text-foreground',
                    isActive && 'bg-secondary text-foreground',
                  )
                }
                to={item.to}
              >
                <item.icon className="h-4 w-4" />
                {item.label}
              </NavLink>
            </SheetClose>
          ))}
        </nav>
      </SheetContent>
    </Sheet>
  )
}
