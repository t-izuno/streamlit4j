import { expect, test } from '@playwright/test';

test('renders the title', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: /streamlit4j/i })).toBeVisible();
});
