ARG HADOOP_VERSION
FROM iwnet/hadoop-base-${HADOOP_VERSION}

# Specify zookeeper resources.
ARG ZOOKEEPER_VERSION
ARG ZOOKEEPER_DATADIR=/data/zookeeper
ARG ZOOKEEPER_PREFIX=/usr/local/zookeeper
ARG ZOOKEEPER_URL=https://archive.apache.org/dist/zookeeper/zookeeper-${ZOOKEEPER_VERSION}/zookeeper-${ZOOKEEPER_VERSION}.tar.gz
#https://dist.apache.org/repos/dist/release/zookeeper/zookeeper-${ZOOKEEPER_VERSION}/zookeeper-$ZOOKEEPER_VERSION.tar.gz

RUN \
    # Create zookeeper directories.
    mkdir -p ${ZOOKEEPER_PREFIX} ${ZOOKEEPER_DATADIR} && \
    # Download zookeeper gpg keys.
    curl https://archive.apache.org/dist/zookeeper/KEYS -o ZOOKEEPER_KEYS && \ 
    gpg --import ZOOKEEPER_KEYS && \
    # Download, install zookeeper.
    curl -fSL "${ZOOKEEPER_URL}" -o /tmp/zookeeper.tar.gz && \
    curl -fSL "${ZOOKEEPER_URL}.asc" -o /tmp/zookeeper.tar.gz.asc && \
    gpg --verify /tmp/zookeeper.tar.gz.asc && \
    tar -C "${ZOOKEEPER_PREFIX}" --strip=1 -xzf /tmp/zookeeper.tar.gz && \
    rm /tmp/zookeeper.tar.gz*

# Add configuration files.
ADD ./zoo.cfg "${ZOOKEEPER_PREFIX}/conf/"

# Set zookeeper environment variables.
ENV ZOOKEEPER_HOME=${ZOOKEEPER_PREFIX}
ENV ZOOKEEPER_DATADIR=${ZOOKEEPER_DATADIR}

EXPOSE 2181 2888 3888

ADD ./entrypoint.sh /
RUN chmod +x /entrypoint.sh

CMD [ "/entrypoint.sh" ]
