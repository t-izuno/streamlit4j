package io.streamlit4j.core.runtime;

import io.streamlit4j.core.protocol.RenderNode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ScriptRunner implements AutoCloseable {

    private final ExecutorService executor;

    public ScriptRunner() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public Future<RenderNode> submit(RenderContext ctx, Runnable entrypoint) {
        return executor.submit(() -> {
            RenderContext.bind(ctx);
            try {
                entrypoint.run();
                return ctx.buildRoot();
            } finally {
                RenderContext.unbind();
            }
        });
    }

    @Override
    public void close() {
        executor.close();
    }
}
