import { http, HttpResponse } from 'msw'

const notes = [
  {
    id: 'note-1',
    title: 'Plano de estudos',
    content: 'Linha um\nLinha dois',
    tags: ['java', 'frontend'],
    color: '#10b981',
    pinned: false,
    archived: false,
    createdAt: '2026-05-20T10:00:00',
    updatedAt: '2026-05-21T10:00:00',
  },
]

const groups = [
  {
    id: 'group-1',
    name: 'Estudos',
    noteCount: 1,
    count: 1,
    color: '#10b981',
  },
]

export const notesHandlers = [
  http.get('*/api/notes', () => HttpResponse.json(notes)),
  http.get('*/api/notes/search', ({ request }) => {
    const query = new URL(request.url).searchParams.get('q')?.toLowerCase() ?? ''
    const filteredNotes = notes.filter(
      (note) =>
        note.title.toLowerCase().includes(query) ||
        note.content.toLowerCase().includes(query),
    )

    return HttpResponse.json(filteredNotes)
  }),
  http.get('*/api/notes/groups', () => HttpResponse.json(groups)),
  http.get('*/api/notes/:id', ({ params }) => {
    const note = notes.find((item) => item.id === params.id)

    if (!note) {
      return HttpResponse.json(
        { status: 404, error: 'Not Found', message: 'Nota nao encontrada.' },
        { status: 404 },
      )
    }

    return HttpResponse.json(note)
  }),
  http.post('*/api/notes', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>

    return HttpResponse.json(
      {
        id: 'note-created',
        createdAt: '2026-05-22T10:00:00',
        updatedAt: '2026-05-22T10:00:00',
        ...body,
      },
      { status: 201 },
    )
  }),
  http.put('*/api/notes/:id', async ({ params, request }) => {
    const body = (await request.json()) as Record<string, unknown>

    return HttpResponse.json({
      id: params.id,
      updatedAt: '2026-05-22T12:00:00',
      ...body,
    })
  }),
  http.delete('*/api/notes/:id', () => new HttpResponse(null, { status: 204 })),
]
