#!/bin/bash

apt-get -y update && apt-get -y upgrade
apt-get install -y curl
apt-get install -y make
apt-get install -y vim
apt-get install -y docker.io
snap install docker

sleep 2
echo $USER
curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

apt-get purge -y --auto-remove apparmor
service docker restart
docker system prune -y --all --volumes

reboot


