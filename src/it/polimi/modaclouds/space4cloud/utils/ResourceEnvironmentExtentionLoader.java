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

public class ResourceEnvironmentExtentionLoader extends ExtensionParser {

	public ResourceEnvironmentExtentionLoader(File extensionFile)
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		super(extensionFile, false);
		ResourceModelExtension loadedExtension = XMLHelper.deserialize(
				extension.toURI().toURL(), ResourceModelExtension.class);
		for (ResourceContainer container : loadedExtension
				.getResourceContainer()) {
			String id = container.getId();
			providers.put(id, container.getProvider());
			if (container.getCloudResource() != null) {
				IaasService resource = container.getCloudResource();
				serviceTypes.put(id, resource.getServiceType());
				serviceNames.put(id, resource.getServiceName());
				instanceSizes.put(id, resource.getResourceSizeID());
				String location = resource.getLocation().getRegion();
				if(resource.getLocation().getZone() != null)
					location += resource.getLocation().getZone();
				serviceLocations.put(id, location);
				int replicas[] = new int[HOURS];
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
