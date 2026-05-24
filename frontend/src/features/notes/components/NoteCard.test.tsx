import { screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'

import { NoteCard } from '@/features/notes/components/NoteCard'
import { renderWithClient } from '@/test/render'

describe('NoteCard', () => {
  it('exibe conteudo preservando quebras de linha', () => {
    renderWithClient(
      <NoteCard
        note={{
          id: 'note-1',
          title: 'Nota com linhas',
          content: 'Linha um\nLinha dois',
          tags: ['java'],
        }}
        onDelete={vi.fn()}
        onEdit={vi.fn()}
      />,
    )

    const content = screen.getByText(
      (_, element) => element?.textContent === 'Linha um\nLinha dois',
    )

    expect(content).toHaveClass('whitespace-pre-line')
  })
})
