ARG HADOOP_VERSION
FROM iwnet/hadoop-base-${HADOOP_VERSION}

# Specify hadoop resources.
ARG HADOOP_PREFIX=/usr/local/hadoop

# Add YARN configuration file.
ADD ./yarn-site.xml "${HADOOP_PREFIX}/etc/hadoop/"

# Set YARN environment variables.
ENV HADOOP_YARN_HOME   "${HADOOP_PREFIX}"
ENV YARN_CONF_DIR      "${HADOOP_PREFIX}/etc/hadoop"

# Add entrypoint for nodemanager.
ADD ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

CMD [ "/entrypoint.sh" ]
