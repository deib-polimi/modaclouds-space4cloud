FROM ubuntu:14.04
MAINTAINER Riccardo B. Desantis <riccardobenito.desantis@polimi.it>

# Setting the environment
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive

WORKDIR /tmp
# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y curl && \
    curl -OL http://dev.mysql.com/get/mysql-apt-config_0.3.7-1ubuntu14.04_all.deb && \
    dpkg -i mysql-apt-config_0.3.7-1ubuntu14.04_all.deb && \
    apt-get update && apt-get install -y mysql-server

# Install the DB and load the dump
RUN curl -OL https://raw.githubusercontent.com/deib-polimi/modaclouds-space4cloud/master/db/Dump.sql && \
    echo "CREATE USER 'moda'@'%' IDENTIFIED BY 'modaclouds';\nGRANT ALL PRIVILEGES ON *.* TO 'moda'@'%' WITH GRANT OPTION;" > createModa.sql && \
    mysql_install_db && \
    service mysql start && \
    mysqladmin -u root password 'modaclouds' && \
    mysql -u root --password=modaclouds < createModa.sql && \
    mysql -u root --password=modaclouds < Dump.sql && \
    rm Dump.sql createModa.sql && \
    service mysql stop && \
    rm mysql-apt-config_0.3.7-1ubuntu14.04_all.deb
WORKDIR ${USERHOME}

# Start the MySQL server
EXPOSE 3306
CMD mysqld