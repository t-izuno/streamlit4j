package io.streamlit4j.core.runtime;

import io.streamlit4j.core.domain.Session;
import io.streamlit4j.core.port.Renderer;
import io.streamlit4j.core.protocol.RenderNode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default {@link Renderer} that executes each render on a fresh virtual thread. Holds the executor lifecycle; call
 * {@link #close()} during shutdown.
 */
public final class ScriptRunner implements Renderer, AutoCloseable {

    private static final int MAX_RERUNS_PER_REQUEST = 5;

    private final ExecutorService executor;

    /** Creates a runner backed by a virtual-thread-per-task executor. */
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
                        } catch (RerunRequested rerun) {
                            reruns++;
                            if (reruns > MAX_RERUNS_PER_REQUEST) {
                                throw new IllegalStateException(
                                        "Rerun signal triggered more than " + MAX_RERUNS_PER_REQUEST + " times", rerun);
                            }
                        } catch (StopRequested stop) {
                            break;
                        }
                    }
                    return ctx.buildRoot();
                } finally {
                    RenderContext.unbind();
                }
            }).get();
            session.setLastRoot(root);
            session.touch();
            return root;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Render interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Render failed", e);
        }
    }

    @Override
    public void close() {
        executor.close();
    }
}
