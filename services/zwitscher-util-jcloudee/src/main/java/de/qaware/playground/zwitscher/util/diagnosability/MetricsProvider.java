package de.qaware.playground.zwitscher.util.diagnosability;

import com.codahale.metrics.MetricRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Provides access to a singleton MetricRegistry.
 *
 * Right now it simply gets the MetricRegistry from
 * MetricRegistryServletContextListener or a new instance
 * if listener is not initialized.
 */
@ApplicationScoped
public class MetricsProvider {

    /**
     * Creates / fetches a metric registry.
     *
     * @return a singleton metric registry
     */
    @Produces
    public static MetricRegistry getMetricRegistry() {
        MetricRegistry mr =  MetricRegistryServletContextListener.getMetricRegistryInstance();
        if (mr == null) mr = new MetricRegistry();
        return mr;
    }
}
