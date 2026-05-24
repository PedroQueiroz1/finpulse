export const routes = {
  home: '/',
  login: '/login',
  register: '/register',
  dashboard: '/dashboard',
  notes: '/notes',
  noteDetail: (id: string) => `/notes/${id}`,
  stocks: '/stocks',
  settings: '/settings',
} as const
