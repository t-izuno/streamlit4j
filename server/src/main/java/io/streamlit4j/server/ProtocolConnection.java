package io.streamlit4j.server;

import io.streamlit4j.core.protocol.Envelope;

interface ProtocolConnection {

    void deliver(Envelope envelope);
}
