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
package de.qaware.playground.zwitscher.util;

import com.google.common.net.HostAndPort;
import de.qaware.playground.zwitscher.util.diagnosability.MetricRegistryServletContextListener;
import de.qaware.playground.zwitscher.util.servicediscovery.impl.ConsulFabioServiceDiscovery;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.compose.FallbackConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.consul.ConsulConfigurationSourceBuilder;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.reload.strategy.PeriodicalReloadStrategy;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TestConfiguration {

    private String CONFIG_ENV = "zwitscher-test";

    @Test
    public void testConsulConfiguration() {
        //consul configuration source
        HostAndPort consulHost = ConsulFabioServiceDiscovery.getConsulHostAndPort();
        ConfigurationSource consulSource = new ConsulConfigurationSourceBuilder()
                .withHost(consulHost.getHostText())
                .withPort(consulHost.getPort())
                .build();

        ConfigurationProvider provider = new ConfigurationProviderBuilder()
                .withConfigurationSource(consulSource)
                .withMetrics(MetricRegistryServletContextListener.getMetricRegistryInstance(), "cfg4j")
                .withReloadStrategy(new PeriodicalReloadStrategy(2, TimeUnit.SECONDS))
                .withEnvironment(new ImmutableEnvironment(CONFIG_ENV))
                .build();

        String key = provider.getProperty("testkey", String.class);
        assertThat(key, equalTo("success!"));
    }

    @Test
    public void testFileConfiguration() {
        //file configuration source
        ConfigFilesProvider configFilesProvider = () -> Arrays.asList(
                Paths.get("application.yaml"));
        ConfigurationSource fileSource = new ClasspathConfigurationSource(configFilesProvider);

        ConfigurationProvider provider = new ConfigurationProviderBuilder()
                .withConfigurationSource(fileSource)
                .withMetrics(MetricRegistryServletContextListener.getMetricRegistryInstance(), "cfg4j")
                .withReloadStrategy(new PeriodicalReloadStrategy(2, TimeUnit.SECONDS))
                .withEnvironment(new ImmutableEnvironment(CONFIG_ENV))
                .build();

        String key = provider.getProperty("testkey", String.class);
        assertThat(key, equalTo("success! from file!"));
        String key2 = provider.getProperty("testkey2", String.class);
        assertThat(key2, equalTo("more success!"));
    }

    @Test
    public void testFallbackConfiguration() {
        //file configuration source
        ConfigFilesProvider configFilesProvider = () -> Arrays.asList(
                Paths.get("application.yaml"));
        ConfigurationSource fileSource = new ClasspathConfigurationSource(configFilesProvider);

        //consul configuration source
        HostAndPort consulHost = ConsulFabioServiceDiscovery.getConsulHostAndPort();
        ConfigurationSource consulSource = new ConsulConfigurationSourceBuilder()
                .withHost(consulHost.getHostText())
                .withPort(consulHost.getPort())
                .build();

        //fallback to config file if consul is not reachable
        FallbackConfigurationSource fallbackConfigSource = new FallbackConfigurationSource(consulSource, fileSource);
        //config values first from Consul and then lookup in file
        MergeConfigurationSource mergedConfigSource = new MergeConfigurationSource(fileSource, fallbackConfigSource);

        ConfigurationProvider provider = new ConfigurationProviderBuilder()
                .withConfigurationSource(mergedConfigSource)
                .withMetrics(MetricRegistryServletContextListener.getMetricRegistryInstance(), "cfg4j")
                .withReloadStrategy(new PeriodicalReloadStrategy(2, TimeUnit.SECONDS))
                .withEnvironment(new ImmutableEnvironment(CONFIG_ENV))
                .build();

        String key = provider.getProperty("testkey", String.class);
        assertThat(key, equalTo("success!"));

        String key2 = provider.getProperty("testkey2", String.class);
        assertThat(key2, equalTo("more success!"));
    }
}