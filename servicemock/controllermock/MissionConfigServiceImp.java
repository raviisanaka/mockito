package drdo.cair.isrd.icrs.mcs.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import drdo.cair.isrd.icrs.loggers.CustomLevels;
import drdo.cair.isrd.icrs.mcs.config.McsCassandraTemplate;
import drdo.cair.isrd.icrs.mcs.controller.MissionConfigController;
import drdo.cair.isrd.icrs.mcs.dao.MissionConfigDao;
import drdo.cair.isrd.icrs.mcs.entity.Mission;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfig;
import drdo.cair.isrd.icrs.mcs.entity.MissionDataManagement;
import drdo.cair.isrd.icrs.mcs.entity.PlatformConfig;
import drdo.cair.isrd.icrs.mcs.entity.PlatformServices;
import drdo.cair.isrd.icrs.mcs.entity.RequireElement;
import drdo.cair.isrd.icrs.mcs.entity.ServiceNode;
import drdo.cair.isrd.icrs.mcs.entity.ServicesByPlatform;
import drdo.cair.isrd.icrs.mcs.entity.platform.Capability;
import drdo.cair.isrd.icrs.mcs.entity.platform.Parameters;
import drdo.cair.isrd.icrs.mcs.entity.platform.Requires;
import drdo.cair.isrd.icrs.mcs.repository.MissionRepo;


/**
 * 
 * @author user
 *
 */
@Service
public class MissionConfigServiceImp implements MissionConfigService {
	@Autowired
	private MissionConfigDao missionConfigDao;
	
	@Autowired
	private McsCassandraTemplate mcsCassandraTemplate;
	
	@Autowired
	CustomLevels logLevels;
	private static final Logger logger = Logger.getLogger(MissionConfigServiceImp.class);
	
	/**
	 * User will Configure the Mission Configuration Information by Object
	 * @param mission
	 * @return {@link MissionConfig}
	 */
  
	
	@Override
	public MissionConfig insertMissionConfig(MissionConfig mission) {

		return missionConfigDao.insertMissionConfig(mission);
	}

	/**
	 * User to get the all Mission Configuration Information
	 * @return {@link MissionConfig}
	 */
	@Override
	public List<MissionConfig> getAllMissionConfigDetails() {
		return missionConfigDao.getAllMissionConfigDetails();
	}
	
	/**
	 * 
	 */
	@Override
	public List<MissionConfig> getMissionConfigDataById(UUID missionConfigId) {

		return missionConfigDao.getMissionConfigDataById(missionConfigId);
	}

	/**
	 * 
	 */
	@Override
	public List<MissionConfig> getMissionConfigDataByPlatformId(UUID missionConfigId, UUID platform_id) {

		return missionConfigDao.getMissionConfigDataByPlatformId(missionConfigId,platform_id);
	}
	
	@Override
	public List<MissionConfig> getMissionConfigPlatformIds(UUID missionConfigId) {
		return missionConfigDao.getMissionConfigPlatformIds(missionConfigId);
	}

	@Override
	public List<MissionConfig> missionConfigListByUser(String username) {
		
		return missionConfigDao.missionConfigListByUser(username);
	}

	@Override
	public MissionConfig updateMissionConfig(MissionConfig mission) {
		
		return missionConfigDao.updateMissionConfig(mission);
	}

	@Override
	public void removeMissionConfig(UUID missionConfigId, UUID platform_id) {
		// TODO Auto-generated method stub
		missionConfigDao.removeMissionConfig(missionConfigId, platform_id);
	}

	@Override
	public void removeMissionConfig(UUID missionConfigId, UUID platform_id, UUID serviceId) {
		missionConfigDao.removeMissionConfig(missionConfigId, platform_id, serviceId);
	}
	
	@Override
	public List<Mission> getAllRunningMissions() {
		return missionConfigDao.getAllRunningMissions();
	}
	
	@Override
	public List<Mission> getMissionRunDataById(UUID missionRunId) {
		return missionConfigDao.getMissionRunDataById(missionRunId);
	}
	
	@Override
	public List<Mission> getRunningMissionDataByRunId(UUID missionRunId) {
		return missionConfigDao.getRunningMissionDataByRunId(missionRunId);
	}

	
	
	@Override
	public PlatformConfig insertPlatformConfig(PlatformConfig platformConfig) {
		
		return missionConfigDao.insertPlatformConfig(platformConfig);
	}

	@Override
	public List<PlatformConfig> getAllPlatformConfigUIData() {
		
		return missionConfigDao.getAll();
	}

	@Override
	public List<PlatformConfig> getPlatformConfigDataById(UUID platform_id) {
		
		return missionConfigDao.getPlatformConfigDataById(platform_id);
	}
	@Override
	public ServicesByPlatform getPlatformServiceDataById(UUID serviceId) {
		return missionConfigDao.getPlatformServiceDataById(serviceId);
	}

	@Override
	public void deleteGlobalPlatformConfigDataById(UUID platformId) {
		
		missionConfigDao.deleteGlobalPlatformConfigDataById(platformId);
	}

	@Override
	public MissionConfig updateMissionConfigDataById(boolean archived, UUID missionConfigId) {
		// TODO Auto-generated method stub
		return missionConfigDao.updateMissionConfigDataById(archived, missionConfigId);
	}
	
	

	@Override
	public void removeMissionConfig(UUID missionConfigId) {
		missionConfigDao.removeMissionConfig(missionConfigId);
		
	}

	@Override
	public List<MissionConfig> getAllMissionConfigDetailsBasedOnMissionName(String missionName) {
		return missionConfigDao.getAllMissionConfigDetailsBasedOnMissionName(missionName);
	}
	
	/** fetching the services from objectmodel xml file
	 * @return {@link List of Capability}
	 */
	public List<Capability> getPlatformServices(String platformType) {
		List<Capability> capabilityListValues = new ArrayList<>();
		try {
			Resource resourcefile=new ClassPathResource("/ObjectModel2.xml");
			String fullPath=resourcefile.getURI().toString();
			String filePath=fullPath.replace("file:", "");
			String filePathWithoutSpace=filePath.replace("%20", " ");
			File fxmlFile = new File(filePathWithoutSpace);
		
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
			Document doc = dbBuilder.parse(fxmlFile);
			doc.getDocumentElement().normalize();
			NodeList platform = doc.getElementsByTagName("platform");
			for (int temp = 0; temp < platform.getLength(); temp++) {
				Node platformNode = platform.item(temp);
				Element platformElement = (Element) platformNode;
				NodeList platformTypeList = platformElement.getElementsByTagName("platform-type");
				for (int i = 0; i < platformTypeList.getLength(); i++) {
					if (platformType.equals(platformTypeList.item(i).getTextContent())) {
						Node platformTypeNode = platform.item(temp);
						Element platformTypeElement = (Element) platformTypeNode;
						NodeList serviceList = platformTypeElement.getElementsByTagName("service");
						for (int k = 0; k < serviceList.getLength(); k++) {
							Capability capability = new Capability();
							Node serviceType = serviceList.item(k);
							Element serviceTypeElement = (Element) serviceType;
							String serviceName = serviceTypeElement.getElementsByTagName("name").item(0)
									.getTextContent();
							String serviceTypeValue = serviceTypeElement.getElementsByTagName("type").item(0)
									.getTextContent();
							String packageName = serviceTypeElement.getElementsByTagName("packageName").item(0)
									.getTextContent();
							String enabled = serviceTypeElement.getElementsByTagName("enabled").item(0)
									.getTextContent();
							
							capability.setName(serviceName);
							capability.setEnabled(new Boolean(enabled));
							capability.setPackageName(packageName);
							capability.setRequired("false");

							NodeList nodePropertyList = serviceTypeElement.getElementsByTagName("node");
							List<drdo.cair.isrd.icrs.mcs.entity.platform.Node> serviceNodeList = new ArrayList<>();
							for(int nodeIndex = 0;nodeIndex<nodePropertyList.getLength();nodeIndex++){
								drdo.cair.isrd.icrs.mcs.entity.platform.Node node = new drdo.cair.isrd.icrs.mcs.entity.platform.Node();
								Node nodeElementName = nodePropertyList.item(nodeIndex);
								Element nodeElementProperty = (Element) nodeElementName;
								String nodeName = nodeElementProperty.getElementsByTagName("name").item(0).getTextContent();
								node.setName(nodeName);
								Map<String,List<RequireElement>> paramtersList = new HashMap<>();
								Parameters parameters = new Parameters();
								//Below code is for getting require values
								NodeList requireParametersList = nodeElementProperty.getElementsByTagName("require");	
									List<Requires> requireElementList = new ArrayList<>();
									for(int requireIndex = 0;requireIndex<requireParametersList.getLength();requireIndex++){
										Node requireParameterElement = requireParametersList.item(requireIndex);
										Requires requires = new Requires();
										Element requireParameterElementProperty = (Element) requireParameterElement;
										String requireElementName = requireParameterElementProperty.getElementsByTagName("name").item(0).getTextContent();
										requires.setName(requireElementName);
										Map<String, String> requirelist = new HashMap<>();
										String requireElementRange = requireParameterElementProperty.getElementsByTagName("range").item(0).getTextContent();
										requirelist.put("range", requireElementRange);
										String requireDefaultValue = requireParameterElementProperty.getElementsByTagName("default").item(0).getTextContent();
										requirelist.put("default", requireDefaultValue);
										String requireValueType = requireParameterElementProperty.getElementsByTagName("type").item(0).getTextContent();
										requirelist.put("type", requireValueType);
										requires.setRequireslist(requirelist);
										requireElementList.add(requires);
									}
									parameters.setRequires(requireElementList);
									node.setParameters(parameters);
									serviceNodeList.add(node);
							}
							capability.setNodes(serviceNodeList);
							capabilityListValues.add(capability);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+ " :Exception in reading componentProperties : ", e.getCause());
		}
		return capabilityListValues;
	}

	@Override
	public void archiveTheMissionById(boolean archived, UUID missionConfigId) {
		
		missionConfigDao.archiveTheMissionById(archived, missionConfigId);
		
	}

	@Override
	public List<MissionConfig> getMissionConfigIdsBasedOnMissionName(String missionName) {
		return missionConfigDao.getMissionConfigIdsBasedOnMissionName(missionName);
	}

	@Override
	public Set<String> getReplyMissionsData(String ss) {
		
		Set<String> missionNameSet=new TreeSet<>();
		
		List<String> missionNames=mcsCassandraTemplate.replyMissionsData();
		
		for (String missioname:missionNames)
		{
			missionNameSet.add(missioname);
		}
		
		return missionNameSet;
		
	}

	
	public List<Mission> getMissionData() {
		
		return mcsCassandraTemplate.getMissionData();
	}
	
	@Override
	public List<Mission> getMissionDataByMissionId(UUID missionRunId) {
		
		return mcsCassandraTemplate.getMissionDataByMissionId(missionRunId);
	}

	@Override
	public List<Mission> getmissionReplyRunIds(String missionName) {
		
		List<MissionDataManagement> missionReplyData = mcsCassandraTemplate.replyMissionsRunIds(missionName);
		System.out.println("list"+missionReplyData);
		
		List<UUID> missionRunIdList=missionReplyData.stream().map(MissionDataManagement::getMissionRunId).collect(Collectors.toList());
		
		List<Mission> missionRunningList =getMissionData();
		
		List<Mission> missionAssociatedData=missionRunningList.stream().filter(e->missionRunIdList.contains(e.getMissionId())).collect(Collectors.toList());
		
		return missionAssociatedData;
	}

	@Override
	public List<MissionConfig> getmissionNameListByUser(String username) {
		;
		List<MissionConfig> missionconfig=missionConfigDao.getmissionNameListByUser(username);
		
		return missionconfig;
	}

	@Override
	public List<Mission> getMissionDataByUser(String currentLogin) {
		
		return missionConfigDao.getMissionDataByUser(currentLogin);
	}

	
}
