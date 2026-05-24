import { z } from 'zod'

export const noteFormSchema = z.object({
  title: z
    .string()
    .min(1, 'Informe um titulo.')
    .max(200, 'Use no maximo 200 caracteres.'),
  content: z.string().optional(),
  tagsInput: z.string().optional(),
  groupId: z.string().optional(),
  color: z.string().optional(),
})

export type NoteFormValues = z.infer<typeof noteFormSchema>
