import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { StarRating } from './StarRating';

describe('StarRating', () => {
  it('renders the configured number of star buttons', () => {
    render(<StarRating args={{ label: 'Quality', max: 4 }} value={0} onChange={() => undefined} />);

    expect(screen.getByText('Quality')).toBeInTheDocument();
    expect(screen.getAllByRole('button')).toHaveLength(4);
  });

  it('marks filled stars based on the current value', () => {
    render(<StarRating args={{ label: 'Rating', max: 5 }} value={3} onChange={() => undefined} />);

    const buttons = screen.getAllByRole('button');
    expect(buttons[0]).toHaveAttribute('aria-pressed', 'true');
    expect(buttons[2]).toHaveAttribute('aria-pressed', 'true');
    expect(buttons[3]).toHaveAttribute('aria-pressed', 'false');
  });

  it('clicking a higher star emits the new rating', () => {
    const onChange = vi.fn();
    render(<StarRating args={{ label: 'Rating' }} value={1} onChange={onChange} />);

    fireEvent.click(screen.getByRole('button', { name: '4 stars' }));
    expect(onChange).toHaveBeenCalledWith(4);
  });

  it('clicking the current rating resets to zero', () => {
    const onChange = vi.fn();
    render(<StarRating args={{ label: 'Rating' }} value={3} onChange={onChange} />);

    fireEvent.click(screen.getByRole('button', { name: '3 stars' }));
    expect(onChange).toHaveBeenCalledWith(0);
  });

  it('falls back to defaults when args are missing or invalid', () => {
    render(
      <StarRating
        args={{ max: 'bogus' as unknown as number }}
        value={null}
        onChange={() => undefined}
      />,
    );

    expect(screen.getByText('Rating')).toBeInTheDocument();
    expect(screen.getAllByRole('button')).toHaveLength(5);
  });

  it('clamps absurdly large max to 10', () => {
    render(<StarRating args={{ max: 999 }} value={0} onChange={() => undefined} />);
    expect(screen.getAllByRole('button')).toHaveLength(10);
  });
});
