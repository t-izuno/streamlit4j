import { spawn, spawnSync } from 'node:child_process';
import { dirname, resolve } from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';

const frontendDir = dirname(dirname(fileURLToPath(import.meta.url)));
const repoRoot = resolve(frontendDir, '..');
const mvnw = process.platform === 'win32' ? 'mvnw.cmd' : './mvnw';
const mavenRepoArgs = process.env.MAVEN_REPO_LOCAL
  ? [`-Dmaven.repo.local=${process.env.MAVEN_REPO_LOCAL}`]
  : [];

const compile = spawnSync(
  mvnw,
  [
    ...mavenRepoArgs,
    '-pl',
    'examples/embedded',
    '-am',
    '-DskipTests',
    'package',
    'install:install',
  ],
  {
    cwd: repoRoot,
    stdio: 'inherit',
  },
);

if (compile.status !== 0) {
  process.exit(compile.status ?? 1);
}

const server = spawn(
  mvnw,
  [
    ...mavenRepoArgs,
    '-pl',
    'examples/embedded',
    '-DskipTests',
    'exec:java',
    '-Dexec.mainClass=io.streamlit4j.examples.FakeLlmChatDemo',
    '-Dexec.args=8511',
  ],
  {
    cwd: repoRoot,
    stdio: 'inherit',
  },
);

server.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 0);
});
