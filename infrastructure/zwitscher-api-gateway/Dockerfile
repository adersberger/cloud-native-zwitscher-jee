FROM janeczku/alpine-kubernetes:3.3
MAINTAINER QAware GmbH <qaware-oss@qaware.de>
ADD bin/fabio-1.2-go1.6.3_linux-amd64 /opt/fabio/fabio
ADD etc/fabio.properties /etc/fabio/fabio.properties
EXPOSE 9998 9999
CMD ["/opt/fabio/fabio", "-cfg", "/etc/fabio/fabio.properties"]