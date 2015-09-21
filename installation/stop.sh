#!/bin/sh

IMAGE="$1"
VERSION="$2"

cd $IMAGE

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    eval "$(docker-machine env s4c)"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

for ID in $(docker ps -a | grep $IMAGE$VERSION | awk '{print $1}'); do
    docker kill $ID
    docker rm $ID
done

