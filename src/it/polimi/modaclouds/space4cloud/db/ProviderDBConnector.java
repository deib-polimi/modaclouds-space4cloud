/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * 
 */
package it.polimi.modaclouds.space4cloud.db;

import it.polimi.modaclouds.resourcemodel.cloud.Backend;
import it.polimi.modaclouds.resourcemodel.cloud.BlobStorage;
import it.polimi.modaclouds.resourcemodel.cloud.CloudElement;
import it.polimi.modaclouds.resourcemodel.cloud.CloudElementType;
import it.polimi.modaclouds.resourcemodel.cloud.CloudFactory;
import it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform;
import it.polimi.modaclouds.resourcemodel.cloud.CloudPlatformType;
import it.polimi.modaclouds.resourcemodel.cloud.CloudProvider;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResourceType;
import it.polimi.modaclouds.resourcemodel.cloud.CloudStorage;
import it.polimi.modaclouds.resourcemodel.cloud.CloudStorageType;
import it.polimi.modaclouds.resourcemodel.cloud.Compute;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.CostProfile;
import it.polimi.modaclouds.resourcemodel.cloud.CostUnitType;
import it.polimi.modaclouds.resourcemodel.cloud.Database;
import it.polimi.modaclouds.resourcemodel.cloud.DatabaseType;
import it.polimi.modaclouds.resourcemodel.cloud.FilesystemStorage;
import it.polimi.modaclouds.resourcemodel.cloud.Frontend;
import it.polimi.modaclouds.resourcemodel.cloud.IaaS_Service;
import it.polimi.modaclouds.resourcemodel.cloud.Middleware;
import it.polimi.modaclouds.resourcemodel.cloud.NoSQL_DB;
import it.polimi.modaclouds.resourcemodel.cloud.OSType;
import it.polimi.modaclouds.resourcemodel.cloud.PaaS_Service;
import it.polimi.modaclouds.resourcemodel.cloud.RelationalDB;
import it.polimi.modaclouds.resourcemodel.cloud.V_Memory;
import it.polimi.modaclouds.resourcemodel.cloud.V_Storage;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResourceType;
import it.polimi.modaclouds.space4cloud.iterfaces.GenericDBConnector;
import it.polimi.modaclouds.space4cloud.utils.EMF;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Provides a Database Connector for Generic Cloud Services.
 * 
 * @author Davide Franceschelli
 * 
 */
public class ProviderDBConnector implements GenericDBConnector {

	/** The db connection. */
	private final Connection dbConnection;

	/** The provider. */
	private final CloudProvider provider;

	/** The emf. */
	private final EMF emf;

	/** The iaas list. */
	private List<IaaS_Service> iaasList;

	/** The iaas dictionary**/
	private HashMap<String, IaaS_Service> iaasMap;

	/** The paas list. */
	private List<PaaS_Service> paasList;

	/** The paas dictionary**/
	private HashMap<String, PaaS_Service> paasMap;


	/**
	 * Creates a new Database Connector for a Generic Cloud Provider.
	 *
	 * @param cp the cp
	 */
	public ProviderDBConnector(CloudProvider cp) {
		dbConnection = new DatabaseConnector().getConnection();
		provider = cp;
		emf = new EMF();
	}

	/**
	 * Return the database connection.
	 * 
	 * @return a Connection element.
	 */
	public Connection getDbConnection() {
		return dbConnection;
	}

	/**
	 * Return the Cloud Provider.
	 * 
	 * @return a CloudProvider element.
	 * @see CloudProvider
	 */
	public CloudProvider getProvider() {
		return provider;
	}

	/**
	 * Return an EMF instance.
	 * 
	 * @return an EMF element.
	 * @see EMF
	 */
	public EMF getEmf() {
		return emf;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getIaaSServices()
	 */
	@Override
	public List<IaaS_Service> getIaaSServices() {
		if (iaasList != null)
			return iaasList;
		try {
			createIaasSets();
			return iaasList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.iterfaces.GenericDBConnector#getIaaSServicesHashMap()
	 */
	@Override
	public HashMap<String, IaaS_Service> getIaaSServicesHashMap(){
		if (iaasMap != null)
			return iaasMap;
		try {
			createIaasSets();
			return iaasMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Creates the iaas sets: List and HashMap.
	 *
	 * @throws SQLException the sQL exception
	 */
	private  void createIaasSets() throws SQLException  {
		ResultSet rs = dbConnection.createStatement().executeQuery(
				"select * from iaas_service where CloudProvider_id="
						+ provider.getId() + " order by name");
		CloudFactory cf = emf.getCloudFactory();
		List<IaaS_Service> list = new ArrayList<IaaS_Service>();
		HashMap<String, IaaS_Service> dict = new HashMap<>();
		IaaS_Service i;
		while (rs.next()) {
			i = cf.createIaaS_Service();
			i.setId(rs.getInt(1));
			i.setName(rs.getString(3));
			for (CloudResource cr : getCloudResources(i)) {
				i.getComposedOf().add(cr);
			}
			list.add(i);
			dict.put(i.getName(), i);
		}
		iaasList = list;
		iaasMap = dict;
		rs.close();
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getPaaSServices()
	 */


	@Override
	public List<PaaS_Service> getPaaSServices() {
		if (paasList != null)
			return paasList;
		try {
			createPaasSets();
			return paasList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method that create the PaasSets : List and HashMap
	 * @throws SQLException
	 */
	private void createPaasSets() throws SQLException {
		ResultSet rs = dbConnection.createStatement().executeQuery(
				"select * from paas_service where CloudProvider_id="
						+ provider.getId() + " order by name");
		CloudFactory cf = emf.getCloudFactory();
		List<PaaS_Service> list = new ArrayList<PaaS_Service>();
		HashMap<String, PaaS_Service> dict = new HashMap<>();
		PaaS_Service i;
		while (rs.next()) {
			i = cf.createPaaS_Service();
			i.setId(rs.getInt(1));
			i.setName(rs.getString(3));
			for (CloudPlatform cp : getCloudPlatforms(i)){
				i.getComposedOf().add(cp);
			}
			list.add(i);
			dict.put(i.getName(), i);
		}
		paasList = list;
		paasMap = dict;
		rs.close();
	}

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.iterfaces.GenericDBConnector#getPaaSServicesHashMap()
	 */
	@Override
	public HashMap<String, PaaS_Service> getPaaSServicesHashMap(){
		if (paasMap != null)
			return paasMap;
		try {
			createPaasSets();
			return paasMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getCloudPlatforms(cloud.PaaS_Service)
	 */
	@Override
	public List<CloudPlatform> getCloudPlatforms(PaaS_Service paas) {
		try {
			ResultSet rs = dbConnection.createStatement().executeQuery(
					"select * from paas_service_composedof P, cloudplatform CP where P.PaaS_id="
							+ paas.getId() + " and P.CloudPlatform_id=CP.id order by name");
			CloudFactory cf = emf.getCloudFactory();
			List<CloudPlatform> list = new ArrayList<CloudPlatform>();
			CloudPlatform i;
			while (rs.next()) {
				CloudPlatformType type = CloudPlatformType.getByName(rs
						.getString(5));
				switch (type) {
				case BACKEND:
					i = cf.createBackend();
					break;
				case MIDDLEWARE:
					i = cf.createMiddleware();
					break;
				case FRONTEND:
					i = cf.createFrontend();
					break;
				case DATABASE:
					DatabaseType dbType = DatabaseType.getByName(rs
							.getString(6));
					switch (dbType) {
					case NOSQL:
						NoSQL_DB n = cf.createNoSQL_DB();
						n.setDBType(dbType);
						i = n;
						break;
					case RELATIONAL:
						RelationalDB r = cf.createRelationalDB();
						r.setDBType(dbType);
						i = r;
						break;
					default:
						throw new Exception("Undefined Database Type.");
					}
					break;
				default:
					throw new Exception("Undefined Cloud Platform Type.");
				}
				i.setType(CloudElementType.PLATFORM);
				i.setPlatformType(type);
				i.setId(rs.getInt(3));
				i.setName(rs.getString(4));
				i.setLanguage(rs.getString(7));
				i.setTechnology(rs.getString(8));
				if (rs.getObject(9) != null)
					i.setHasCostProfile(getCostProfile(i, rs.getInt(9)));
				defineCosts(i);
				ResultSet rs1 = dbConnection.createStatement().executeQuery(
						"select * from runon R, cloudresource CR where R.CloudPlatform_id="
								+ rs.getInt(3)
								+ " and R.CloudResource_id=CR.id");
				CloudResource cr;
				while (rs1.next()) {
					CloudResourceType crtype = CloudResourceType.getByName(rs1
							.getString(5));
					switch (crtype) {
					case COMPUTE:
						Compute c = cf.createCompute();
						c.setOS(OSType.getByName(rs1.getString(7)));
						cr = c;
						break;
					case CLOUDSTORAGE:
						CloudStorageType storageType = CloudStorageType
						.getByName(rs1.getString(6));
						switch (storageType) {
						case BLOBSTORAGE:
							BlobStorage bs = cf.createBlobStorage();
							bs.setStorageType(storageType);
							cr = bs;
							break;
						case FILESYSTEM:
							FilesystemStorage fs = cf.createFilesystemStorage();
							fs.setStorageType(storageType);
							cr = fs;
							break;
						default:
							throw new Exception("Undefined Cloud Storage Type.");
						}
						break;
					default:
						throw new Exception("Undefined Cloud Resource Type.");
					}
					cr.setType(CloudElementType.RESOURCE);
					cr.setResourceType(crtype);
					cr.setId(rs1.getInt(3));
					cr.setName(rs1.getString(4));
					List<VirtualHWResource> lvhr = getVHRs(cr);
					if (lvhr != null)
						for (VirtualHWResource v : lvhr)
							cr.getComposedOf().add(v);
					if (rs1.getObject(8) != null)
						cr.setHasCostProfile(getCostProfile(cr, rs1.getInt(8)));
					defineCosts(cr);
					i.getRunsOnCloudResource().add(cr);
				}
				list.add(i);
				rs1.close();
			}
			rs.close();

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<CloudPlatform>();
		}
	}

	/**
	 * Retrieves from the database the Cost Profile with the specified id,
	 * associated to the generic Cloud Element provided as input.
	 * 
	 * @param ce
	 *            is the CloudElement needed to select the right cost profile
	 *            table within the database.
	 * @param id
	 *            is the integer representing the id of the CostProfile.
	 * @return a CostProfile element if the operation succeeds, null otherwise.
	 * @see CostProfile
	 * @see CloudElement
	 */
	private CostProfile getCostProfile(CloudElement ce, int id) {
		try {
			String s = "";
			if (ce instanceof CloudResource)
				s = "cloudresource";
			else if (ce instanceof CloudPlatform)
				s = "cloudplatform";
			else
				throw new Exception("Undefined Cloud Element.");
			ResultSet rs = dbConnection.createStatement().executeQuery(
					"select * from " + s + "_costprofile CP where CP.id=" + id);
			CloudFactory cf = emf.getCloudFactory();
			CostProfile cp = cf.createCostProfile();
			while (rs.next()) {
				ResultSet rs1 = dbConnection
						.createStatement()
						.executeQuery(
								"select * from "
										+ s
										+ "_costprofile_cost X, cost C where X.CostProfile_id="
										+ id + " and X.Cost_id=C.id");
				cp.setId(id);
				cp.setDescription(rs.getString(2));
				cp.setAssociatedToCloudElement(ce);
				while (rs1.next()) {
					Cost cost = cf.createCost();
					// cost.setAssociatedToCloudElement(ce);
					cost.setId(rs1.getInt(3));
					cost.setDescription(rs1.getString(4));
					cost.setUnit(CostUnitType.getByName(rs1.getString(5)));
					cost.setValue(rs1.getDouble(6));
					if (rs1.getObject(7) != null)
						cost.setPeriod(rs1.getInt(7));
					else
						cost.setPeriod(-1);
					if (rs1.getObject(8) != null)
						cost.setDefinedOn(getVHRByID(rs1.getInt(8)));
					else
						cost.setDefinedOn(null);
					if (rs1.getObject(9) != null)
						cost.setLowerBound(rs1.getInt(9));
					else
						cost.setLowerBound(-1);
					if (rs1.getObject(10) != null)
						cost.setUpperBound(rs1.getInt(10));
					else
						cost.setUpperBound(-1);
					cp.getComposedOf().add(cost);
				}
				rs1.close();
			}
			rs.close();
			return cp;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Retrieves from the database the list of Cost associated to the specified
	 * Cloud Element.
	 * 
	 * @param ce
	 *            is the input CloudElement.
	 * @return a List of Cost elements if the operation succeeds, null
	 *         otherwise.
	 * @see Cost
	 * @see CloudElement
	 */
	private List<Cost> getCosts(CloudElement ce) {
		try {
			String s = "";
			String s1 = "";
			if (ce instanceof CloudResource) {
				s = "cloudresource";
				s1 = "CloudResource";
			} else if (ce instanceof CloudPlatform) {
				s = "cloudplatform";
				s1 = "CloudPlatform";
			} else
				throw new Exception("Udefined Cloud Element.");
			List<Cost> list = new ArrayList<Cost>();
			ResultSet rs = dbConnection.createStatement().executeQuery(
					"select * from cost C, " + s + "_cost X where X." + s1
					+ "_id=" + ce.getId() + " and X.Cost_id=C.id");
			CloudFactory cf = emf.getCloudFactory();
			while (rs.next()) {
				Cost cost = cf.createCost();
				cost.setAssociatedToCloudElement(ce);
				cost.setId(rs.getInt(1));
				cost.setDescription(rs.getString(2));
				cost.setUnit(CostUnitType.getByName(rs.getString(3)));
				cost.setValue(rs.getDouble(4));
				if (rs.getObject(5) != null)
					cost.setPeriod(rs.getInt(5));
				else
					cost.setPeriod(-1);
				if (rs.getObject(6) != null)
					cost.setDefinedOn(getVHRByID(rs.getInt(6)));
				else
					cost.setDefinedOn(null);
				if (rs.getObject(7) != null)
					cost.setLowerBound(rs.getInt(7));
				else
					cost.setLowerBound(-1);
				if (rs.getObject(8) != null)
					cost.setUpperBound(rs.getInt(8));
				else
					cost.setUpperBound(-1);				
				cost.setRegion(rs.getString(9));					
				list.add(cost);				
			}
			rs.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Retrieves from the database the list of Virtual Hardware Resources
	 * associated to the specified Cloud Element.
	 * 
	 * @param ce
	 *            is the input CloudElement.
	 * @return a List of VirtualHWResource elements if the operation succeeds,
	 *         null otherwise.
	 * @see VirtualHWResource
	 * @see CloudElement
	 */
	private List<VirtualHWResource> getVHRs(CloudElement ce) {
		try {
			ResultSet rs = dbConnection
					.createStatement()
					.executeQuery(
							"select * from cloudresource_allocation CA, virtualhwresource VHR where CA.CloudResource_id="
									+ ce.getId()
									+ " and CA.VirtualHWResource_id=VHR.id");
			CloudFactory cf = emf.getCloudFactory();
			List<VirtualHWResource> lvhr = new ArrayList<VirtualHWResource>();
			while (rs.next()) {
				VirtualHWResource v = null;
				VirtualHWResourceType type = VirtualHWResourceType.getByName(rs
						.getString(4));
				switch (type) {
				case CPU:
					v = cf.createV_CPU();
					break;
				case MEMORY:
					V_Memory m = cf.createV_Memory();
					m.setSize(rs.getInt(7));
					v = m;
					break;
				case STORAGE:
					V_Storage s = cf.createV_Storage();
					s.setSize(rs.getInt(7));
					v = s;
					break;
				default:
					throw new Exception(
							"Undefined Virtual Hardware Resource Type.");
				}
				v.setId(rs.getInt(3));
				v.setType(type);
				v.setProcessingRate(rs.getDouble(5));
				v.setNumberOfReplicas(rs.getInt(6));
				lvhr.add(v);
			}
			rs.close();
			return lvhr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Retrieves from the database the Virtual Hardware Resource with the
	 * specified id.
	 * 
	 * @param id
	 *            is the id of the VirtualHWResource element.
	 * @return a VirtualHWResource element if the operation succeeds, null
	 *         otherwise.
	 * @see VirtualHWResource
	 */
	private VirtualHWResource getVHRByID(int id) {
		try {
			ResultSet rs = dbConnection.createStatement().executeQuery(
					"select * from virtualhwresource where id=" + id);
			CloudFactory cf = emf.getCloudFactory();
			VirtualHWResource v = null;
			while (rs.next()) {
				VirtualHWResourceType type = VirtualHWResourceType.getByName(rs
						.getString(2));
				switch (type) {
				case CPU:
					v = cf.createV_CPU();
					break;
				case MEMORY:
					V_Memory m = cf.createV_Memory();
					m.setSize(rs.getInt(5));
					v = m;
					break;
				case STORAGE:
					V_Storage s = cf.createV_Storage();
					s.setSize(rs.getInt(5));
					v = s;
					break;
				default:
					throw new Exception(
							"Undefined Virtual Hardware Resource Type.");
				}
				v.setId(rs.getInt(1));
				v.setType(type);
				v.setProcessingRate(rs.getDouble(3));
				v.setNumberOfReplicas(rs.getInt(4));
			}
			rs.close();
			return v;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the costs of the specified Cloud Element.
	 * 
	 * @param ce
	 *            is the input CloudElement.
	 * @see CloudElement
	 */
	private void defineCosts(CloudElement ce) {
		List<Cost> lc = getCosts(ce);
		if (lc != null)
			for (Cost c : lc)
				ce.getHasCost().add(c);
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getFrontendCloudPlatforms(cloud.PaaS_Service)
	 */
	@Override
	public List<Frontend> getFrontendCloudPlatforms(PaaS_Service paas) {
		List<Frontend> lf = new ArrayList<Frontend>();
		for (CloudPlatform cp : paas.getComposedOf())
			if (cp instanceof Frontend)
				lf.add((Frontend) cp);
		return lf;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getMiddlewareCloudPlatforms(cloud.PaaS_Service)
	 */
	@Override
	public List<Middleware> getMiddlewareCloudPlatforms(PaaS_Service paas) {
		List<Middleware> lm = new ArrayList<Middleware>();
		for (CloudPlatform cp : paas.getComposedOf())
			if (cp instanceof Middleware)
				lm.add((Middleware) cp);
		return lm;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getBackendCloudPlatforms(cloud.PaaS_Service)
	 */
	@Override
	public List<Backend> getBackendCloudPlatforms(PaaS_Service paas) {
		List<Backend> lb = new ArrayList<Backend>();
		for (CloudPlatform cp : paas.getComposedOf())
			if (cp instanceof Backend)
				lb.add((Backend) cp);
		return lb;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getDatabaseCloudPlatforms(cloud.PaaS_Service)
	 */
	@Override
	public List<Database> getDatabaseCloudPlatforms(PaaS_Service paas) {
		List<Database> ld = new ArrayList<Database>();
		for (CloudPlatform cp : paas.getComposedOf())
			if (cp instanceof Database)
				ld.add((Database) cp);
		return ld;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getCloudResources(cloud.IaaS_Service)
	 */
	@Override
	public List<CloudResource> getCloudResources(IaaS_Service iaas) {
		try {

			/*TODO: Non capisco questo codice, perchè accedere di nuovo al db? abbiamo già caricato le 
			 * risorse cloud quando abbiamo caricato i servizi anche se effettivamente non ne abbiamo definito il tipo*/
			ResultSet rs = dbConnection.createStatement().executeQuery(
					"select * from iaas_service_composedof I, cloudresource CR where I.IaaS_id="
							+ iaas.getId() + " and I.CloudResource_id=CR.id order by name");

			CloudFactory cf = emf.getCloudFactory();
			List<CloudResource> list = new ArrayList<CloudResource>();
			CloudResource i;
			while (rs.next()) {
				CloudResourceType type = CloudResourceType.getByName(rs
						.getString(5));
				switch (type) {
				case COMPUTE:
					Compute c = cf.createCompute();
					c.setOS(OSType.getByName(rs.getString(7)));
					i = c;
					break;
				case CLOUDSTORAGE:
					CloudStorageType storageType = CloudStorageType
					.getByName(rs.getString(6));
					switch (storageType) {
					case BLOBSTORAGE:
						BlobStorage bs = cf.createBlobStorage();
						bs.setStorageType(storageType);
						i = bs;
						break;
					case FILESYSTEM:
						FilesystemStorage fs = cf.createFilesystemStorage();
						fs.setStorageType(storageType);
						i = fs;
						break;
					default:
						rs.close();
						throw new Exception("Undefined Cloud Storage Type.");
					}
					break;
				default:
					rs.close();
					throw new Exception("Undefined Cloud Resource Type.");				
				}
				i.setType(CloudElementType.RESOURCE);
				i.setResourceType(type);
				i.setId(rs.getInt(3));
				i.setName(rs.getString(4));
				List<VirtualHWResource> lvhr = getVHRs(i);
				if (lvhr != null)
					for (VirtualHWResource v : lvhr)
						i.getComposedOf().add(v);

				if (rs.getObject(8) != null)
					i.setHasCostProfile(getCostProfile(i, rs.getInt(8)));

				defineCosts(i);
				list.add(i);
			}
			rs.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<CloudResource>();
		}
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getComputeCloudResources(cloud.IaaS_Service)
	 */
	@Override
	public List<Compute> getComputeCloudResources(IaaS_Service iaas) {
		List<Compute> lc = new ArrayList<Compute>();
		for (CloudResource cr : getCloudResources(iaas))
			if (cr instanceof Compute)
				lc.add((Compute) cr);
		return lc;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getCloudStorageCloudResources(cloud.IaaS_Service)
	 */
	@Override
	public List<CloudStorage> getCloudStorageCloudResources(IaaS_Service iaas) {
		List<CloudStorage> lcs = new ArrayList<CloudStorage>();
		for (CloudResource cr : getCloudResources(iaas))
			if (cr instanceof CloudStorage)
				lcs.add((CloudStorage) cr);
		return lcs;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getFileSystemStorageCloudResources(cloud.IaaS_Service)
	 */
	@Override
	public List<FilesystemStorage> getFileSystemStorageCloudResources(
			IaaS_Service iaas) {
		List<FilesystemStorage> lfs = new ArrayList<FilesystemStorage>();
		for (CloudResource cr : getCloudResources(iaas))
			if (cr instanceof FilesystemStorage)
				lfs.add((FilesystemStorage) cr);
		return lfs;
	}

	/* (non-Javadoc)
	 * @see it.polimi.franceschelli.space4cloud.iterfaces.GenericDBConnector#getBlobStorageCloudResources(cloud.IaaS_Service)
	 */
	@Override
	public List<BlobStorage> getBlobStorageCloudResources(IaaS_Service iaas) {
		List<BlobStorage> lbs = new ArrayList<BlobStorage>();
		for (CloudResource cr : getCloudResources(iaas))
			if (cr instanceof BlobStorage)
				lbs.add((BlobStorage) cr);
		return lbs;
	}

}
