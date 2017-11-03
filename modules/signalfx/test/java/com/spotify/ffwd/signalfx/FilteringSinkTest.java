package com.spotify.ffwd.signalfx;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.signalfx.metrics.flush.AggregateMetricSender;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers;
import com.spotify.ffwd.filter.TrueFilter;
import com.spotify.ffwd.model.Metric;
import com.spotify.ffwd.output.FilteringPluginSink;
import eu.toolchain.async.AsyncFramework;
import eu.toolchain.async.TinyAsync;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FilteringSinkTest {

    private FilteringPluginSink filteringSink;
    @Spy
    private SignalFxPluginSink signalFxSink;

    private AsyncFramework async;
    private Supplier<AggregateMetricSender> senderSupplier;

    private final int threadCount = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    private final String METRIC_KEY = "signalfx_filter_test";
    private final String WHAT = "error-reply-ratio";

    @Mock
    private AggregateMetricSender sender;

    @Mock
    private AggregateMetricSender.Session mockSession;


    @Before
    public void setup() {
        async = TinyAsync.builder().executor(executor).build();
        senderSupplier = () -> sender;
        signalFxSink = new SignalFxPluginSink();
        signalFxSink.async = async;
        signalFxSink.senderSupplier = senderSupplier;
    }

    @Test
    public void sendMetricPassAll() throws Exception {
        filteringSink = new FilteringPluginSink(new TrueFilter());

        Field field = FilteringPluginSink.class.getDeclaredField("sink");
        field.setAccessible(true);
        field.set(filteringSink, signalFxSink);

        System.out.println(filteringSink);

        Metric metric =
            new Metric(METRIC_KEY, 1278, new Date(), "", ImmutableSet.of(),
                ImmutableMap.of("what", WHAT, "pod", "awsus"), "test_proc");
        final List<Metric> metricsList = Collections.singletonList(metric);


        ArgumentCaptor<SignalFxProtocolBuffers.DataPoint> captor = ArgumentCaptor.forClass(SignalFxProtocolBuffers.DataPoint.class);
        when(sender.createSession()).thenReturn(mockSession);
        when(mockSession.setDatapoint(captor.capture())).thenReturn(mockSession);

        filteringSink.start();

        metricsList.stream().forEach(filteringSink::sendMetric);

        filteringSink.stop();

        List<SignalFxProtocolBuffers.DataPoint> points = captor.getAllValues();
        assertEquals(1, points.size());

    }

}
