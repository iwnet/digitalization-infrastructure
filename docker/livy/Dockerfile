ARG SPARK_VERSION
FROM iwnet/spark-${SPARK_VERSION}

# Specify livy resources.
ARG LIVY_VERSION
ARG LIVY_PREFIX=/usr/local/livy
ARG LIVY_URL=https://downloads.apache.org/incubator/livy/${LIVY_VERSION}/apache-livy-${LIVY_VERSION}-bin.zip

RUN \
    # Install dependencies.
    DEBIAN_FRONTEND=noninteractive \
    apt-get install --yes --no-install-recommends \
        unzip \
        python-requests \
        python-requests-kerberos \
        python-flake8 \
        python-flaky \
        python-pytest && \
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    # Download livy gpg keys.
    curl https://downloads.apache.org/incubator/livy/KEYS -o LIVY_KEYS && \
    gpg --import LIVY_KEYS && \
    # Make directories
    mkdir -p "${LIVY_PREFIX}" "${LIVY_PREFIX}/logs" /tmp/livy_unzipped/ && \
    # Download, install livy.
    curl -fSL "${LIVY_URL}" -o /tmp/livy.zip && \
    curl -fSL "${LIVY_URL}.asc" -o /tmp/livy.zip.asc && \
    gpg --verify /tmp/livy.zip.asc && \
    unzip -d /tmp/livy_unzipped/ /tmp/livy.zip && \
    mv /tmp/livy_unzipped/*/* "${LIVY_PREFIX}" && \
    rm -rf /tmp/livy_unzipped /tmp/livy.zip*

# Set environment variables
ENV LIVY_HOME=${LIVY_PREFIX}

# Add configuration files.
ADD ./livy.conf "${LIVY_PREFIX}/conf/"
ADD ./livy-client.conf "${LIVY_PREFIX}/conf/"

# Add entrypoint.
ADD ./entrypoint.sh /
RUN chmod +x /entrypoint.sh

CMD [ "./entrypoint.sh" ]
