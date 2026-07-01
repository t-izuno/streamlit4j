import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { Markdown, stabilizePartialMarkdown } from './Markdown';

describe('Markdown', () => {
  it('closes an unfinished fenced code block before rendering', () => {
    const body = stabilizePartialMarkdown('```java\nSystem.out.println("hi");');

    expect(body).toBe('```java\nSystem.out.println("hi");\n```');
    render(<Markdown body={'```java\nSystem.out.println("hi");'} />);

    expect(screen.getByText('System.out.println("hi");')).toBeInTheDocument();
    expect(document.querySelector('pre code')).not.toBeNull();
  });

  it('keeps a partially streamed table as a table layout', () => {
    const body = stabilizePartialMarkdown('| Name | Value |\n|');

    expect(body).toBe('| Name | Value |\n| --- | --- |');
    render(<Markdown body={'| Name | Value |\n|'} />);

    expect(screen.getByRole('table')).toHaveTextContent('Name');
  });

  it('keeps dangling list and math fragments bounded', () => {
    const body = stabilizePartialMarkdown('- item\n- \n\n$$\na = b');

    expect(body).toBe('- item\n- \u00a0\n\n$$\na = b\n$$');
  });
});
