# Environment variables:
# PORT: port the JEE container will listen to
# CONSUL: consul endpoint in HostAndPort format: <host>:<port>
FROM qaware-oss-docker-registry.bintray.io/base/debian8-jre8
MAINTAINER QAware GmbH <qaware-oss@qaware.de>

RUN mkdir -p /opt/zwitscher-board
COPY build/classes/ /opt/zwitscher/classes/
COPY build/dependency/ /opt/zwitscher/dependency/

EXPOSE 12340
ENTRYPOINT java -cp /opt/zwitscher/classes/:/opt/zwitscher/dependency/* com.kumuluz.ee.EeApplication