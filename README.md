# Cloud Native Zwitscher Showcase (JEE-based)

This showcase demonstrates how to build a cloud native application using
JEE and several cloud native infrastructure components. The individual parts
will later be deployed and run on DC/OS.

## Used technologies
 * JEE Service Container: [KumuluzEE](https://ee.kumuluz.com)
 * API Gateway / Edge Server: [Traefik](https://traefik.io)
 * Service Discovery: [Consul](https://www.consul.io)
 * Service Client: [Jersey](https://jersey.java.net) and [Hystrix](https://github.com/Netflix/Hystrix) and [Ribbon](https://github.com/Netflix/ribbon)
 * Configuration & Coordination: [Consul](https://www.consul.io)
 * Cluster Resource Manager: [Mesos](http://mesos.apache.org) by [DC/OS](https://dcos.io)
 * Cluster / Container Orchestrator: [Marathon](https://mesosphere.github.io/marathon) by [DC/OS](https://dcos.io)
 * Diagnosability: [Metrics](http://metrics.dropwizard.io), [Jolokia](https://jolokia.org)

## Build instructions

In order to compile and run the examples you do not need much. A recent JDK8 needs to
be available in your SEU.
```shell
$ ./gradlew clean build
```

## Running the Cloud Native Zwitscher showcase

The showcase can be run on your local machine as well as on a remote DC/OS cluster.

### Local maschine

TBD

### DC/OS

You need a DC/OS cluster with at least 3GB free RAM.
That amounts to one instance of each service.
You can use the DC/OS Vagrant to run a DC/OS cluster on your local machine,
provided your computer has at least 16GB of RAM
(https://github.com/dcos/dcos-vagrant).

With DC/OS Vagrant installed, use the following command to spin up a properly
sized cluster.
```shell
vagrant up m1 a1 a2 a3 boot
```

Each service comes with it's own Marathon config file. You can deploy the
services one at a time or all at once.

Example deployment of the config service:
```shell
curl -X POST http://m1.dcos:8080/v2/apps -H "Content-type: application/json" -d @zwitscher-config/marathon-zwitscher-config.json
```

#### Troubleshooting

* *The download of the docker images takes longer than the health check grace
period in Marathon.*
This may lead to Marathon perpetually trying and failing to deploy a service.
In case of this error, try downloading the images manually with Docker on the
cluster workers.
* *Not enough free memory on the VirtualBox Host.*
This usually leads to a dead slow cluster and may cause spurious errors. ensure
that you run the showcase on a host with at least 16 GB RAM, of which 13 GB must
remain free.
* *Other spurious errors.*
The setup has a lot of moving parts that might break down. In case you run into
errors that are not easily identifyable, try tearing down and re-creating the
cluster:
```shell
vagrant destroy -f m1 a1 a2 a3 boot
vagrant up m1 a1 a2 a3 boot
```

## License

This software is provided under the MIT open source license, read the `LICENSE.txt` file for details.

