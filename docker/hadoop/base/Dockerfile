FROM openjdk:8-slim-stretch

# Specify hadoop resources.
ARG HADOOP_VERSION
ARG HADOOP_PREFIX=/usr/local/hadoop
ARG HADOOP_URL=https://archive.apache.org/dist/hadoop/core/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz

# Update stretch repositories
RUN sed -i s/deb.debian.org/archive.debian.org/g /etc/apt/sources.list
RUN sed -i 's|security.debian.org|archive.debian.org/|g' /etc/apt/sources.list
RUN sed -i '/stretch-updates/d' /etc/apt/sources.list

RUN \
    # Install dependencies.
    DEBIAN_FRONTEND=noninteractive \
    apt-get update && \
    apt-get install --yes --no-install-recommends procps curl gpg host netcat && \
    # Clean.
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    # Create hadoop directory.
    mkdir -p ${HADOOP_PREFIX} && \
    # Download hadoop gpg keys.
    curl https://dist.apache.org/repos/dist/release/hadoop/common/KEYS -o HADOOP_KEYS && \
    gpg --import HADOOP_KEYS && \
    # Download, install hadoop.
    curl -fSL "${HADOOP_URL}" -o /tmp/hadoop.tar.gz && \
    curl -fSL "${HADOOP_URL}.asc" -o /tmp/hadoop.tar.gz.asc && \
    gpg --verify /tmp/hadoop.tar.gz.asc && \
    tar -C "${HADOOP_PREFIX}" --strip=1 -xzf /tmp/hadoop.tar.gz && \
    rm /tmp/hadoop.tar.gz*

# Set hadoop environment variables.
ENV HADOOP_HOME         "${HADOOP_PREFIX}"
ENV HADOOP_COMMON_HOME  "${HADOOP_PREFIX}"
ENV HADOOP_CONF_DIR     "${HADOOP_PREFIX}/etc/hadoop"
ENV HADOOP_HDFS_HOME    "${HADOOP_PREFIX}"
ENV HADOOP_MAPRED_HOME  "${HADOOP_PREFIX}"
ENV HADOOP_NAMENODE_DIR "/var/local/hadoop/hdfs/namenode/"
ENV HADOOP_DATANODE_DIR "/var/local/hadoop/hdfs/datanode/"

# Set YARN environment variables.
ENV HADOOP_YARN_HOME   "${HADOOP_PREFIX}"
ENV YARN_CONF_DIR      "${HADOOP_PREFIX}/etc/hadoop"

# Hdfs ports
EXPOSE 50010 50020 50070 50075 50090 8020 9000
# Mapred ports
EXPOSE 19888
#Yarn ports
EXPOSE 8030 8031 8032 8033 8040 8042 8088
#Other ports
EXPOSE 49707 2122

# Add configuration files.
ADD ./hdfs-site.xml ./core-site.xml "${HADOOP_PREFIX}/etc/hadoop/"
