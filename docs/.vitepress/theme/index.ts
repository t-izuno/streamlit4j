// @ts-expect-error VitePress resolves CSS side-effect imports at build time;
// TS does not need a type declaration for this asset.
import "./brand.css";
import DefaultTheme from "vitepress/theme";

export default DefaultTheme;
