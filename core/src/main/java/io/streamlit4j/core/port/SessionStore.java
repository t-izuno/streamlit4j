package io.streamlit4j.core.port;

import io.streamlit4j.core.domain.Session;
import java.util.Optional;

public interface SessionStore {

    Session create();

    Optional<Session> find(String id);

    void remove(String id);

    int activeCount();
}
