ARG HADOOP_VERSION
FROM iwnet/hadoop-base-${HADOOP_VERSION}

# Specify hbase resources.
ARG HBASE_VERSION
ARG HBASE_DATADIR=/data/hbase
ARG HBASE_PREFIX=/usr/local/hbase
ARG HBASE_URL=http://archive.apache.org/dist/hbase/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz
#https://dist.apache.org/repos/dist/release/hbase/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz

RUN \
    # Create hbase directories.
    mkdir -p ${HBASE_DATADIR} ${HBASE_PREFIX} && \
    # Download hbase gpg keys.
    curl http://archive.apache.org/dist/hbase/KEYS -o HBASE_KEYS && \ 
    gpg --import HBASE_KEYS && \
    # Download, install hbase.
    curl -fSL "${HBASE_URL}" -o /tmp/hbase.tar.gz && \
    curl -fSL "${HBASE_URL}.asc" -o /tmp/hbase.tar.gz.asc && \
    gpg --verify /tmp/hbase.tar.gz.asc && \
    tar -C "${HBASE_PREFIX}" --strip=1 -xzf /tmp/hbase.tar.gz && \
    rm /tmp/hbase.tar.gz*

# Add configuration files.
ADD ./hbase-site.xml ./regionservers "${HBASE_PREFIX}/conf/"

EXPOSE 2181 8080 8085 9090 9095 60000 60010 60201 60301

# Set hbase environment variables.
ENV HBASE_HOME=${HBASE_PREFIX}
ENV HBASE_DATADIR=${HBASE_DATADIR}
