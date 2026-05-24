import { z } from 'zod'

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Informe seu email.')
    .email('Informe um email valido.'),
  password: z.string().min(1, 'Informe sua senha.'),
})

export const registerSchema = z.object({
  name: z.string().min(2, 'Informe seu nome.'),
  email: z
    .string()
    .min(1, 'Informe seu email.')
    .email('Informe um email valido.'),
  password: z.string().min(8, 'Use pelo menos 8 caracteres.'),
})

export type LoginFormValues = z.infer<typeof loginSchema>
export type RegisterFormValues = z.infer<typeof registerSchema>
