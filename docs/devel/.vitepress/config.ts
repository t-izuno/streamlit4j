import { defineConfig } from "vitepress";

// CJK 対応 MiniSearch トークナイザー。Intl.Segmenter で単語境界を切る。
function cjkTokenize(text: string): string[] {
  const segmenter = new Intl.Segmenter(undefined, { granularity: "word" });
  return Array.from(segmenter.segment(text))
    .map((s) => s.segment)
    .filter((s) => s.trim().length > 0);
}

export default defineConfig({
  title: "streamlit4j 設計文書",
  description: "streamlit4j の設計 / 仕様 / ADR / タスク履歴を集約した設計文書サイト",
  lang: "ja",
  lastUpdated: true,
  cleanUrls: true,
  srcExclude: ["**/README.md"],
  ignoreDeadLinks: [
    /^https?:\/\/localhost(:\d+)?/,
    /^https?:\/\/127\.0\.0\.1/,
    // 内部文書から公開サイトへの参照は本サイトのスコープ外
    /^(\.\.\/)?public\//,
    /^(\.\.\/)?\.\.\/public\//,
  ],
  themeConfig: {
    siteTitle: "streamlit4j 設計文書",
    nav: [
      { text: "要件", link: "/requirements" },
      { text: "仕様", link: "/specification" },
      { text: "設計", link: "/design" },
      { text: "ADR", link: "/adr/" },
      { text: "タスク", link: "/tasks/task" },
      { text: "公開サイト", link: "http://localhost:5173/" },
      {
        text: "GitHub",
        link: "https://github.com/t-izuno/streamlit4j",
      },
    ],
    sidebar: [
      {
        text: "プロジェクト基盤",
        items: [
          { text: "要件（requirements）", link: "/requirements" },
          { text: "仕様（specification）", link: "/specification" },
          { text: "設計（design）", link: "/design" },
        ],
      },
      {
        text: "運用",
        items: [
          { text: "公開手順（publishing）", link: "/publishing" },
          { text: "セキュリティースキャン（security-scan）", link: "/security-scan" },
        ],
      },
      {
        text: "アーキテクチャー判断（ADR）",
        collapsed: false,
        items: [
          { text: "目次", link: "/adr/" },
          { text: "ADR-0002 JSON プロトコル", link: "/adr/0002-json-over-messagepack" },
          { text: "ADR-0004 GraalVM 延期", link: "/adr/0004-graalvm-deferred" },
          { text: "ADR-0005 明示的ページ登録", link: "/adr/0005-explicit-page-registration" },
          { text: "ADR-0006 MIT ライセンス", link: "/adr/0006-mit-license" },
          { text: "ADR-0007 iframe コンポーネント不採用", link: "/adr/0007-no-iframe-components" },
          { text: "ADR-0008 React フロントエンド", link: "/adr/0008-react-frontend" },
          { text: "ADR-0009 フロント Vite 採用", link: "/adr/0009-vite-for-frontend" },
          { text: "ADR-0010 docs サイト VitePress", link: "/adr/0010-vitepress-for-docs" },
          { text: "ADR-0011 Jetty 組み込みサーバー", link: "/adr/0011-jetty-embedded-server" },
        ],
      },
      {
        text: "タスク管理",
        items: [{ text: "全タスク履歴", link: "/tasks/task" }],
      },
    ],
    socialLinks: [
      {
        icon: "github",
        link: "https://github.com/t-izuno/streamlit4j",
      },
    ],
    search: {
      provider: "local",
      options: {
        miniSearch: {
          options: {
            tokenize: cjkTokenize,
          },
          searchOptions: {
            combineWith: "AND",
            tokenize: cjkTokenize,
          },
        },
      },
    },
    footer: {
      message:
        "一般公開対象外の設計文書サイト。利用者向けデプロイ（GitHub Pages 等）は行いません。",
      copyright: "© streamlit4j contributors",
    },
    docFooter: {
      prev: "前のページ",
      next: "次のページ",
    },
    outline: {
      label: "このページの内容",
    },
    lastUpdated: {
      text: "最終更新",
    },
    darkModeSwitchLabel: "外観",
    sidebarMenuLabel: "メニュー",
    returnToTopLabel: "ページトップへ",
  },
});
