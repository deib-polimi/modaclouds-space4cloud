# Space 4Clouds + Dependencies

Docker container for Space 4Clouds and everything it needs.

## Disclaimer of warranty

Licensor provides the Work (and each Contributor provides its Contributions) on an "**AS IS**" *BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND*, either express or implied, including, without limitation, any warranties or conditions of *TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE*. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

More details on the disclaimer at [Apache 2.0]. Following more details on the components and licenses.

### Components and licenses

The components developed directly by the [MODAClouds group](http://www.modaclouds.eu) are the following:
* Space 4Clouds, license: [Apache 2.0]
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

To start it:

```sh
docker run --name <name> \
-p <port>:5901 \
-v <local path to models>:/opt/models \
deibpolimi/space4clouds-all
```

and please remember that all the passwords are `modaclouds`.

If you want, you could also specify the resolution that the server will use. The default is `1440x900`, you could for example use `1680x1050` in this way:

```sh
docker run --name <name> \
-p <port>:5901 \
-v <local path to models>:/opt/models \
-e "GEOMETRY=1680x1050" \
deibpolimi/space4clouds-all
```

On Linux, another option is to run eclipse and use directly the running X.Org:

```sh
docker run --name <name> \
-v <local path to models>:/opt/models \
-e DISPLAY=$DISPLAY \
-v /tmp/.X11-unix:/tmp/.X11-unix \
deibpolimi/space4clouds-all \
s4c
```
