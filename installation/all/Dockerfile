FROM ubuntu:14.04
MAINTAINER Riccardo B. Desantis <riccardobenito.desantis@polimi.it>

# Setting the environment
ENV JAVA_HOME /usr/lib/jvm/default-java
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive
ENV USER root
ENV GEOMETRY 1440x900

WORKDIR /tmp
# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y --force-yes --no-install-recommends \
    unzip \
    xorg \
    lxde-core \
    lxterminal \
    lxde-icon-theme \
    gtk2-engines \
    dmz-cursor-theme \
    tightvncserver \
    default-jdk \
    libglib2.0-0 \
    xrdp \
    curl && \
    curl -OL http://dev.mysql.com/get/mysql-apt-config_0.3.7-1ubuntu14.04_all.deb && \
    dpkg -i mysql-apt-config_0.3.7-1ubuntu14.04_all.deb && \
    rm mysql-apt-config_0.3.7-1ubuntu14.04_all.deb && \
    apt-get update && apt-get install -y --no-install-recommends mysql-server && \
    apt-get autoclean && \
    apt-get autoremove && \
    rm -rf /var/lib/apt/lists/*

# Installing MCR
RUN curl -L http://it.mathworks.com/supportfiles/downloads/R2015a/deployment_files/R2015a/installers/glnxa64/MCR_R2015a_glnxa64_installer.zip --create-dirs -o MCR/MCR_R2015a_glnxa64_installer.zip
#COPY MCR_R2015a_glnxa64_installer.zip MCR/
WORKDIR MCR
RUN unzip -q MCR_R2015a_glnxa64_installer.zip && \
    ./install -mode silent -agreeToLicense yes && \
    rm -rf /tmp/MCR

# Installing LINE
WORKDIR /opt
RUN curl -OL http://svn.code.sf.net/p/line-solver/code/branches/unstable/v072/bin/linux/LINE_072_linux.zip
#COPY LINE_072_linux.zip ./
RUN unzip -q LINE_072_linux.zip && \
    mv LINE_072_linux/bin LINE && \
    rm -rf LINE_072_linux*
WORKDIR LINE

# Fix the files
RUN printf "%b\n" "port=5463\nmaxIter=500\nmaxJobSize=6\nverbose=0\nparallel=PARFOR\ntimeoutConnection=3600\nrespTimePerc=SEFF\nrespTimePercMin=0.85\nrespTimePercStep=0.05\nrespTimePercMax=0.90\ndirectory=/opt/LINE\nsolver=FLUID" > LINE.properties && \
    sed 's|<value>[0-9]*.0</value>|<value>4.0</value>|' <lineClusterProfile.settings >lineClusterProfile-new.settings && \
    mv lineClusterProfile-new.settings lineClusterProfile.settings && \
    mv LINE LINE-orig && \
    printf "%b\n" '#!'"`which env` sh\n\nMCRROOT=\"/usr/local/MATLAB/MATLAB_Runtime/v85\"\nLINEROOT=\"/opt/LINE\"\n\ncd \${LINEROOT} && sh run_LINE.sh \${MCRROOT} \"\$@\"" > LINE && \
    sed 's|LINE|LINE-orig|' <run_LINE.sh >run_LINE-new.sh && \
    mv run_LINE-new.sh run_LINE.sh && \
    printf "%b\n" '#!'"`which sh`\n\nwhile true\ndo\n\t/opt/LINE/LINE /opt/LINE/LINE.properties\n\tsleep 2\ndone" > run_LINE_loop.sh && \
    chmod +x run_LINE.sh LINE run_LINE_loop.sh LINE-orig

# Install eclipse
WORKDIR ${USERHOME}
RUN curl -OL "ftp://ftp.modaclouds.eu/public/space4cloud/distribution/eclipse_palladio.tar.gz"
#COPY eclipse_palladio.tar.gz ./
RUN tar zxf eclipse_palladio.tar.gz && rm eclipse_palladio.tar.gz && \
    mkdir Desktop && \
    printf "%b\n" '#!'"`which env` xdg-open\n\n[Desktop Entry]\nExec=/root/eclipse_palladio/eclipse -data /root/workspace\nIcon=/root/eclipse_palladio/icon.xpm\nTerminal=false\nType=Application\nPath=/root/eclipse_palladio/" > Desktop/Space\ 4Clouds.desktop && \
    ln -s /opt/models Desktop/models

# Set the VNC server
RUN echo "modaclouds" > password.txt && \
    cat password.txt password.txt | vncpasswd && \
    rm password.txt

# Set the RDP server
RUN sed 's|\[xrdp1\]|\[xrdp1000\]\nname=VNC-Session\nlib=libvnc.so\nip=localhost\nport=5901\nusername=na\npassword=ask\n\n\[xrdp1\]|' </etc/xrdp/xrdp.ini >/etc/xrdp/xrdp-new.ini && \
    mv /etc/xrdp/xrdp-new.ini /etc/xrdp/xrdp.ini

# Install the DB and load the dump
RUN curl -OL https://raw.githubusercontent.com/deib-polimi/modaclouds-space4cloud/master/db/Dump.sql
RUN printf "%b\n" "CREATE USER 'moda'@'%' IDENTIFIED BY 'modaclouds';\nGRANT ALL PRIVILEGES ON *.* TO 'moda'@'%' WITH GRANT OPTION;" > createModa.sql && \
    mysql_install_db && \
    service mysql start && \
    mysqladmin -u root password 'modaclouds' && \
    mysql -u root --password=modaclouds < createModa.sql && \
    mysql -u root --password=modaclouds < Dump.sql && \
    rm Dump.sql createModa.sql

# Installing CMPL
WORKDIR /tmp
RUN curl -OL http://www.coliop.org/_download/Cmpl-1-10-0-linux64.tar.gz
RUN tar zxf Cmpl-1-10-0-linux64.tar.gz
WORKDIR Cmpl
RUN bash install
WORKDIR ..
RUN rm -rf Cmpl*
WORKDIR $USERHOME

RUN printf "%b\n" '#!'"`which sh`\n\nservice mysql start\n/root/eclipse_palladio/eclipse -data /root/workspace &\n/opt/LINE/run_LINE_loop.sh" > /usr/bin/s4c && \
    chmod +x /usr/bin/s4c

# Starts MySQL, LINE, the RDP and the VNC server
EXPOSE 5901
EXPOSE 3389
CMD service mysql start && service xrdp start && vncserver :1 -desktop S4C -geometry $GEOMETRY -depth 24 -dpi 100 && /opt/LINE/run_LINE_loop.sh
