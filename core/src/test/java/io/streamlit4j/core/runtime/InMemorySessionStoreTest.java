package io.streamlit4j.core.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.SessionLifecycleListener;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemorySessionStoreTest {

    @Test
    void createPutsSessionInStoreAndFiresCreatedEvent() {
        InMemorySessionStore store = new InMemorySessionStore();
        List<SessionLifecycleListener.Event> events = new ArrayList<>();
        store.addListener((event, session) -> events.add(event));

        Session session = store.create();

        assertThat(store.find(session.id())).contains(session);
        assertThat(events).containsExactly(SessionLifecycleListener.Event.CREATED);
    }

    @Test
    void removeFiresDestroyedEvent() {
        InMemorySessionStore store = new InMemorySessionStore();
        List<SessionLifecycleListener.Event> events = new ArrayList<>();

        Session session = store.create();
        store.addListener((event, s) -> events.add(event));
        store.remove(session.id());

        assertThat(store.find(session.id())).isEmpty();
        assertThat(events).containsExactly(SessionLifecycleListener.Event.DESTROYED);
    }

    @Test
    void evictIdleRemovesStaleSessionsAndFiresExpired() throws InterruptedException {
        InMemorySessionStore store = new InMemorySessionStore(Duration.ofMillis(10));
        List<SessionLifecycleListener.Event> events = new ArrayList<>();
        Session session = store.create();
        store.addListener((event, s) -> events.add(event));

        Thread.sleep(50);
        int evicted = store.evictIdle();

        assertThat(evicted).isEqualTo(1);
        assertThat(store.find(session.id())).isEmpty();
        assertThat(events).containsExactly(SessionLifecycleListener.Event.EXPIRED);
    }
}
