ARG SPARK_VERSION
FROM iwnet/spark-${SPARK_VERSION}

RUN \
    # Make directories.
    mkdir -p /code /usr/share/man/man1 /usr/share/man/man7 /uploads /results && \
    # Install packages.
    apt-get install -y \
        postgresql-client \
        maven \
        tmux \
        vim \
        curl

RUN mkdir -p /ssl
COPY ./ssl/. /ssl/.

# Set working directory.
WORKDIR /code

CMD [ "/bin/sh", "-c", "while true; do sleep 1d; done"]
