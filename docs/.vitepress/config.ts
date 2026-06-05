import { defineConfig } from "vitepress";

export default defineConfig({
  title: "streamlit4j",
  description:
    "Idiomatic, open-source Java framework for interactive data apps on the JVM — inspired by Streamlit.",
  lang: "en-US",
  lastUpdated: true,
  cleanUrls: true,
  // タスクのソースは docs/tasks/ にあるが、Markdown ドキュメントとして
  // サイトへ含めると常に変動するため除外する
  srcExclude: ["tasks/**", "**/README.md"],
  ignoreDeadLinks: [/^https?:\/\/localhost(:\d+)?/, /^https?:\/\/127\.0\.0\.1/],
  themeConfig: {
    siteTitle: "streamlit4j",
    nav: [
      { text: "Guide", link: "/guide/getting-started" },
      { text: "Reference", link: "/reference/overview" },
      { text: "Design", link: "/design" },
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
            { text: "Spring Boot Integration", link: "/guide/spring-boot" },
          ],
        },
      ],
      "/reference/": [
        {
          text: "Reference",
          items: [{ text: "Overview", link: "/reference/overview" }],
        },
      ],
      "/": [
        {
          text: "Specification",
          items: [
            { text: "Requirements", link: "/requirements" },
            { text: "Specification", link: "/specification" },
            { text: "Design", link: "/design" },
          ],
        },
        {
          text: "Architecture Decisions",
          collapsed: true,
          items: [
            { text: "Index", link: "/adr/" },
            { text: "ADR-0002 JSON protocol", link: "/adr/0002-json-over-messagepack" },
            { text: "ADR-0004 GraalVM deferred", link: "/adr/0004-graalvm-deferred" },
            {
              text: "ADR-0005 Explicit page registration",
              link: "/adr/0005-explicit-page-registration",
            },
            { text: "ADR-0006 MIT license", link: "/adr/0006-mit-license" },
            { text: "ADR-0007 No iframe components", link: "/adr/0007-no-iframe-components" },
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
