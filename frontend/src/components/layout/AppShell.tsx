import { Outlet, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'

import { Sidebar } from '@/components/layout/Sidebar'
import { Topbar } from '@/components/layout/Topbar'

export function AppShell() {
  const location = useLocation()

  return (
    <div className="min-h-screen bg-background text-foreground">
      <Sidebar />
      <div className="flex min-h-screen flex-col lg:pl-72">
        <Topbar />
        <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
          <motion.div
            animate={{ opacity: 1, y: 0 }}
            className="mx-auto w-full max-w-7xl"
            initial={{ opacity: 0, y: 8 }}
            key={location.pathname}
            transition={{ duration: 0.18, ease: 'easeOut' }}
          >
            <Outlet />
          </motion.div>
        </main>
      </div>
    </div>
  )
}
