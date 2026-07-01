import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

export function Markdown({ body }: { body: string }) {
  const stableBody = stabilizePartialMarkdown(body);
  return (
    <div className="markdown">
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{stableBody}</ReactMarkdown>
    </div>
  );
}

export function stabilizePartialMarkdown(body: string): string {
  return closeUnfinishedBlockMath(
    fillDanglingListItems(completePartialTableSeparator(closeUnfinishedFence(body))),
  );
}

function closeUnfinishedFence(body: string): string {
  const fenceLines = body.match(/^(?:```+|~~~+)/gm) ?? [];
  if (fenceLines.length % 2 === 0) {
    return body;
  }
  const marker = fenceLines[fenceLines.length - 1].startsWith('~') ? '~~~' : '```';
  return `${body}\n${marker}`;
}

function completePartialTableSeparator(body: string): string {
  const lines = body.split('\n');
  if (lines.length < 2) {
    return body;
  }

  const lastIndex = lines.length - 1;
  const header = lines[lastIndex - 1].trim();
  const separator = lines[lastIndex].trim();
  if (!isTableRow(header) || !/^\|[\s:-]*$/.test(separator)) {
    return body;
  }

  lines[lastIndex] = buildTableSeparator(header);
  return lines.join('\n');
}

function isTableRow(line: string): boolean {
  return line.startsWith('|') && line.endsWith('|') && line.split('|').length > 2;
}

function buildTableSeparator(header: string): string {
  const cells = header
    .slice(1, -1)
    .split('|')
    .map(() => '---');
  return `| ${cells.join(' | ')} |`;
}

function fillDanglingListItems(body: string): string {
  return body
    .split('\n')
    .map((line) => {
      if (/^\s*(?:[-*+]|\d+[.)])\s*$/.test(line)) {
        return `${line}\u00a0`;
      }
      return line;
    })
    .join('\n');
}

function closeUnfinishedBlockMath(body: string): string {
  const blockMathDelimiters = body.match(/\$\$/g) ?? [];
  if (blockMathDelimiters.length % 2 === 0) {
    return body;
  }
  return `${body}\n$$`;
}
