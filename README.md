MODAClouds SPACE4CLOUD
==========================

#Description
The System PerformAnce and Cost Evaluation on CLOUD (SPACE4CLOUD) tool is developed inside the [MODAClouds](http://www.modaclouds.eu/) project. It aims at automatizing the process of deciding a deployment configuration of a multi-cloud application. For more information look at project deliverables [D5.2.1](http://www.modaclouds.eu/wp-content/uploads/2012/09/MODAClouds_D5.2.1_MODACloudMLQoSAbstractionsAndPredictionModelsSpecificationInitialVersion.pdf) and [D5.4.1 ](http://www.modaclouds.eu/wp-content/uploads/2012/09/MODAClouds_D5.4.1_PredictionAndCostAssessmentToolProofOfConcept.pdf). 

The tool takes as input a Palladio Component Model (PCM) that describes the application, an extension file that specify the set of cloud resources used to run the application and a set of constraints. It then solves by mean of an heuristic an optimization problem in order to find a solution that fulfill the constraints and minimizes the costs. 

#Installation

To compile sources for SPACE4CLOUD an installation of Palladio 3.5 is needed. You can obtain it [here](http://www.palladio-simulator.com/tools/download/).

SPACE4CLOUD uses version 4.5.7.2 of LQNS solver that can be obtained [here](http://www.sce.carleton.ca/rads/lqns/lqn-documentation/) and version 0.5e of LINE that can be obtained [here](http://code.google.com/p/line/). 

To interact with these solvers Palladio needs to be patched. To do so checkout the project called de.uka.ipd.sdq.pcmsolver under Core/trunk from [palladio official repository](https://svnserver.informatik.kit.edu/i43/svn/code/Palladio/Core/trunk/Solver/de.uka.ipd.sdq.pcmsolver/), credentials to access the repository can be found [here](https://sdqweb.ipd.kit.edu/wiki/Palladio_Component_Model).
Then apply the patch de.uka.ipd.sdq.pcmsolver.patch that can be found under Palladio\patch. 

The interaction with the resource model DB is performed by a connector whose code is hosted in the [modaclouds-space4cloud-mysqldriver](https://github.com/deib-polimi/modaclouds-space4cloud-mysqldriver) repository and needs the resource model meta model hosted in the [modaclouds-space4cloud-metamodel](https://github.com/deib-polimi/modaclouds-space4cloud-metamodel) repository. 

To run the code make a new Eclipse Application run configuration as shown [here](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Fguide%2Ftools%2Flaunchers%2Feclipse_application_launcher.htm) and specify the following VM arguments:
```java
-Dosgi.requiredJavaVersion=1.5 -Dhelp.lucene.tokenizer=standard -Xms64m -Xmx512m -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=128M
```

#Usage

The Palladio patch integrates the LINE solver into Palladio and it allows its selection while making a new run configuration. Please refer to the [Palladio documentation](https://sdqweb.ipd.kit.edu/wiki/Palladio_Component_Model/Documentation,_Tutorials,_and_Screencasts) for further information. 

To run the SPACE4CLOUD plugin launch the Eclipse Application run configuration then click on the cloud shaped icon.
In order to retrieve information about cloud resources SPACE4CLOUD interacts with a database that hosts the resource model. In this preliminary version the database is hosted on [Flexiscale](http://www.flexiscale.com/) and can be used only for testing purposes. The accuracy and update frequency of the information stored in the database is not guaranteed. 

#Examples
Example projects can be found in the SpecWeb.rar archive under the Palladio folder. The example cam be imported in the new instance of Eclipse, evaluated using LINE or LQNS or used for the optimization performed by SPACE4CLOUD. 

The example model also contains some examples of constraints and extension that are used to drive the solution generation. 
