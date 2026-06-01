package io.streamlit4j.core.runtime;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.protocol.RenderNode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ScriptRunner implements Renderer, AutoCloseable {

    private static final int MAX_RERUNS_PER_REQUEST = 5;

    private final ExecutorService executor;

    public ScriptRunner() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public RenderNode render(Session session, Runnable entrypoint) {
        return session.withRerunLock(() -> renderUnderLock(session, entrypoint));
    }

    private RenderNode renderUnderLock(Session session, Runnable entrypoint) {
        try {
            RenderNode root = executor.submit(() -> {
                        RenderContext ctx = new RenderContext(session.state());
                        RenderContext.bind(ctx);
                        try {
                            int reruns = 0;
                            while (true) {
                                ctx.reset();
                                try {
                                    entrypoint.run();
                                    break;
                                } catch (ControlSignals.RerunRequested rerun) {
                                    if (++reruns > MAX_RERUNS_PER_REQUEST) {
                                        throw new IllegalStateException("Rerun signal triggered more than "
                                                + MAX_RERUNS_PER_REQUEST + " times");
                                    }
                                } catch (ControlSignals.StopRequested stop) {
                                    break;
                                }
                            }
                            return ctx.buildRoot();
                        } finally {
                            RenderContext.unbind();
                        }
                    })
                    .get();
            session.setLastRoot(root);
            session.touch();
            return root;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Render interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        }
    }

    @Override
    public void close() {
        executor.close();
    }
}
