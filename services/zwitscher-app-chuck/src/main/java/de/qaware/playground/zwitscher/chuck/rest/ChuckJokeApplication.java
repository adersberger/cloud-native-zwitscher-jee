/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Josef Adersberger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.qaware.playground.zwitscher.chuck.rest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import de.qaware.playground.zwitscher.util.servicediscovery.IServiceDiscovery;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/chuck")
public class ChuckJokeApplication extends ResourceConfig {

    @Inject
    public ChuckJokeApplication(
            @Named("consul-fabio") IServiceDiscovery serviceDiscovery,
            MetricRegistry metricRegistry) {
        super();
        register(ChuckJokeResource.class);

        //Instrument application with metrics
        register(new InstrumentedResourceMethodApplicationListener(metricRegistry));
        HystrixPlugins.getInstance().registerMetricsPublisher(
                new HystrixCodaHaleMetricsPublisher(metricRegistry));

        //Register Prometheus metric exporter
        CollectorRegistry.defaultRegistry.register(new DropwizardExports(metricRegistry));

        //Register service
        serviceDiscovery.registerService("zwitscher-chuck", "/chuck/joke");
    }

}