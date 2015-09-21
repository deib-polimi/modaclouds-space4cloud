FROM ubuntu:14.04
MAINTAINER Riccardo B. Desantis <riccardobenito.desantis@polimi.it>

# Setting the environment
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive
ENV USER root

# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y \
    lxde-core \
    tightvncserver \
    default-jdk \
    curl

# Install eclipse
WORKDIR ${USERHOME}
RUN curl -OL "ftp://ftp.modaclouds.eu/public/space4cloud/distribution/eclipse_palladio.tar.gz" && \
    tar zxf eclipse_palladio.tar.gz && rm eclipse_palladio.tar.gz
WORKDIR Desktop
RUN echo "#\!/usr/bin/env xdg-open\n\n[Desktop Entry]\nExec=/root/eclipse_palladio/eclipse\nIcon=/root/eclipse_palladio/icon.xpm\nTerminal=false\nType=Application\nPath=/root/eclipse_palladio/" > Eclipse_Palladio.desktop
WORKDIR ${USERHOME}

# Set the VNC server
WORKDIR ${USERHOME}
RUN echo "modaclouds" > password.txt && \
    cat password.txt password.txt | vncpasswd && \
    rm password.txt

# Starts the VNC server
EXPOSE 5901
CMD vncserver :1 -geometry 1440x900 -depth 24 && tail -F /root/.vnc/*.log
