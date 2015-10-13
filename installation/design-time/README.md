# MODAClouds DesignTime

Docker container for the whole design time in MODAClouds, which means Space 4Clouds and everything it needs, and Modelio Creator 4Clouds.

## Disclaimer of warranty

Licensor provides the Work (and each Contributor provides its Contributions) on an "**AS IS**" *BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND*, either express or implied, including, without limitation, any warranties or conditions of *TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE*. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

More details on the disclaimer at [Apache 2.0]. Following more details on the components and licenses.

### Components and licenses

The components developed directly by the [MODAClouds group](http://www.modaclouds.eu) are the following:
* Space 4Clouds, license: [Apache 2.0]
* Modelio Creator 4Clouds, licenses: [GPL 3.0] and [Apache 2.0]
* Line Solver, license: [BSD New]

Main components the tools above depends on, not developed by us and released as is:
* Eclipse Kepler, license: [EPL 1.0]
* Palladio Component Model (PCM) plugin for Eclipse, license: [EPL 1.0]
* Matlab Compiler Runtime R2015a
* MySQL Server, license: [GPL 3.0]
* CMPL, license: [GPL 3.0]

Other components used in the container:
* LXDE, lxterminal and the LXDE icon theme
* GTK2
* X.Org
* OpenJDK 1.7
* Tight VNC Server
* Xrdp

[BSD New]: https://opensource.org/licenses/BSD-3-Clause
[Apache 2.0]: https://www.apache.org/licenses/LICENSE-2.0
[EPL 1.0]: https://www.eclipse.org/org/documents/epl-v10.php
[GPL 3.0]: https://www.gnu.org/licenses/gpl-3.0.en.html

## How to run it

To run this container, you can either go on to the next paragraph, or you could use a pseudo-app that we have created. Download it for:

* [Mac OS X](https://github.com/deib-polimi/modaclouds-space4cloud/raw/master/installation/bin/MODAClouds_DesignTime.dmg), tested on El Capitan 10.11, needs either [homebrew](http://brew.sh) or docker, docker-machine and VirtualBox (install them via [docker-toolbox](https://www.docker.com/toolbox) or via homebrew again)
* [Linux](https://github.com/deib-polimi/modaclouds-space4cloud/raw/master/installation/bin/MODAClouds_DesignTime-linux.tar.gz), tested on ArchLinux and Ubuntu 14.04, no difference in architecture, needs of course [docker](https://docs.docker.com/installation/)
* ~~Windows~~ (not available at the moment)

## How to run it directly

To start it:

```sh
docker run --name <name> \
-p <port1>:5901 \
-p <port2>:3389 \
-v <local path to s4c models>:/opt/space4clouds \
-v <local path to modelio models>:/opt/modelio \
deibpolimi/modaclouds-designtime
```

and please remember that all the passwords are `modaclouds`.

If you want, you could also specify the resolution that the server will use. The default is `1440x900`, you could for example use `1680x1050` in this way:

```sh
docker run --name <name> \
-p <port1>:5901 \
-p <port2>:3389 \
-v <local path to s4c models>:/opt/space4clouds \
-v <local path to modelio models>:/opt/modelio \
-e "GEOMETRY=1680x1050" \
deibpolimi/modaclouds-designtime
```

On Linux, another option is to run eclipse and use directly the running x.org:

```sh
docker run --name <name> \
-v <local path to s4c models>:/opt/space4clouds \
-v <local path to modelio models>:/opt/modelio \
-e DISPLAY=$DISPLAY \
-v /tmp/.X11-unix:/tmp/.X11-unix \
deibpolimi/modaclouds-designtime \
s4c
```

for Space 4Clouds, and for Modelio Creator 4Clouds:

```sh
docker run --name <name> \
-v <local path to s4c models>:/opt/space4clouds \
-v <local path to modelio models>:/opt/modelio \
-e DISPLAY=$DISPLAY \
-v /tmp/.X11-unix:/tmp/.X11-unix \
deibpolimi/modaclouds-designtime \
modelio
```
