ARG HADOOP_VERSION 
FROM iwnet/hadoop-yarn-nodemanager-${HADOOP_VERSION}

# Specify hadoop resources.
ARG HADOOP_PREFIX=/usr/local/hadoop

# Specify hbase resources.
ARG HBASE_VERSION
ARG HBASE_PREFIX=/usr/local/hbase
ARG HBASE_URL=http://archive.apache.org/dist/hbase/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz

RUN \
    # Create hbase directories.
    mkdir -p ${HBASE_PREFIX} && \
    # Download hbase gpg keys.
    curl http://archive.apache.org/dist/hbase/KEYS -o HBASE_KEYS && \
    gpg --import HBASE_KEYS && \
    # Download, install hbase.
    curl -fSL "${HBASE_URL}" -o /tmp/hbase.tar.gz && \
    curl -fSL "${HBASE_URL}.asc" -o /tmp/hbase.tar.gz.asc && \
    gpg --verify /tmp/hbase.tar.gz.asc && \
    tar -C "${HBASE_PREFIX}" --strip=1 -xzf /tmp/hbase.tar.gz && \
    rm /tmp/hbase.tar.gz*

# Set hbase environment variables.
ENV HBASE_HOME=${HBASE_PREFIX}

# Specify spark resources.
ARG SPARK_VERSION
ARG SPARK_PREFIX=/usr/local/spark
ARG SPARK_URL=https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop2.7.tgz

RUN \
    # Install dependencies.
    DEBIAN_FRONTEND=noninteractive \
    apt-get update && \
    apt-get upgrade --yes && \
    apt-get install --yes --no-install-recommends \
        python3 \
        python3-psycopg2 \
        python3-dateutil \
        python3-requests \
        python3-numpy \
        python3-pandas && \
    # Clean.
    apt-get clean autoclean && \
    apt-get autoremove --yes && \
    # Create spark, resources directories.
    mkdir -p ${SPARK_PREFIX} /resources && \
    # Download postgresql jar.
    curl https://jdbc.postgresql.org/download/postgresql-42.2.1.jar --output /resources/postgresql-42.2.1.jar && \
    # Download spark gpg keys.
    curl https://dist.apache.org/repos/dist/release/spark/KEYS -o SPARK_KEYS && \
    gpg --import SPARK_KEYS && \
    # Download, install spark.
    curl -fSL "${SPARK_URL}" -o /tmp/spark.tgz && \
    curl -fSL "${SPARK_URL}.asc" -o /tmp/spark.tgz.asc && \
    gpg --verify /tmp/spark.tgz.asc && \
    tar -C "${SPARK_PREFIX}" --strip=1 -xzf /tmp/spark.tgz && \
    rm /tmp/spark.tgz* && \
    apt install python3-pip --yes && \
    pip3 install kafka-python
    
# Add hbase configuration files.
ADD ./hbase-site.xml "${SPARK_PREFIX}/conf/"

RUN mkdir /ssl
COPY ./ssl/. /ssl/.

# Set spark environment variables.
ENV SPARK_HOME "${SPARK_PREFIX}"
ENV PYSPARK_PYTHON "/usr/bin/python3"
