package de.qaware.playground.zwitscher.util;

import com.codahale.metrics.MetricRegistry;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(CdiTestRunner.class)
public class TestMetrics {

    @Inject
    MetricRegistry metrics;

    @Test
    public void testMetrics() {
        metrics.counter("fun counter").inc();
    }
}
