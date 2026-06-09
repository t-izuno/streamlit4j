import { defineConfig } from "vitepress";

// CJK（日本語 / 中国語 / 韓国語）対応の MiniSearch トークナイザー。
// Intl.Segmenter で単語単位に分割し、空白区切り言語と CJK の両方に対応する。
function cjkTokenize(text: string): string[] {
  const segmenter = new Intl.Segmenter(undefined, { granularity: "word" });
  return Array.from(segmenter.segment(text))
    .map((s) => s.segment)
    .filter((s) => s.trim().length > 0);
}

export default defineConfig({
  title: "streamlit4j",
  description:
    "Idiomatic, open-source Java framework for interactive data apps on the JVM — inspired by Streamlit.",
  lastUpdated: true,
  cleanUrls: true,
  srcExclude: ["**/README.md"],
  ignoreDeadLinks: [/^https?:\/\/localhost(:\d+)?/, /^https?:\/\/127\.0\.0\.1/],
  head: [
    ["link", { rel: "icon", type: "image/svg+xml", href: "/favicon.svg" }],
  ],
  themeConfig: {
    siteTitle: "streamlit4j",
    logo: {
      light: "/streamlit4j-logo.svg",
      dark: "/streamlit4j-logo-dark.svg",
      alt: "streamlit4j",
    },
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
  },
  locales: {
    root: {
      label: "日本語",
      lang: "ja",
      themeConfig: {
        nav: [
          { text: "ガイド", link: "/guide/getting-started" },
          { text: "リファレンス", link: "/reference/overview" },
          {
            text: "GitHub",
            link: "https://github.com/t-izuno/streamlit4j",
          },
        ],
        sidebar: {
          "/guide/": [
            {
              text: "ガイド",
              items: [
                { text: "はじめに", link: "/guide/getting-started" },
                { text: "インストール", link: "/guide/installation" },
                { text: "ソースから動かす", link: "/guide/run-from-source" },
                { text: "Spring Boot 統合", link: "/guide/spring-boot" },
                { text: "カスタムコンポーネント", link: "/guide/custom-components" },
              ],
            },
          ],
          "/reference/": [
            {
              text: "リファレンス",
              items: [
                { text: "概要", link: "/reference/overview" },
                { text: "テキスト", link: "/reference/text" },
                { text: "ステータスと通知", link: "/reference/status" },
                { text: "表形式データ", link: "/reference/data" },
                { text: "メディア", link: "/reference/media" },
                { text: "グラフ", link: "/reference/charts" },
                { text: "入力ウィジェット", link: "/reference/inputs" },
                { text: "ファイル", link: "/reference/files" },
                { text: "レイアウト", link: "/reference/layout" },
                { text: "フォーム", link: "/reference/forms" },
                { text: "キャッシュ", link: "/reference/cache" },
                { text: "マルチページ", link: "/reference/pages" },
                { text: "カスタムコンポーネント", link: "/reference/components" },
                { text: "制御フロー", link: "/reference/control" },
              ],
            },
          ],
        },
        footer: {
          message:
            "コミュニティー OSS。Snowflake, Inc. や Streamlit プロジェクトと提携 / 公認 / スポンサー関係はありません。",
          copyright: "MIT License で公開。",
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
        lightModeSwitchTitle: "ライトモードに切り替え",
        darkModeSwitchTitle: "ダークモードに切り替え",
        sidebarMenuLabel: "メニュー",
        returnToTopLabel: "ページトップへ",
        langMenuLabel: "言語を切り替える",
      },
    },
    en: {
      label: "English",
      lang: "en-US",
      link: "/en/",
      themeConfig: {
        nav: [
          { text: "Guide", link: "/en/guide/getting-started" },
          { text: "Reference", link: "/en/reference/overview" },
          {
            text: "GitHub",
            link: "https://github.com/t-izuno/streamlit4j",
          },
        ],
        sidebar: {
          "/en/guide/": [
            {
              text: "Guide",
              items: [
                { text: "Getting Started", link: "/en/guide/getting-started" },
                { text: "Installation", link: "/en/guide/installation" },
                { text: "Run from source", link: "/en/guide/run-from-source" },
                { text: "Spring Boot Integration", link: "/en/guide/spring-boot" },
                { text: "Custom Components", link: "/en/guide/custom-components" },
              ],
            },
          ],
          "/en/reference/": [
            {
              text: "Reference",
              items: [
                { text: "Overview", link: "/en/reference/overview" },
                { text: "Text & document flow", link: "/en/reference/text" },
                { text: "Status & notifications", link: "/en/reference/status" },
                { text: "Tabular data", link: "/en/reference/data" },
                { text: "Media", link: "/en/reference/media" },
                { text: "Charts", link: "/en/reference/charts" },
                { text: "Input widgets", link: "/en/reference/inputs" },
                { text: "Files", link: "/en/reference/files" },
                { text: "Layout", link: "/en/reference/layout" },
                { text: "Forms", link: "/en/reference/forms" },
                { text: "Cache", link: "/en/reference/cache" },
                { text: "Multi-page", link: "/en/reference/pages" },
                { text: "Custom components", link: "/en/reference/components" },
                { text: "Control flow", link: "/en/reference/control" },
              ],
            },
          ],
        },
        footer: {
          message:
            "Independent community OSS. Not affiliated with, endorsed by, or sponsored by Snowflake, Inc. or the Streamlit project.",
          copyright: "Released under the MIT License.",
        },
      },
    },
  },
});
