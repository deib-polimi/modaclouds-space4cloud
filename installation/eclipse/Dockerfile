FROM ubuntu:14.04
MAINTAINER Riccardo B. Desantis <riccardobenito.desantis@polimi.it>

# Setting the environment
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive
ENV USER root
ENV GEOMETRY 1440x900

# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    xorg \
    lxde-core \
    lxterminal \
    lxde-icon-theme \
    gtk2-engines \
    dmz-cursor-theme \
    tightvncserver \
    default-jdk \
    curl && \
    apt-get autoclean && \
    apt-get autoremove && \
    rm -rf /var/lib/apt/lists/*

# Install eclipse
WORKDIR ${USERHOME}
RUN curl -OL "ftp://ftp.modaclouds.eu/public/space4cloud/distribution/eclipse_palladio.tar.gz" && \
    tar zxf eclipse_palladio.tar.gz && rm eclipse_palladio.tar.gz
WORKDIR Desktop
RUN printf "%b\n" '#!'"`which env` xdg-open\n\n[Desktop Entry]\nExec=/root/eclipse_palladio/eclipse -data /root/workspace\nIcon=/root/eclipse_palladio/icon.xpm\nTerminal=false\nType=Application\nPath=/root/eclipse_palladio/" > Space\ 4Clouds.desktop && \
    ln -s /opt/models models
WORKDIR ${USERHOME}

# Set the VNC server
WORKDIR ${USERHOME}
RUN echo "modaclouds" > password.txt && \
    cat password.txt password.txt | vncpasswd && \
    rm password.txt

RUN printf "%b\n" '#!'"`which sh`\n\n/root/eclipse_palladio/eclipse -data /root/workspace" > /usr/bin/s4c && \
    chmod +x /usr/bin/s4c

# Starts the VNC server
EXPOSE 5901
CMD vncserver :1 -desktop S4C -geometry $GEOMETRY -depth 24 -dpi 100 && tail -F /root/.vnc/*.log
