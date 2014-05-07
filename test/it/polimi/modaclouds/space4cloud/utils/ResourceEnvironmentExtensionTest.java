package it.polimi.modaclouds.space4cloud.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ResourceEnvironmentExtensionTest {

	@Test
	public void parseOfBizSample() {
		File extension = new File("target/test/ofBiz_model/resource_model_extension_OfBiz.xml");
		ResourceEnvironmentExtentionLoader loader = null; 
		try {
			loader = new ResourceEnvironmentExtentionLoader(extension);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Map<String, String> providers = new HashMap<String,String>();
		providers.put("_ervoQKhyEeOVLLp4qCj_jg", "Amazon");
		providers.put("_-sJ1AMhrEeKON4DtRoKCMw", "Amazon");
		
		Map<String, String> sizes = new HashMap<String,String>();
		sizes.put("_ervoQKhyEeOVLLp4qCj_jg", "m1.small");
		sizes.put("_-sJ1AMhrEeKON4DtRoKCMw", "m1.small");
		
		Map<String, String> names = new HashMap<String,String>();
		names.put("_ervoQKhyEeOVLLp4qCj_jg", "Elastic Compute Cloud (EC2)");
		names.put("_-sJ1AMhrEeKON4DtRoKCMw", "Elastic Compute Cloud (EC2)");
		
		Map<String, String> types = new HashMap<String,String>();
		types.put("_ervoQKhyEeOVLLp4qCj_jg", "Compute");
		types.put("_-sJ1AMhrEeKON4DtRoKCMw", "Compute");
		
		Map<String, int[]> replicas = new HashMap<String,int[]>();
		int[] first = {1,5,2,2,10,10,5,15,15,8,8,15,10,10,20,10,25,7,30,30,30,18,9,40};
		int[] second = {3,5,10,15,20,25,15,35,20,22,25,27,30,32,69,74,78,42,88,93,98,103,108,57};
		replicas.put("_ervoQKhyEeOVLLp4qCj_jg", first);
		replicas.put("_-sJ1AMhrEeKON4DtRoKCMw", second );
		
		//the region		
		assertEquals("us-east",loader.getRegion());
		//the resources
		assertEquals(providers.keySet(), loader.getProviders().keySet());
		//the providers
		assertEquals(providers,loader.getProviders());
		//the instance sizes
		assertEquals(sizes, loader.getInstanceSize());
		//the service name
		assertEquals(names, loader.getServiceName());		
		//the service types
		assertEquals(types, loader.getServiceType());
				
	}

}
