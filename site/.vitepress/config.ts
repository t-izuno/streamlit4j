import { defineConfig } from "vitepress";

export default defineConfig({
  title: "streamlit4j",
  description:
    "Idiomatic, open-source Java framework for interactive data apps on the JVM — inspired by Streamlit.",
  lang: "en-US",
  lastUpdated: true,
  cleanUrls: true,
  // ../docs/ をコンテンツルートとして読む
  srcDir: "../docs",
  // tasks は変動が激しいためサイトには含めない。README.md も同様
  srcExclude: ["internal/tasks/**", "**/README.md"],
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
    nav: [
      { text: "Guide", link: "/guide/getting-started" },
      { text: "Reference", link: "/reference/overview" },
      { text: "Design", link: "/internal/design" },
      {
        text: "GitHub",
        link: "https://github.com/t-izuno/streamlit4j",
      },
    ],
    sidebar: {
      "/guide/": [
        {
          text: "Guide",
          items: [
            { text: "Getting Started", link: "/guide/getting-started" },
            { text: "Installation", link: "/guide/installation" },
            { text: "Run from source", link: "/guide/run-from-source" },
            { text: "Spring Boot Integration", link: "/guide/spring-boot" },
            { text: "Custom Components", link: "/guide/custom-components" },
          ],
        },
      ],
      "/reference/": [
        {
          text: "Reference",
          items: [
            { text: "Overview", link: "/reference/overview" },
            { text: "Text & document flow", link: "/reference/text" },
            { text: "Status & notifications", link: "/reference/status" },
            { text: "Tabular data", link: "/reference/data" },
            { text: "Media", link: "/reference/media" },
            { text: "Charts", link: "/reference/charts" },
            { text: "Input widgets", link: "/reference/inputs" },
            { text: "Files", link: "/reference/files" },
            { text: "Layout", link: "/reference/layout" },
            { text: "Forms", link: "/reference/forms" },
            { text: "Cache", link: "/reference/cache" },
            { text: "Multi-page", link: "/reference/pages" },
            { text: "Custom components", link: "/reference/components" },
            { text: "Control flow", link: "/reference/control" },
          ],
        },
      ],
      "/internal/": [
        {
          text: "Internal specs",
          items: [
            { text: "Requirements", link: "/internal/requirements" },
            { text: "Specification", link: "/internal/specification" },
            { text: "Design", link: "/internal/design" },
            { text: "Publishing", link: "/internal/publishing" },
            { text: "Security scan (OWASP)", link: "/internal/security-scan" },
          ],
        },
        {
          text: "Architecture Decisions",
          collapsed: true,
          items: [
            { text: "Index", link: "/internal/adr/" },
            { text: "ADR-0002 JSON protocol", link: "/internal/adr/0002-json-over-messagepack" },
            { text: "ADR-0004 GraalVM deferred", link: "/internal/adr/0004-graalvm-deferred" },
            {
              text: "ADR-0005 Explicit page registration",
              link: "/internal/adr/0005-explicit-page-registration",
            },
            { text: "ADR-0006 MIT license", link: "/internal/adr/0006-mit-license" },
            { text: "ADR-0007 No iframe components", link: "/internal/adr/0007-no-iframe-components" },
            { text: "ADR-0008 React frontend", link: "/internal/adr/0008-react-frontend" },
            { text: "ADR-0009 Vite for frontend", link: "/internal/adr/0009-vite-for-frontend" },
            { text: "ADR-0010 VitePress for docs", link: "/internal/adr/0010-vitepress-for-docs" },
            { text: "ADR-0011 Jetty embedded server", link: "/internal/adr/0011-jetty-embedded-server" },
          ],
        },
      ],
    },
    socialLinks: [
      {
        icon: "github",
        link: "https://github.com/t-izuno/streamlit4j",
      },
    ],
    footer: {
      message:
        "Independent community OSS. Not affiliated with, endorsed by, or sponsored by Snowflake, Inc. or the Streamlit project.",
      copyright: "Released under the MIT License.",
    },
    search: {
      provider: "local",
    },
  },
});
