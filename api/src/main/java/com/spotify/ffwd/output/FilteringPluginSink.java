package com.spotify.ffwd.output;

import com.google.inject.Inject;
import com.spotify.ffwd.filter.Filter;
import com.spotify.ffwd.model.Batch;
import com.spotify.ffwd.model.Event;
import com.spotify.ffwd.model.Metric;
import eu.toolchain.async.AsyncFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilteringPluginSink implements PluginSink {
    @Inject
    protected PluginSink sink;

    protected final Filter filter;

    public FilteringPluginSink(final Filter filter) {
        this.filter = filter;
    }

    @Override
    public void init() {

    }

    @Override
    public void sendEvent(final Event event) {
        if (filter.matchesEvent(event)) {
            sink.sendEvent(event);
        }
    }

    @Override
    public void sendMetric(final Metric metric) {
        if (filter.matchesMetric(metric)) {
            sink.sendMetric(metric);
        }
    }

    @Override
    public void sendBatch(final Batch batch) {
        if (filter.matchesBatch(batch)) {
            sink.sendBatch(batch);
        }
    }

    @Override
    public AsyncFuture<Void> start() {
        log.info("Starting filtering sink {}", filter);
        return sink.start();
    }

    @Override
    public AsyncFuture<Void> stop() {
        return sink.stop();
    }

    @Override
    public boolean isReady() {
        return sink.isReady();
    }
}
