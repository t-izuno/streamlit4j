import { expect, test } from '@playwright/test';

test('validates packaged Fake LLM artifact smoke surface', async ({ page, request }) => {
  const indexResponse = await request.get('/');
  expect(indexResponse.status()).toBe(200);
  expect(indexResponse.headers()['content-type']).toContain('text/html');

  const indexHtml = await indexResponse.text();
  const assetPath = indexHtml.match(/\/assets\/index-[^"]+\.js/)?.[0];
  expect(assetPath).toBeTruthy();
  const assetResponse = await request.get(assetPath ?? '');
  expect(assetResponse.status()).toBe(200);
  expect(assetResponse.headers()['content-type']).toContain('javascript');

  const eventResponses: number[] = [];
  page.on('response', (response) => {
    if (response.url().endsWith('/events')) {
      eventResponses.push(response.status());
    }
  });

  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Fake LLM chat' })).toBeVisible();
  await expect(page.getByLabel('Ask Fake LLM')).toBeVisible();
  await expect.poll(() => eventResponses).toContain(200);
});

for (const transport of ['sse', 'websocket'] as const) {
  test(`exercises the Fake LLM chat acceptance flow over ${transport}`, async ({ page }) => {
    const sentMessages: unknown[] = [];
    if (transport === 'websocket') {
      page.on('websocket', (ws) => {
        ws.on('framesent', (event) => {
          try {
            sentMessages.push(JSON.parse(event.payload));
          } catch {
            sentMessages.push(event.payload);
          }
        });
      });
    } else {
      page.on('request', (request) => {
        if (request.method() === 'POST' && request.url().endsWith('/events')) {
          const body = request.postData();
          if (body) {
            sentMessages.push(JSON.parse(body));
          }
        }
      });
    }

    await page.goto(transport === 'websocket' ? '/?transport=websocket' : '/');

    await expect(page.getByRole('heading', { name: 'Fake LLM chat' })).toBeVisible();
    await expect(
      page.getByText('Ask a question to generate a deterministic response.'),
    ).toBeVisible();

    await page.getByLabel('Ask Fake LLM').fill('Explain streaming markdown');
    await page.getByLabel('Ask Fake LLM').press('Enter');

    await expect(page.getByLabel('user message')).toContainText('Explain streaming markdown');
    await expect(page.getByLabel('assistant message')).toContainText(
      'Answer for Explain streaming markdown (retry 0)',
    );
    await expect(page.getByRole('status', { name: 'streamed response' })).toContainText(
      'System.out.println("Explain streaming markdown");',
    );
    await expect(page.getByText('Fake retrieval')).toBeVisible();
    await expect(page.locator('.tool-result')).toContainText('Explain streaming markdown');
    await expect(page.getByText('success')).toBeVisible();

    await page.getByRole('button', { name: 'Stop' }).click();
    await expect(page.getByLabel('assistant message')).toContainText('generation stopped');
    await expect(page.getByText('cancelled')).toBeVisible();

    await page.getByRole('button', { name: 'Retry' }).click();
    await expect(page.getByLabel('assistant message')).toContainText(
      'Answer for Explain streaming markdown (retry 1)',
    );
    await expect(page.getByText('success')).toBeVisible();

    await page.getByLabel('Edit prompt').fill('Use edited prompt');
    await expect(page.getByLabel('Edit prompt')).toHaveValue('Use edited prompt');
    await page.getByRole('button', { name: 'Regenerate' }).click();
    expect(sentMessages).toContainEqual(
      expect.objectContaining({
        type: 'widget_event',
        value: { action: 'edit_regenerate', value: 'Use edited prompt' },
      }),
    );
    await expect(page.getByLabel('user message')).toContainText('Use edited prompt');
    await expect(page.getByLabel('assistant message')).toContainText(
      'Answer for Use edited prompt (retry 0)',
    );
    await expect(page.locator('.tool-result')).toContainText('Use edited prompt');
  });
}
