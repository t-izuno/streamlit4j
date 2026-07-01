package io.streamlit4j.core.api;

/**
 * One-shot action emitted by {@link St#chatControls()}.
 *
 * @param type
 *            requested chat operation
 * @param value
 *            optional edited prompt for {@link Type#EDIT_REGENERATE}
 */
public record ChatAction(Type type, String value) {

    /** Chat control action types. */
    public enum Type {
        /** Stop the in-flight generation. */
        STOP("stop"),
        /** Retry the latest assistant generation. */
        RETRY("retry"),
        /** Replace the latest user input and regenerate the following answer. */
        EDIT_REGENERATE("edit_regenerate");

        private final String wireName;

        Type(String wireName) {
            this.wireName = wireName;
        }

        String wireName() {
            return wireName;
        }

        static Type fromWireName(String wireName) {
            for (Type type : values()) {
                if (type.wireName.equals(wireName)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown chat action: " + wireName);
        }
    }
}
