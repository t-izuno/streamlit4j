package io.streamlit4j.core.domain;

/**
 * Declaration of a single page in a multi-page app.
 *
 * @param name display label shown in the page navigator
 * @param path URL-style path identifier (used to look up the active page)
 * @param body script body invoked when this page is active
 */
public record Page(String name, String path, Runnable body) {}
