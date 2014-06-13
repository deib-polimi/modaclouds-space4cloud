package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.qos_models.schema.IaasService;
import it.polimi.modaclouds.qos_models.schema.PaasService;
import it.polimi.modaclouds.qos_models.schema.ReplicaElement;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author GiovanniPaolo
 * Loads the extension of the Palladio resource environment model as defined in "resource_model_extension.xsd" under https://github.com/deib-polimi/modaclouds-qos-models, s4cextension branch  
 */
public class ResourceEnvironmentExtentionLoader extends ResourceEnvironmentExtensionParser {

	/**
	 * 
	 * @param extensionFile xml file containing the extension of the model 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public ResourceEnvironmentExtentionLoader(File extensionFile)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		//build structures
		super(extensionFile, false);

		//load the model 
		ResourceModelExtension loadedExtension = XMLHelper.deserialize(
				extension.toURI().toURL(), ResourceModelExtension.class);
		
		//fill structures
		for (ResourceContainer container : loadedExtension
				.getResourceContainer()) {
//			String id = container.getId();
			String provider = container.getProvider();
			String id = container.getId() + (provider!=null?provider:"");
			providers.put(id, provider);
			if (container.getCloudResource() != null) {
				IaasService resource = container.getCloudResource();
				serviceTypes.put(id, resource.getServiceType());
				serviceNames.put(id, resource.getServiceName());
				instanceSizes.put(id, resource.getResourceSizeID());
				
				if (resource.getLocation() != null) {
                    String location = resource.getLocation().getRegion();
                    if(resource.getLocation().getZone() != null)
                        location += resource.getLocation().getZone();
                    setRegion(container.getProvider(), location);
                }
				
//				String location = resource.getLocation().getRegion();
//				if(resource.getLocation().getZone() != null){
//					location += resource.getLocation().getZone();
//				}
//				serviceLocations.put(id, location);
				
				int[] replicas = new int[HOURS];
				for (int i = 0; i<HOURS;i++) {
					replicas[i] = 1;
				}
				if (resource.getReplicas() != null) {
					for (ReplicaElement element : resource.getReplicas()
							.getReplicaElement()) {
						replicas[(int) element.getHour()] = element.getValue();
					}
				}
				instanceReplicas.put(id, replicas);
			} else {
				PaasService resource = container.getCloudPlatform();
				serviceTypes.put(id, resource.getServiceType());
				serviceNames.put(id, resource.getServiceName());
			}

		}
	}

}
