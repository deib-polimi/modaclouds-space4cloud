#!/bin/sh

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    eval "$(docker-machine env s4c)"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

docker run --name db1 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=cloud -e MYSQL_USER=moda -e MYSQL_PASSWORD=modaclouds -p 3306:3306 -d mysql:latest
ID=`docker ps | grep mysql:latest | awk '{print $1}'`
docker exec $ID /bin/bash -c "apt-get update && apt-get install -y curl"
docker exec $ID curl -L https://raw.githubusercontent.com/deib-polimi/modaclouds-space4cloud/master/db/Dump.sql -o /root/Dump.sql
docker exec $ID /bin/bash -c "mysql -u root --password=root < /root/Dump.sql"
