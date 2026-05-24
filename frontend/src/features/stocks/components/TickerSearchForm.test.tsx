import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { TickerSearchForm } from '@/features/stocks/components/TickerSearchForm'
import { renderWithClient } from '@/test/render'

describe('TickerSearchForm', () => {
  it('valida ticker antes de submeter', async () => {
    const onSubmit = vi.fn()
    const user = userEvent.setup()

    renderWithClient(
      <TickerSearchForm onClear={vi.fn()} onSubmit={onSubmit} symbol="" />,
    )

    await user.type(screen.getByLabelText(/ticker/i), '???')
    await user.click(screen.getByRole('button', { name: /buscar/i }))

    expect(
      screen.getByText(/use um ticker valido, como aapl/i),
    ).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('normaliza ticker para uppercase ao submeter', async () => {
    const onSubmit = vi.fn()
    const user = userEvent.setup()

    renderWithClient(
      <TickerSearchForm onClear={vi.fn()} onSubmit={onSubmit} symbol="" />,
    )

    await user.type(screen.getByLabelText(/ticker/i), 'aapl')
    await user.click(screen.getByRole('button', { name: /buscar/i }))

    expect(onSubmit).toHaveBeenCalledWith('AAPL')
  })
})
