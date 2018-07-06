#!/usr/bin/env bash

# Build jar with dockered maven
# permission issue, don't let the container (run as root) create the target folder
sudo rm -rf target
container_work_dir=/usr/src/proj
sudo docker run -it --rm -v "$PWD":"$container_work_dir" \
        -v "$HOME/.m2":/root/.m2 \
        -v "$PWD/target:$container_work_dir/target" \
        -w "$container_work_dir" maven:latest mvn package

sudo docker build -t chelidonium:2.0 .

sudo docker run -v /files1:/download -p 9292:9292 -d chelidonium:2.0

