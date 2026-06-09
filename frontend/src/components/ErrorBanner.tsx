import type { JSX } from 'react';

export type AppErrorKind = 'script' | 'connection';

export interface AppError {
  id: string;
  kind: AppErrorKind;
  title: string;
  message: string;
  stackTrace?: string;
}

interface Props {
  errors: AppError[];
  onDismiss: (id: string) => void;
}

export function ErrorBanner({ errors, onDismiss }: Props): JSX.Element | null {
  if (errors.length === 0) return null;
  return (
    <div className="error-banner" role="alert" aria-live="assertive">
      {errors.map((err) => (
        <div
          key={err.id}
          className={`error-banner__item error-banner__item--${err.kind}`}
          data-kind={err.kind}
        >
          <button
            type="button"
            className="error-banner__close"
            aria-label="Dismiss error"
            onClick={() => onDismiss(err.id)}
          >
            ×
          </button>
          <div className="error-banner__title">{err.title}</div>
          <div className="error-banner__message">{err.message}</div>
          {err.stackTrace && (
            <details className="error-banner__details">
              <summary>Stack trace</summary>
              <pre className="error-banner__stack">{err.stackTrace}</pre>
            </details>
          )}
        </div>
      ))}
    </div>
  );
}
