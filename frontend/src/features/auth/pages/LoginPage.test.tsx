import { MemoryRouter } from 'react-router-dom'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { LoginPage } from '@/features/auth/pages/LoginPage'
import { renderWithClient } from '@/test/render'

describe('LoginPage', () => {
  it('renderiza campos e valida email/senha obrigatorios', async () => {
    const user = userEvent.setup()

    renderWithClient(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    )

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/senha/i)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /entrar/i }))

    expect(await screen.findByText(/informe seu email/i)).toBeInTheDocument()
    expect(screen.getByText(/informe sua senha/i)).toBeInTheDocument()
  })
})
