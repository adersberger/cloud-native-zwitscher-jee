[![MIT License](https://img.shields.io/badge/license-MIT%20License-blue.svg)](https://github.com/qaware/cloud-native-zwitscher/blob/master/LICENSE)

# Cloud Native Zwitscher Showcase (JEE-based)

This showcase demonstrates how to build a cloud native application using
JEE and several cloud native infrastructure components. The individual parts
will later be deployed and run on DC/OS.

## Used technologies
 * JEE Service Container: [KumuluzEE](https://ee.kumuluz.com)
 * API Gateway / Edge Server: [Fabio](https://github.com/eBay/fabio) or [Traefik](https://traefik.io)
 * Service Discovery: [Consul](https://www.consul.io)
 * Service Client: [Jersey](https://jersey.java.net) and [Hystrix](https://github.com/Netflix/Hystrix)
 * Configuration & Coordination: [Consul](https://www.consul.io)
 * Cluster Resource Manager: [Mesos](http://mesos.apache.org) by [DC/OS](https://dcos.io)
 * Cluster / Container Orchestrator: [Marathon](https://mesosphere.github.io/marathon) by [DC/OS](https://dcos.io)
 * Diagnosability: [Metrics](http://metrics.dropwizard.io)

## Running the application

The application can be run on your local machine as well as on a remote DC/OS cluster.

### Local maschine
 * import the gradle project into your favorite IDE. JDK8 required.
 * perform a gradle build:

```shell
$ ./gradlew clean build
```

 * start the infrastructure (Consul and Fabio):

```shell
$ cd infrastructure
$ ./local-consul-up.sh
$ ./local-fabio-up.sh
```

 * pick out a service within the `./services` directory and run its `Main` class. Important: set the `PORT` environment variable before to free port of your choice.
 * now you hopefully see the service running within the JEE container and its service getting registered at Consul and Fabio.

You can also use `docker-compose` to launch a complete local system (all micro services and all
required infrastructure like Consul and Fabio). You need a remote docker daemon like the one provided
by `docker-machine`.

 * point your DOCKER_HOST environment variable to the remote docker daemon. If you're using docker-machine on macOS this is as easy as `eval "$(docker-machine env default)"`.
 * build your docker files with the `buildDockerImage` gradle task.
 * now you can start the local system with `./infrastructure/dockerized-up.sh` and stop it with
 `./infrastructure/dockerized-down.sh`.

### DC/OS
 * setup a DC/OS Cluster. You can do so locally by using DC/OS Vagrant (https://github.com/dcos/dcos-vagrant) or you use a public cloud provider. As the Vagrant setting consumes a lot of resources our current favorite is the packet (https://www.packet.net) IaaS cloud. It's fast and way cheaper than AWS for running a DC/OS cluster. A DC/OS cluster can be created by a Terraform script on Packet: https://dcos.io/docs/1.8/administration/installing/cloud/packet.
 * install the DC/OS command line tool and auth it to your cluster.
 * now you can start the cluster with `./infrastructure/marathonized-up.sh`and
 stop it with `./infrastructure/marathonized-down.sh`.

If you want to deploy the microservices you've to push them to docker hub:

 * point your DOCKER_HOST environment variable to the remote docker daemon. If you're using docker-machine on macOS this is as easy as `eval "$(docker-machine env default)"`.
 * set your docker hub credentials by using `./gradlew setCredentials --service docker --username <YOUR-DOCKER_HUB-USERNAME>`
 * Build and deploy the container:

 ```shell
 $ ./gradlew clean build buildDockerImage pushDockerImage
 ```

## License

This software is provided under the MIT open source license, read the `LICENSE` file for details.
