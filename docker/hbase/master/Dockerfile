ARG HBASE_VERSION
FROM iwnet/hbase-base-${HBASE_VERSION}

# Add entrypoint for hbase master.
ADD ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

CMD [ "/entrypoint.sh" ]
