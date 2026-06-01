package io.streamlit4j.core.domain;

public record Page(String name, String path, Runnable body) {}
