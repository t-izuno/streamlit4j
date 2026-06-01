package io.streamlit4j.examples;

import io.streamlit4j.core.St;

public final class Hello {

    private Hello() {}

    public static void run() {
        St.title("Hello, streamlit4j!");
        St.markdown("This is the **streamlit4j** PoC sample.");
        int year = St.slider("Year", 2018, 2026, 2025);
        St.metric("Selected year", year);
        if (St.button("Greet")) {
            St.toast("Hello from year " + year);
        }
    }
}
