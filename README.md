MODAClouds SPACE4CLOUD
==========================

#Description
The System PerformAnce and Cost Evaluation on CLOUD (SPACE4CLOUD) tool is developed inside the [MODAClouds](http://www.modaclouds.eu/) project. It aims at automatizing the process of deciding a deployment configuration of a multi-cloud application. For more information look at project deliverables [D5.2.1](http://www.modaclouds.eu/wp-content/uploads/2012/09/MODAClouds_D5.2.1_MODACloudMLQoSAbstractionsAndPredictionModelsSpecificationInitialVersion.pdf) and [D5.4.1 ](http://www.modaclouds.eu/wp-content/uploads/2012/09/MODAClouds_D5.4.1_PredictionAndCostAssessmentToolProofOfConcept.pdf). 

The tool takes as input a Palladio Component Model (PCM) that describes the application, an extension file that specify the set of cloud resources used to run the application and a set of constraints. It then solves by mean of an heuristic an optimization problem in order to find a solution that fulfill the constraints and minimizes the costs. 

The user manual with information about the installation and usage of the tool can be found
here: ftp://home.dei.polimi.it/outgoing/Giovanni.Paolo.Gibilisco/space4cloudExample/SPACE4Cloud%20User%20Manual.pdf

#Installation

To install the latest release of SPACE4CLOUD an installation of Palladio 3.5 is needed. You can obtain it [here](http://www.palladio-simulator.com/tools/download/).

SPACE4CLOUD uses version 4.5.7.2 of LQNS solver that can be obtained [here](http://www.sce.carleton.ca/rads/lqns/lqn-documentation/) and version 0.5f of LINE that can be obtained [here](http://code.google.com/p/line/). 


To install SPACE4CLOUD you can either click on Help->Install new software.. and use ftp://home.dei.polimi.it/outgoing/Giovanni.Paolo.Gibilisco/space4cloud/ as update site or build it from the sources.

In order to beuild from sources checkout the project called de.uka.ipd.sdq.pcmsolver under Core/trun k from [palladio official repository](https://svnserver.informatik.kit.edu/i43/svn/code/Palladio/Core/trunk/Solver/de.uka.ipd.sdq.pcmsolver/), credentials to access the repository can be found [here](https://sdqweb.ipd.kit.edu/wiki/Palladio_Component_Model).
Then apply the patch de.uka.ipd.sdq.pcmsolver.patch that can be found under Palladio\patch. 

The interaction with the resource model DB is performed by [MySQL Connector/J]{http://dev.mysql.com/downloads/connector/j/} and needs the resource model meta model hosted in the [modaclouds-resourcemodel](https://github.com/deib-polimi/modaclouds-resourcemodel) repository. 

Dependencies to non plugin artifacts are managed by maven, in order to link needed jars in the maven repository to the workspace (under target/dependencies folder) run mvn dependency:copy-dependencies then compile the project. 

To run the code make a new Eclipse Application run configuration as shown [here](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Fguide%2Ftools%2Flaunchers%2Feclipse_application_launcher.htm) and specify the following VM arguments:
```java
-Dosgi.requiredJavaVersion=1.5 -Dhelp.lucene.tokenizer=standard -Xms64m -Xmx512m -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=128M
```

#Usage

The Palladio patch integrates the LINE solver into Palladio and it allows its selection while making a new run configuration. Please refer to the [Palladio documentation](https://sdqweb.ipd.kit.edu/wiki/Palladio_Component_Model/Documentation,_Tutorials,_and_Screencasts) for further information. 

To run the SPACE4CLOUD plugin launch the Eclipse Application run configuration then click on the cloud shaped icon.
In order to retrieve information about cloud resources SPACE4CLOUD interacts with a database that hosts the resource model. In this preliminary version the database is hosted on [Flexiscale](http://www.flexiscale.com/) and can be used only for testing purposes. The accuracy and update frequency of the information stored in the database is not guaranteed. 

#Examples
An example project can be found here: ftp://home.dei.polimi.it/outgoing/Giovanni.Paolo.Gibilisco/space4cloudExample. The example cam be imported in the new instance of Eclipse, evaluated using LINE or LQNS or used for the optimization performed by SPACE4CLOUD. 

The example model also contains some examples of constraints and extension that are used to drive the solution generation. 
