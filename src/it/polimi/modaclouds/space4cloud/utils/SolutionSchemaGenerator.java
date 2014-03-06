package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.BlobStorage;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Database;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.FilesystemStorage;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.HourlySolution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.NOSQL;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Provider;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SQL;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Storage;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class SolutionSchemaGenerator extends SchemaOutputResolver{

	   public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {		   
	        File file = new File(suggestedFileName);
	        System.out.println("filename: "+file.getAbsolutePath());
	        StreamResult result = new StreamResult(file);
	        result.setSystemId(file.toURI().toURL().toString());
	        return result;
	    }
	   
	   public static void main(String[] args){
		   System.out.println("generating schema");	
		   Class[] classes = new Class[17]; 
		   
		   //solution.impl package		  		   		   
		   classes[0] = BlobStorage.class;
		   classes[1] = CloudService.class;
		   classes[2] = Component.class;
		   classes[3] = Compute.class;
		   classes[4] = Database.class;
		   classes[5] = FilesystemStorage.class;
		   classes[6] = Functionality.class;
		   classes[7] = HourlySolution.class;
		   classes[8] = IaaS.class;
		   classes[9] = Instance.class;
		   classes[10] = NOSQL.class;
		   classes[11] = PaaS.class;
		   classes[12] = Provider.class;
		   classes[13] = Solution.class;
		   classes[14] = SQL.class;
		   classes[15] = Storage.class;
		   classes[16] = Tier.class;
		   
		   
		   //solution package
//		   classes[17] = IArchitecturalConstrainable.class;
//		   classes[18] = IConstrainable.class;
//		   classes[19] = IFlowConstrainable.class;
//		   classes[20] = IQoSConstrainable.class;
//		   classes[21] = IReplicaConstrainable.class;
//		   classes[22] = IResponseTimeConstrainable.class;
//		   classes[23] = IUtilizationConstrainable.class;
		   try {
			JAXBContext jaxbContext = JAXBContext.newInstance(classes);
			
			 
			SchemaOutputResolver sor = new SolutionSchemaGenerator();
			jaxbContext.generateSchema(sor);
		} catch (JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
}

