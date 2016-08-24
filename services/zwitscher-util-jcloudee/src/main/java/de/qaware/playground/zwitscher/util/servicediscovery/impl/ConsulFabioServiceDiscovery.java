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
package de.qaware.playground.zwitscher.util.servicediscovery.impl;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.google.common.io.Files;
import com.google.common.net.HostAndPort;
import de.qaware.playground.zwitscher.util.servicediscovery.IServiceDiscovery;
import io.mikael.urlbuilder.UrlBuilder;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Service registry based on Consul and Fabio.
 *
 * Registers service with right host and port depending on
 * if running local, inside a docker container or on Marathon.
 *
 * Local env:
 * - HOST: unset -> localhost
 * - PORT: $PORT envvar
 *
 * Docker env:
 * - HOST: $HOSTNAME -> /etc/hosts
 * - PORT: $PORT envvar
 * - Detect docker: /.dockerenv file
 *
 * DC/OS (Marathon) env:
 * - HOST: $HOST envvar
 * - PORT: $PORT envvar
 * - Detect Marathon: $MARATHON_APP_ID gesetzt
 *
 */
@Named("consul-fabio")
@ApplicationScoped
public class ConsulFabioServiceDiscovery implements IServiceDiscovery {

    @Inject
    private Logger logger;
    private static final long HEALTHCHECK_INTERVAL = 2L;

    public ConsulFabioServiceDiscovery() {
    }

    /**
     * Registeres a service
     *
     * see https://github.com/eBay/fabio/wiki/Service-Configuration
     */
    public synchronized void registerService(String serviceName, String servicePath) {

        String applicationHost = getOutboundHost();
        int applicationPort = getOutboundPort();
        HostAndPort consulEndpoint = getConsulHostAndPort();
        logger.info("Will register service on host {} and port {} at consul endpoint {}",
                    applicationHost, applicationPort, consulEndpoint.toString());

        //generate unique serviceId
        String serviceId = serviceName + "-" + applicationHost + ":" + applicationPort;
        String fabioServiceTag = "urlprefix-" + servicePath;

        //point healthcheck URL to dropwizard metrics healthcheck servlet
        URL serviceUrl = UrlBuilder.empty()
                .withScheme("http")
                .withHost(applicationHost)
                .withPort(applicationPort)
                .withPath("/metrics/ping").toUrl();

        // Service bei Consul registrieren inklusive einem Health-Check auf die URL des REST-Endpunkts.
        logger.info("Registering service with ID {} and NAME {} with healthcheck URL {} and inbound ROUTE {}",
                serviceId, serviceName, serviceUrl, fabioServiceTag);

        //use consul API to register service
        ConsulClient client = new ConsulClient(consulEndpoint.toString());
        NewService service = new NewService();
        service.setId(serviceId);
        service.setName(serviceName);
        service.setPort(applicationPort);
        service.setAddress(applicationHost);
        List<String> tags = new ArrayList<>();
        tags.add(fabioServiceTag);
        service.setTags(tags);
        //register health check
        NewService.Check check = new NewService.Check();
        check.setHttp(serviceUrl.toString());
        check.setInterval(ConsulFabioServiceDiscovery.HEALTHCHECK_INTERVAL + "s");
        service.setCheck(check);
        client.agentServiceRegister(service);
    }

    public static String getOutboundHost() {
        String hostName = System.getenv(HOSTNAME_ENVVAR);
        String host = System.getenv(HOST_ENVVAR);
        if (hostName == null && host == null) return DEFAULT_HOST;
        else if (host != null) return host;
        else {
            File etcHosts = new File("/etc/hosts");
            List<String> lines;
            try {
                lines = Files.readLines(etcHosts, Charset.defaultCharset());
            } catch (IOException e) {
                return DEFAULT_HOST;
            }
            for (String line: lines){
                if (!line.trim().startsWith("#") && !line.trim().isEmpty()) {
                    String[] etcEntry = line.split("\\s+");
                    if (etcEntry[1].equals(hostName)) return etcEntry[0];
                }
            }
            return DEFAULT_HOST;
        }
    }

    public static int getOutboundPort() {
        String portEnv = System.getenv(PORT_ENVVAR);
        if (portEnv == null) return DEFAULT_PORT;
        return Integer.valueOf(portEnv);
    }

    public static HostAndPort getConsulHostAndPort() {
        String consulEnv = System.getenv(CONSUL_ENVVAR);
        if (consulEnv == null) return HostAndPort.fromString(CONSUL_DEFAULT_HOSTANDPORT);
        else return HostAndPort.fromString(consulEnv);
    }

    private static final String PORT_ENVVAR = "PORT";
    private static final String HOST_ENVVAR = "HOST";
    private static final String HOSTNAME_ENVVAR = "HOSTNAME";
    private static final String CONSUL_ENVVAR = "CONSUL";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String CONSUL_DEFAULT_HOSTANDPORT = "localhost:8500";
}