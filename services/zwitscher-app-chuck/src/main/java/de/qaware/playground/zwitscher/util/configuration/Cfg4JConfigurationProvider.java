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
package de.qaware.playground.zwitscher.util.configuration;

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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * A configuration provider based on the cfg4j library with
 * primary Consul config values and secondary (in case of failure
 * or non-existing value) file configuration (in application.yaml).
 *
 * Requires environment variable CONFIG_ENV set to path of config. If
 * no according envvar is defined, CONFIG_ENV is set to "config".
 * In case of Consul CONFIG_ENV is the path to the key-value pairs and in
 * case of the config file this is the path to the application.yaml
 * file.
 */
@ApplicationScoped
public class Cfg4JConfigurationProvider {

    private static final String CONFIG_ENV = "CONFIG_ENV";
    private static final String CONFIG_FILE = "application.yaml";
    private static final String DEFAULT_CONFIG_ENV = "config";

    private static ConfigurationProvider cfgProvider;

    /**
     * CDI producer for cfg4j configuration providers.
     *
     * @return a ready-to-use cfg4j configuration provider.
     */
    @Produces
    public ConfigurationProvider produceLogger() {

        if (cfgProvider == null) {
            //file configuration source
            ConfigFilesProvider configFilesProvider = () -> Arrays.asList(
                    Paths.get(CONFIG_FILE));
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

            String configEnv = System.getenv(CONFIG_ENV);
            if (configEnv == null) configEnv = DEFAULT_CONFIG_ENV;

            cfgProvider = new ConfigurationProviderBuilder()
                    .withConfigurationSource(mergedConfigSource)
                    .withMetrics(MetricRegistryServletContextListener.getMetricRegistryInstance(), "cfg4j")
                    .withReloadStrategy(new PeriodicalReloadStrategy(2, TimeUnit.SECONDS))
                    .withEnvironment(new ImmutableEnvironment(configEnv))
                    .build();
        }

        return cfgProvider;
    }

}
