#!/bin/bash

# Write myid only if it does not exist.
if [ ! -f "$ZOOKEEPER_DATADIR/myid" ]; then
    echo "${ZOOKEEPER_MYID}" > "$ZOOKEEPER_DATADIR/myid"
fi

# Start zookeeper.
echo "Starting Zookeeper daemon."
$ZOOKEEPER_HOME/bin/zkServer.sh start-foreground
