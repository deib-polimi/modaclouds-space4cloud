#!/bin/sh

PORT="$1"

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    IP=`docker-machine ip s4c`
    ssh root@$IP -o StrictHostKeyChecking=no -p $PORT -i id_rsa
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    # Linux
    ssh root@localhost -o StrictHostKeyChecking=no -p $PORT -i id_rsa
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi
