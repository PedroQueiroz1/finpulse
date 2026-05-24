import { expect, test } from '@playwright/test'

test('carrega landing page', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'FinPulse' })).toBeVisible()
})
