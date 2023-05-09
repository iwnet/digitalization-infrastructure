ARG HADOOP_VERSION
FROM iwnet/hadoop-base-${HADOOP_VERSION}

# Add entrypoint for namenode.
ADD ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

CMD [ "/entrypoint.sh" ]
