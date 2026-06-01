module.exports = {
  forbidden: [
    {
      name: 'no-circular',
      severity: 'error',
      comment: 'Modules must not form circular dependencies.',
      from: {},
      to: { circular: true },
    },
    {
      name: 'no-orphans',
      severity: 'warn',
      comment: 'Modules should be reachable from an entry point.',
      from: {
        orphan: true,
        pathNot: [
          '\\.d\\.ts$',
          '(^|/)tsconfig\\.json$',
          '(^|/)\\.dependency-cruiser\\.cjs$',
          '(^|/)vite\\.config\\.ts$',
          '(^|/)vitest\\.config\\.ts$',
          '(^|/)vitest\\.setup\\.ts$',
          '(^|/)playwright\\.config\\.ts$',
        ],
      },
      to: {},
    },
    {
      name: 'components-do-not-cross-depend',
      severity: 'error',
      comment: 'UI components must be composed only by render.tsx, not directly by each other.',
      from: { path: '^src/components/' },
      to: { path: '^src/components/', pathNot: '$0' },
    },
    {
      name: 'components-no-upstream-deps',
      severity: 'error',
      comment: 'Components must not depend on the dispatcher or App.',
      from: { path: '^src/components/' },
      to: { path: '^src/(render|App)' },
    },
    {
      name: 'ws-no-ui-deps',
      severity: 'error',
      comment: 'WebSocket client must remain UI-free for reuse and testability.',
      from: { path: '^src/ws\\.' },
      to: { path: '^src/(components/|render|App)' },
    },
    {
      name: 'protocol-is-leaf',
      severity: 'error',
      comment: 'protocol.ts is the contract surface — it must not import from anywhere in src.',
      from: { path: '^src/protocol\\.' },
      to: { path: '^src/', pathNot: '^src/protocol\\.' },
    },
  ],
  options: {
    doNotFollow: { path: 'node_modules' },
    tsPreCompilationDeps: true,
    tsConfig: { fileName: 'tsconfig.json' },
    enhancedResolveOptions: {
      exportsFields: ['exports'],
      conditionNames: ['import', 'require', 'node', 'default', 'types'],
      mainFields: ['module', 'main', 'types', 'typings'],
    },
    reporterOptions: {
      text: { highlightFocused: true },
    },
  },
};
