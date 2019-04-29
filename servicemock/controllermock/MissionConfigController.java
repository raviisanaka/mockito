package drdo.cair.isrd.icrs.mcs.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.Slf4JLoggingSystem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import drdo.cair.isrd.icrs.loggers.CustomLevels;
import drdo.cair.isrd.icrs.mcs.bean.PlatformConfigUI;
import drdo.cair.isrd.icrs.mcs.entity.MapSettings;
import drdo.cair.isrd.icrs.mcs.entity.Mission;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfig;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfigUI;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfigValidator;
import drdo.cair.isrd.icrs.mcs.entity.MissionDataManagement;
import drdo.cair.isrd.icrs.mcs.entity.MissionRunUI;
import drdo.cair.isrd.icrs.mcs.entity.PlatformConfig;
import drdo.cair.isrd.icrs.mcs.entity.PlatformConfigValidator;
import drdo.cair.isrd.icrs.mcs.entity.PlatformServices;
import drdo.cair.isrd.icrs.mcs.entity.PlatformUI;
import drdo.cair.isrd.icrs.mcs.entity.PropertiesUI;
import drdo.cair.isrd.icrs.mcs.entity.RequireElement;
import drdo.cair.isrd.icrs.mcs.entity.Service;
import drdo.cair.isrd.icrs.mcs.entity.ServiceNode;
import drdo.cair.isrd.icrs.mcs.entity.ServiceNodeParameters;
import drdo.cair.isrd.icrs.mcs.entity.ServiceNodeParametersUI;
import drdo.cair.isrd.icrs.mcs.entity.ServiceProperties;
import drdo.cair.isrd.icrs.mcs.entity.ServiceUI;
import drdo.cair.isrd.icrs.mcs.entity.ServicesByPlatform;
import drdo.cair.isrd.icrs.mcs.entity.UpdateMissionConfigValidator;
import drdo.cair.isrd.icrs.mcs.entity.platform.Capabilities;
import drdo.cair.isrd.icrs.mcs.entity.platform.Capability;
import drdo.cair.isrd.icrs.mcs.services.MissionConfigService;
import drdo.cair.isrd.icrs.mcs.services.MissionNameValidator;
import drdo.cair.isrd.icrs.mcs.services.MissionService;
import drdo.cair.isrd.icrs.mcs.services.platform.CapabilitiesDao;
import drdo.cair.isrd.icrs.mcs.services.platform.FileService;
import drdo.cair.isrd.icrs.mcs.validators.PlatformNameValidator;

/**
 * Controller interface for mission configuration activities.
 * 
 * @author user
 *
 */

@RestController
public class MissionConfigController {

	@Autowired
	private MissionConfigService missionConfigService;

	@Autowired
	private MissionConfigValidator missionConfigValidator;

	@Autowired
	private UpdateMissionConfigValidator updateMissionConfigValidator;
	
	@Autowired
	private MissionNameValidator missionNameValidator;
	
	@Autowired
	private PlatformConfigValidator platformConfigValidator;
	
	@Autowired
	private PlatformNameValidator platformNameValidator;

	@Autowired
	CassandraOperations cassandraOperation;
	
	@Autowired
	CapabilitiesDao capabilitiesDao;
	
	@Autowired
	FileService fileService;

	@Autowired
	CustomLevels logLevels;
	
	@InitBinder("missionConfigUi")
	protected void initMissionConfigBinder(WebDataBinder binder) {
		binder.setValidator(missionConfigValidator);
		binder.setValidator(updateMissionConfigValidator);
		binder.setValidator(missionNameValidator);
		
	}
	
	@InitBinder("platformUI")
	protected void initPlatformBinder(WebDataBinder binder) {
		binder.setValidator(platformNameValidator);
		
	}
	
	@InitBinder("platformConfigUI")
	protected void initPlatformConfigBinder(WebDataBinder binder) {
		binder.setValidator(platformConfigValidator);
		
	}
	
	
	@InitBinder("missionConfigUiUpdate")
	protected void initMissionConfigUpdateBinder(WebDataBinder binder) {
		
		binder.setValidator(updateMissionConfigValidator);
	}

	private static final Logger logger = Logger.getLogger(MissionConfigController.class);

	/**
	 * This method is used to read the data from XML to get PlatformTypes
	 * 
	 * @return List
	 */

	@RequestMapping(value = "/platformTypes", method = RequestMethod.GET)
	public List<String> getPlatformTypes() {
		List<String> platformTypes = new ArrayList<>();
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

			NodeList nlist = doc.getElementsByTagName("platform");
			for (int temp = 0; temp < nlist.getLength(); temp++) {
				Node node = nlist.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					platformTypes.add(element.getElementsByTagName("platform-type").item(0).getTextContent());
				}
			}

		} catch (Exception e) {
			logger.log(logLevels.ALARM ,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Exception in getting platformTypes :", e.getCause());
		}
		return platformTypes;
	}

	/**
	 * This method is used to read the data from XML to get the Services and
	 * Service Properties
	 * 
	 * @param platformType
	 * @return {link Services and Service Properties}
	 */
	@RequestMapping(value = "/platformservice/{platformType}", method = RequestMethod.GET)
	public PlatformServices getPlatformServices(@PathVariable("platformType") String platformType) {
		PlatformServices platformServices = new PlatformServices();
		List<Service> serviceListValues = new ArrayList<>();
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
							Service service = new Service();
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
							
							service.setServiceName(serviceName);
							service.setName(serviceName);
							service.setPackageName(packageName);
							service.setServiceType(serviceTypeValue);
							service.setEnabled(new Boolean(enabled));
							//service.setRequired("false");
							NodeList nodePropertyList = serviceTypeElement.getElementsByTagName("node");
							List<ServiceNode> serviceNodeList = new ArrayList<>();
							for(int nodeIndex = 0;nodeIndex<nodePropertyList.getLength();nodeIndex++){
								ServiceNode serviceNode = new ServiceNode();
								Node nodeElementName = nodePropertyList.item(nodeIndex);
								Element nodeElementProperty = (Element) nodeElementName;
								String nodeName = nodeElementProperty.getElementsByTagName("name").item(0).getTextContent();
								serviceNode.setName(nodeName);
								serviceNodeList.add(serviceNode);
								Map<String,List<RequireElement>> paramtersList = new HashMap<>();
		
									//Below code is for getting require values
									NodeList requireParametersList = nodeElementProperty.getElementsByTagName("require");
									
									List<RequireElement> requireElementList = new ArrayList<>();
									for(int requireIndex = 0;requireIndex<requireParametersList.getLength();requireIndex++){
										Node requireParameterElement = requireParametersList.item(requireIndex);
										RequireElement requireelement = new RequireElement();
										Element requireParameterElementProperty = (Element) requireParameterElement;
										String requireElementName = requireParameterElementProperty.getElementsByTagName("name").item(0).getTextContent();
										requireelement.setName(requireElementName);
										Map<String, String> requirelist = new HashMap<>();
										String requireElementRange = requireParameterElementProperty.getElementsByTagName("range").item(0).getTextContent();
										requirelist.put("range", requireElementRange);
										String requireDefaultValue = requireParameterElementProperty.getElementsByTagName("default").item(0).getTextContent();
										requirelist.put("default", requireDefaultValue);
										String requireValueType = requireParameterElementProperty.getElementsByTagName("type").item(0).getTextContent();
										requirelist.put("type", requireValueType);
										
										requireelement.setRequireslist(requirelist);
										requireElementList.add(requireelement);
									}
									paramtersList.put("requires", requireElementList);
									serviceNode.setParameters(paramtersList);
							}
							service.setNodes(serviceNodeList);
							NodeList propertyList = serviceTypeElement.getElementsByTagName("property");
							Map<String, String> properties = new TreeMap<>();

							for (int l = 0; l < propertyList.getLength(); l++) {

								Node propertyName = propertyList.item(l);
								Element prEle = (Element) propertyName;
								String Name = prEle.getElementsByTagName("name").item(0).getTextContent();
								String Value = prEle.getElementsByTagName("value").item(0).getTextContent();
								properties.put(Name, Value);
							}
							service.setProperties(properties);
							service.setServiceYamlName("icrs_diag_interface");
							service.setServiceYamlPackage("icrs_diag");
							service.setRequired("false");
							serviceListValues.add(service);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+ " :Exception in reading componentProperties : ", e.getCause());
		}
		platformServices.setServices(serviceListValues);
		return platformServices;
	}

	/**
	 * User will Configure the Mission Information by Object
	 * 
	 * @param missionConfigUi
	 * @return {@link MissionService}
	 */
	@RequestMapping(value = "/saveMissionAndPlatformData", method = RequestMethod.POST)
	@PreAuthorize("hasRole('CREATE_MISSION')")
	public ResponseEntity insertMissionConfig(@RequestBody MissionConfigUI missionConfigUi, Errors errors) {
		//List<PlatformUI> platformS = missionConfigUi.
		missionConfigValidator.validate(missionConfigUi, errors);
		if (errors.hasErrors()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errors.getAllErrors());
		}
		ArrayList<String> missionConfigData = new ArrayList<>();
		MissionConfig tempMissionConfig = new MissionConfig();
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName +" :values " + missionConfigUi.getMissionName());

		Iterator<PlatformUI> platformIterator = missionConfigUi.getPlatforms().iterator();
		UUID missionConfigId = UUIDs.timeBased();
		tempMissionConfig.setMissionConfig_id(missionConfigId);
		tempMissionConfig.setArchived(missionConfigUi.isArchived());
		tempMissionConfig.setMission_name(missionConfigUi.getMissionName());
		tempMissionConfig.setMission_users(missionConfigUi.getMissionUsers());
		tempMissionConfig.setDescription(missionConfigUi.getMissionDes());
		tempMissionConfig.setCreatedDate(new Date());
		tempMissionConfig.setEnableReplayMission(missionConfigUi.isEnableReplayMission());
		PlatformUI tempPlatform;
		while (platformIterator.hasNext()) {

			tempPlatform = platformIterator.next();
			UUID platformId = UUIDs.timeBased();
			tempMissionConfig.setPlatform_id(platformId);
			tempMissionConfig.setPlatform_name(tempPlatform.getName());
			tempMissionConfig.setPlatform_type(tempPlatform.getType());

			List<ServiceUI> services = tempPlatform.getServices();
			/* Injecting marfLogCapability here
			 * TODO: This has to come from UI during missionConfigSave. To come from UI, we have to read marf capabilities from XML.
			 */
			/*ServiceUI marfLogServiceUI = new ServiceUI();
			marfLogServiceUI.setServiceName("marf_Log_Service_interface");
			marfLogServiceUI.setName("marf_Log_Service_interface");
			marfLogServiceUI.setLogEnabled(true);
			marfLogServiceUI.setRecordEnabled(true);
			marfLogServiceUI.setRequired(true);
			
			List<ServiceNodeParametersUI> ServiceNodeParametersUIList =new ArrayList<>();
			ServiceNodeParametersUI marfServiceNodeParametersUI = new ServiceNodeParametersUI();
			marfServiceNodeParametersUI.setNodeName("marf");
			
			List<PropertiesUI> marfPropertiesUIList = new ArrayList<>();
			PropertiesUI marfPropertiesUI = new PropertiesUI();
			marfPropertiesUI.setName("diagnostics_level");
			marfPropertiesUI.setValue("INFO");
			marfPropertiesUIList.add(marfPropertiesUI);
			
			PropertiesUI marfTracePropertiesUI = new PropertiesUI();
			marfTracePropertiesUI.setName("trace_level");
			marfPropertiesUI.setValue("");
			marfPropertiesUIList.add(marfTracePropertiesUI);
			
			marfServiceNodeParametersUI.setProperties(marfPropertiesUIList);
			
			ServiceNodeParametersUIList.add(marfServiceNodeParametersUI);
			marfLogServiceUI.setServiceNodeList(ServiceNodeParametersUIList);
			services.add(marfLogServiceUI);*/
			//Injecting marfLogCapability end here
			
			
			//add capability for each
			//Capabilities capsByName =  capabilitiesDao.getPlatFromCapabilitiesByName(tempPlatform.getType());
			
			Iterator<ServiceUI> componentIterator = services.iterator();
			ServiceUI tempService;
			while (componentIterator.hasNext()) {
				tempService = componentIterator.next();
				UUID serviceId = UUIDs.timeBased();
				tempMissionConfig.setPlatform_service_id(serviceId);
				tempMissionConfig.setPlatform_serviceName(tempService.getServiceName());
				if (tempService.getServiceName().equalsIgnoreCase("marf_Log_Service_interface")) {
					tempMissionConfig.setPlatform_serviceType(ServiceProperties.CONTINUOUS.name());
				} else {
					tempMissionConfig.setPlatform_serviceType(ServiceProperties.DISCRETE.name());
				}

				tempMissionConfig.setYamlServicename(tempService.getName());
				tempMissionConfig.setYamlServicePackage(tempService.getPackageName());
				tempMissionConfig.setLogEnabled(tempService.isLogEnabled());
				tempMissionConfig.setRecordEnabled(tempService.isRecordEnabled());
				tempMissionConfig.setRequired(tempService.isRequired());
				
				List<ServiceNodeParametersUI> serviceNodeList = tempService.getServiceNodeList();
				Iterator<ServiceNodeParametersUI> serviceNodeListIterator = serviceNodeList.iterator();

				List<UDTValue> udtList = new ArrayList<>();

				while (serviceNodeListIterator.hasNext()) {
					ServiceNodeParametersUI serviceNodeObj = serviceNodeListIterator.next();
					List<PropertiesUI> properiesList = serviceNodeObj.getProperties();

					Map<String, String> serviceNodeProperties = new HashMap<>();

					Iterator<PropertiesUI> propertyListIterator = properiesList.iterator();
					while (propertyListIterator.hasNext()) {
						PropertiesUI propertyObj = propertyListIterator.next();
						serviceNodeProperties.put(propertyObj.getName(), propertyObj.getValue());
					}

					Session session = cassandraOperation.getSession();
					UDTValue udtValue = session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace())
							.getUserType("servicenodeparameters").newValue();
					udtValue.setString("nodeName", serviceNodeObj.getNodeName());
					udtValue.setMap("properties", serviceNodeProperties);
					udtList.add(udtValue);

				}
				
				tempMissionConfig.setServiceNodeParameters(udtList);
				//set cap
				if(tempService.getName() != null) {
					Capabilities caps = fileService.getCapByPlatformType(tempPlatform.getType(), tempService.getName());
					
					Capability cap = caps.getCapabilitylist().isEmpty()?new Capability():caps.getCapabilitylist().get(0);
					ObjectMapper objectMapper = new ObjectMapper();
					
					String capString = null;
					try {
						capString = objectMapper.writeValueAsString(cap);
						
						//Capability fromDb = objectMapper.readValue(capString, Capability.class);
						
					} catch (JsonProcessingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tempMissionConfig.setCapabilityInfo(capString);
					
					try {
					MissionConfig missionConfig = null;
						//save
						missionConfig = missionConfigService.insertMissionConfig(tempMissionConfig);

						missionConfigData.add(missionConfig.toString());
					} catch (Exception e) {
						logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while inserting missionConfiguration data", e.getCause());
						return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
					}
				}
			}
		}
		// List<MissionConfig> missionConfig =
		// missionConfigService.getMissionConfigDataById(missionConfigId);
		ResponseEntity responseEntityObject = new ResponseEntity(HttpStatus.OK);
		return responseEntityObject.status(HttpStatus.OK).body(missionConfigData);
	}
	/**
	 * update the mission configuration details
	 * @param missionConfigUi
	 * @param errors
	 * @return
	 */
	@RequestMapping(value = "/updateMissionAndPlatformData", method = RequestMethod.POST)
	@PreAuthorize("hasRole('CREATE_MISSION')")
	public ResponseEntity updateMissionConfig(@RequestBody MissionConfigUI missionConfigUi, Errors errors) {
 
		//List<PlatformUI> platformS = missionConfigUi.
		//Below statement is for doing validations of mission data  while update the mission
		updateMissionConfigValidator.validate(missionConfigUi, errors);
		//Below code is for if any parameter value is invalid then it throw validation messages
		if (errors.hasErrors()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errors.getAllErrors());
		}
		//End of the code  for if any parameter value is invalid then it throw validation messages
		
		if(missionConfigUi.getMissionConfigId()!=null){
			updateMissionConfigData(missionConfigUi.getMissionConfigId(),missionConfigUi.getMissionName());
		}
		//Below statement is for storing inserted mission data into list object
		ArrayList<String> missionConfigData = new ArrayList<>();
		//Below statement is for creating temporry missionconfig object
		MissionConfig tempMissionConfig = new MissionConfig();
		//Below statement is for iterating list of platforms of particular mission
		Iterator<PlatformUI> platformIterator = missionConfigUi.getPlatforms().iterator();
		
		//Below code is assign values to misssion config common variables data
		UUID missionConfigId = UUIDs.timeBased();
		tempMissionConfig.setMissionConfig_id(missionConfigId);
		tempMissionConfig.setArchived(missionConfigUi.isArchived());
		tempMissionConfig.setMission_name(missionConfigUi.getMissionName());
		tempMissionConfig.setMission_users(missionConfigUi.getMissionUsers());
		tempMissionConfig.setDescription(missionConfigUi.getMissionDes());
		tempMissionConfig.setCreatedDate(new Date());
		tempMissionConfig.setEnableReplayMission(missionConfigUi.isEnableReplayMission());
        //End of the code for assign values to misssion config common variables data
		
		//
		PlatformUI tempPlatform;
		while (platformIterator.hasNext()) {

			tempPlatform = platformIterator.next();
			// int platformId = new Random().nextInt(10000);
			UUID platformId = UUIDs.timeBased();

			tempMissionConfig.setPlatform_id(platformId);
			tempMissionConfig.setPlatform_name(tempPlatform.getName());
			tempMissionConfig.setPlatform_type(tempPlatform.getType());

			List<ServiceUI> services = tempPlatform.getServices();
			/* Injecting marfLogCapability here
			 * TODO: This has to come from UI during missionConfigSave. To come from UI, we have to read marf capabilities from XML.
			 */
			/*ServiceUI marfLogServiceUI = new ServiceUI();
			marfLogServiceUI.setServiceName("marf_Log_Service_interface");
			//marfLogServiceUI.setName(name);
			marfLogServiceUI.setLogEnabled(true);
			marfLogServiceUI.setRecordEnabled(true);
			marfLogServiceUI.setRequired(true);
			
			List<ServiceNodeParametersUI> ServiceNodeParametersUIList =new ArrayList<>();
			ServiceNodeParametersUI marfServiceNodeParametersUI = new ServiceNodeParametersUI();
			marfServiceNodeParametersUI.setNodeName("marf");
			
			List<PropertiesUI> marfPropertiesUIList = new ArrayList<>();
			PropertiesUI marfPropertiesUI = new PropertiesUI();
			marfPropertiesUI.setName("diagnostics_level");
			marfPropertiesUI.setValue("INFO");
			marfPropertiesUIList.add(marfPropertiesUI);
			
			PropertiesUI marfTracePropertiesUI = new PropertiesUI();
			marfTracePropertiesUI.setName("trace_level");
			marfPropertiesUI.setValue("");
			marfPropertiesUIList.add(marfTracePropertiesUI);
			
			marfServiceNodeParametersUI.setProperties(marfPropertiesUIList);
			
			ServiceNodeParametersUIList.add(marfServiceNodeParametersUI);
			marfLogServiceUI.setServiceNodeList(ServiceNodeParametersUIList);
			services.add(marfLogServiceUI);*/
			//Injecting marfLogCapability end here
			
			
			//add capability for each
			//Capabilities capsByName =  capabilitiesDao.getPlatFromCapabilitiesByName(tempPlatform.getType());
			
			Iterator<ServiceUI> componentIterator = services.iterator();
			ServiceUI tempService;
			while (componentIterator.hasNext()) {
				tempService = componentIterator.next();
				UUID serviceId = UUIDs.timeBased();
				tempMissionConfig.setPlatform_service_id(serviceId);
				tempMissionConfig.setPlatform_serviceName(tempService.getServiceName());
				if (tempService.getServiceName().equalsIgnoreCase("marf_Log_Service_interface")) {
					tempMissionConfig.setPlatform_serviceType(ServiceProperties.CONTINUOUS.name());
				} else {
					tempMissionConfig.setPlatform_serviceType(ServiceProperties.DISCRETE.name());
				}

				tempMissionConfig.setYamlServicename(tempService.getName());
				tempMissionConfig.setYamlServicePackage(tempService.getPackageName());
				tempMissionConfig.setLogEnabled(tempService.isLogEnabled());
				tempMissionConfig.setRecordEnabled(tempService.isRecordEnabled());
				tempMissionConfig.setRequired(tempService.isRequired());
				
				List<ServiceNodeParametersUI> serviceNodeList = tempService.getServiceNodeList();
				Iterator<ServiceNodeParametersUI> serviceNodeListIterator = serviceNodeList.iterator();

				List<UDTValue> udtList = new ArrayList<>();

				while (serviceNodeListIterator.hasNext()) {
					ServiceNodeParametersUI serviceNodeObj = serviceNodeListIterator.next();
					List<PropertiesUI> properiesList = serviceNodeObj.getProperties();

					Map<String, String> serviceNodeProperties = new HashMap<>();

					Iterator<PropertiesUI> propertyListIterator = properiesList.iterator();
					while (propertyListIterator.hasNext()) {
						PropertiesUI propertyObj = propertyListIterator.next();
						serviceNodeProperties.put(propertyObj.getName(), propertyObj.getValue());
					}

					Session session = cassandraOperation.getSession();
					UDTValue udtValue = session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace())
							.getUserType("servicenodeparameters").newValue();
					udtValue.setString("nodeName", serviceNodeObj.getNodeName());
					udtValue.setMap("properties", serviceNodeProperties);
					udtList.add(udtValue);

				}
				
				tempMissionConfig.setServiceNodeParameters(udtList);
				
				if(tempService.getName() != null) {
					Capabilities caps = fileService.getCapByPlatformType(tempPlatform.getType(), tempService.getName());
					
					Capability cap = caps.getCapabilitylist().isEmpty()?new Capability():caps.getCapabilitylist().get(0);
					ObjectMapper objectMapper = new ObjectMapper();
					
					String capString = null;
					try {
						capString = objectMapper.writeValueAsString(cap);
						
						//Capability fromDb = objectMapper.readValue(capString, Capability.class);
						
					} catch (JsonProcessingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tempMissionConfig.setCapabilityInfo(capString);
				
				try {
					MissionConfig missionConfig = null;
					missionConfig = missionConfigService.insertMissionConfig(tempMissionConfig);

					missionConfigData.add(missionConfig.toString());
				} catch (Exception e) {
					logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while inserting missionConfiguration data", e.getCause());
					return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				}
			}
		}
		}
		// List<MissionConfig> missionConfig =
		// missionConfigService.getMissionConfigDataById(missionConfigId);
		ResponseEntity responseEntityObject = new ResponseEntity(HttpStatus.OK);
		return responseEntityObject.status(HttpStatus.OK).body(missionConfigData);
	}
	

	@RequestMapping(value = "/importMission", method = RequestMethod.GET)
	public List<MissionConfig> missionConfigListByUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentLogin = authentication.getName();
		return missionConfigService.missionConfigListByUser(currentLogin);

	}

	/**
	 * Imports MissionConfiguration data while editing
	 * 
	 * @param missionConfigId
	 * @return MissionConfigUI
	 */
	@RequestMapping(value = "/editImportMissionConfig/{missionconfig_id}", method = RequestMethod.GET)
	public MissionConfigUI find(@PathVariable("missionconfig_id") UUID missionConfigId) {
		MissionConfigUI missionConfigUIObject = new MissionConfigUI();

		List<MissionConfig> missionConfig = missionConfigService.getMissionConfigDataById(missionConfigId);
		Iterator<MissionConfig> missionConfigIterator = missionConfig.iterator();

		/*
		 * TODO : Because of cassandra table structure for mission config ,
		 * where each service is stored as a row, below logic has been
		 * implemented. To check for better mechanism.
		 */

		MissionConfig tempMissionConfig = null;
		String platformName = null;
		PlatformUI platformUiObj = null;
		List<ServiceUI> serviceUiObjList = null;
		List<PlatformUI> platformUiObjList = null;
		boolean firstRow = true;
		while (missionConfigIterator.hasNext()) {
			tempMissionConfig = missionConfigIterator.next();

			if (firstRow) {
				// Fetch missionConfig general information from first row. Skip
				// for rest of rows.
				missionConfigUIObject.setMissionConfigId(tempMissionConfig.getMissionConfig_id());
				missionConfigUIObject.setMissionName(tempMissionConfig.getMission_name());
				missionConfigUIObject.setMissionDes(tempMissionConfig.getDescription());
				missionConfigUIObject.setMissionUsers(tempMissionConfig.getMission_users());
				Map<String, String> mapData = tempMissionConfig.getMission_MapConfig();

				MapSettings mapSettings = new MapSettings();
				mapSettings.setLatitude(mapData.get("latitude"));
				mapSettings.setLongitude(mapData.get("longiutde"));
				mapSettings.setZoomLevel(Integer.parseInt(mapData.get("zoomLevel")));
				missionConfigUIObject.setMapSettings(mapSettings);

				platformUiObjList = new ArrayList<>();
				missionConfigUIObject.setPlatforms(platformUiObjList);

				firstRow = false;
			}

			ServiceUI serviceUiObj = new ServiceUI();
			serviceUiObj.setServiceId(tempMissionConfig.getPlatform_service_id());
			serviceUiObj.setServiceName(tempMissionConfig.getPlatform_serviceName());
			serviceUiObj.setServiceType(tempMissionConfig.getPlatform_serviceType());
			// serviceUiObj.setProperties(tempMissionConfig.getPlatform_serviceProperties());

			if (tempMissionConfig.getPlatform_name().equals(platformName)) {
				// Service object of existing platform
				serviceUiObjList.add(serviceUiObj);
			} else {
				// Service object of New Platform
				platformUiObj = new PlatformUI();
				platformUiObj.setPlatformId(tempMissionConfig.getPlatform_id());
				platformUiObj.setName(tempMissionConfig.getPlatform_name());
				platformUiObj.setType(tempMissionConfig.getPlatform_type());
				serviceUiObjList = new ArrayList<ServiceUI>();
				serviceUiObjList.add(serviceUiObj);
				platformUiObj.setServices(serviceUiObjList);
				platformUiObjList.add(platformUiObj);
				platformName = tempMissionConfig.getPlatform_name();

			}
		}

		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :missionCOnfigUIObject value............." + missionConfigUIObject);
		return missionConfigUIObject;

	}

	/**
	 * Update the MissionConfiguration data
	 * 
	 * @param missionConfigUi
	 * @param errors
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/updateImportMissionConfig", method = RequestMethod.PUT)
	public ResponseEntity updateMissionConfigDetails(@RequestBody MissionConfigUI missionConfigUi, Errors errors) {
		updateMissionConfigValidator.validate(missionConfigUi, errors);
		if (errors.hasErrors()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errors.getAllErrors());
		}
		List<MissionConfig> missionConfig = missionConfigService
				.getMissionConfigDataById(missionConfigUi.getMissionConfigId());
		List<UUID> platformids = new ArrayList<>();
		List<UUID> serviceids = new ArrayList<>();

		List<UUID> platformUiIds = new ArrayList<>();
		List<UUID> serviceUIids = new ArrayList<>();

		for (MissionConfig tempMissionConfig : missionConfig) {
			platformids.add(tempMissionConfig.getPlatform_id());
			serviceids.add(tempMissionConfig.getPlatform_service_id());
		}
		MissionConfig tempMissionConfig1 = new MissionConfig();
		tempMissionConfig1.setMissionConfig_id(missionConfigUi.getMissionConfigId());
		tempMissionConfig1.setMission_name(missionConfigUi.getMissionName());
		Map<String, String> mapSettings = new HashMap<>();
		mapSettings.put("latitude", missionConfigUi.getMapSettings().getLatitude());
		mapSettings.put("longiutde", missionConfigUi.getMapSettings().getLongitude());
		mapSettings.put("zoomLevel", Integer.toString(missionConfigUi.getMapSettings().getZoomLevel()));
		tempMissionConfig1.setMission_MapConfig(mapSettings);
		tempMissionConfig1.setMission_users(missionConfigUi.getMissionUsers());
		tempMissionConfig1.setDescription(missionConfigUi.getMissionDes());
		tempMissionConfig1.setCreatedDate(new Date());
		List<PlatformUI> platformsList = missionConfigUi.getPlatforms();
		for (PlatformUI platforms : platformsList) {
			// updates the MissionConfiguration data while adding new Platform
			if (!platformids.contains(platforms.getPlatformId())) {
				// int platformId = new Random().nextInt(10000);
				UUID platformId = UUIDs.timeBased();
				tempMissionConfig1.setPlatform_id(platformId);
				tempMissionConfig1.setPlatform_name(platforms.getName());
				tempMissionConfig1.setPlatform_type(platforms.getType());
				List<ServiceUI> servicesList = platforms.getServices();
				for (ServiceUI service : servicesList) {
					// int componentId = new Random().nextInt(10000);
					UUID serviceId = UUIDs.timeBased();
					tempMissionConfig1.setPlatform_service_id(serviceId);
					tempMissionConfig1.setPlatform_serviceName(service.getServiceName());
					tempMissionConfig1.setPlatform_serviceType(service.getServiceType());
					// tempMissionConfig1.setPlatform_serviceProperties(service.getProperties());
					try {
						missionConfigService.updateMissionConfig(tempMissionConfig1);
					} catch (Exception e) {
						logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while inserting missionConfiguration data", e.getCause());

					}
				}
			} else {
				// updates the MissionConfiguration data while existing Platform
				platformUiIds.add(platforms.getPlatformId());
				tempMissionConfig1.setPlatform_id(platforms.getPlatformId());
				tempMissionConfig1.setPlatform_name(platforms.getName());
				tempMissionConfig1.setPlatform_type(platforms.getType());
				List<ServiceUI> servicesList = platforms.getServices();
				for (ServiceUI service : servicesList) {
					tempMissionConfig1.setPlatform_service_id(service.getServiceId());
					tempMissionConfig1.setPlatform_serviceName(service.getServiceName());
					tempMissionConfig1.setPlatform_serviceType(service.getServiceType());
					// tempMissionConfig1.setPlatform_serviceProperties(service.getProperties());
					// updates the MissionConfiguration data while selecting new
					// Capabilities to existing Platform
					if (!serviceids.contains(service.getServiceId())) {
						// int componentId = new Random().nextInt(10000);
						UUID serviceId = UUIDs.timeBased();
						tempMissionConfig1.setPlatform_service_id(serviceId);
						tempMissionConfig1.setPlatform_serviceName(service.getServiceName());
						tempMissionConfig1.setPlatform_serviceType(service.getServiceType());
						// tempMissionConfig1.setPlatform_serviceProperties(service.getProperties());

					} else {
						serviceUIids.add(service.getServiceId());
					}
					try {
						missionConfigService.updateMissionConfig(tempMissionConfig1);
					} catch (Exception e) {
						logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while inserting missionConfiguration data", e.getCause());

					}
				}
			}
		}
		for (UUID missionPlatformId : platformids) {
			// updates the MissionConfiguration data while removing existing
			// Platform
			if (!platformUiIds.contains(missionPlatformId)) {
				try {
					missionConfigService.removeMissionConfig(missionConfigUi.getMissionConfigId(), missionPlatformId);
				} catch (Exception e) {
					logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while inserting missionConfiguration data", e.getCause());
				}
			} else {
				for (UUID missionServiceId : serviceids) {
					// updates the MissionConfiguration data while deselecting
					// new Capabilities to existing Platform
					if (!serviceUIids.contains(missionServiceId)) {
						try {
							missionConfigService.removeMissionConfig(missionConfigUi.getMissionConfigId(),
									missionPlatformId, missionServiceId);
						} catch (Exception e) {
							return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

						}
					}

				}
			}
		}
		ArrayList<String> missionConfigUpdateResponse = new ArrayList<>();
		missionConfigUpdateResponse.add("Mission Configuration Data Updated Successfully!");
		ResponseEntity responseEntityObject = new ResponseEntity(HttpStatus.OK);
		return responseEntityObject.status(HttpStatus.OK).body(missionConfigUpdateResponse);

	}
	/**
	 * to fetch all the mission config data from the database
	 * @return {@link ResponseEntity} contains MissionConfig Data List
	 */
	@RequestMapping(value = "/getAllMissions")
	public ResponseEntity getAllMissionConfigDetails() {
		List<MissionConfigUI> missionConfigUIObjectList = new ArrayList<>();
		List<UUID> missionConfiguidsList = new ArrayList<>();
		try{
		List<MissionConfig> missionConfigList = missionConfigService.getAllMissionConfigDetails();
		for (MissionConfig missionConfig : missionConfigList) {
			// check whether the mission config id exists or not
			if (!missionConfiguidsList.contains(missionConfig.getMissionConfig_id())) {
				missionConfiguidsList.add(missionConfig.getMissionConfig_id());
				MissionConfigUI missionConfigUI = new MissionConfigUI();
				PlatformUI platformUI = null;
				ServiceUI serviceUI = null;
				List<PlatformUI> platformUIObjectList = null;
				List<ServiceUI> serviceUIObjectList = null;
				List<ServiceNodeParametersUI> serviceNodeList = null;
				String platformName = null;
				List<MissionConfig> tempMissionConfig = missionConfigService
						.getMissionConfigDataById(missionConfig.getMissionConfig_id());
				boolean firstRow = true;
				for (MissionConfig missionConfigIterator : tempMissionConfig) {
					if (firstRow) {
						missionConfigUI.setMissionConfigId(missionConfigIterator.getMissionConfig_id());
						missionConfigUI.setMissionName(missionConfigIterator.getMission_name());
						missionConfigUI.setMissionDes(missionConfigIterator.getDescription());
						missionConfigUI.setMissionUsers(missionConfigIterator.getMission_users());
						missionConfigUI.setCreatedDate(missionConfigIterator.getCreatedDate());
						missionConfigUI.setArchived(missionConfigIterator.getArchived());
						missionConfigUI.setEnableReplayMission(missionConfigIterator.isEnableReplayMission());
						platformUIObjectList = new ArrayList<>();
						missionConfigUI.setPlatforms(platformUIObjectList);
						firstRow = false;
					}
					// create object and assign values for ServiceUI class
					serviceUI = new ServiceUI();
					serviceUI.setServiceId(missionConfigIterator.getPlatform_service_id());
					serviceUI.setName(missionConfigIterator.getYamlServicename());
					serviceUI.setPackageName(missionConfigIterator.getYamlServicePackage());
					serviceUI.setServiceName(missionConfigIterator.getPlatform_serviceName());
					serviceUI.setLogEnabled(missionConfigIterator.getLogEnabled());
					serviceUI.setRecordEnabled(missionConfigIterator.getRecordEnabled());
					serviceUI.setRequired(missionConfigIterator.isRequired());
					serviceUI.setServiceType(missionConfigIterator.getPlatform_serviceType());
					serviceNodeList = getServiceNodeParameters(missionConfigIterator);
					serviceUI.setServiceNodeList(serviceNodeList);
					// add ServiceUIobject to services list and assign value to
					// platformUIclass
					if (missionConfigIterator.getPlatform_name().equals(platformName)) {
						serviceUIObjectList.add(serviceUI);
					} else {
						platformUI = new PlatformUI();
						platformUI.setPlatformId(missionConfigIterator.getPlatform_id());
						platformUI.setType(missionConfigIterator.getPlatform_type());
						platformUI.setName(missionConfigIterator.getPlatform_name());
						// create object and assign values for ServiceUI class
						serviceUI = new ServiceUI();
						serviceUI.setLogEnabled(missionConfigIterator.getLogEnabled());
						serviceUI.setName(missionConfigIterator.getYamlServicename());
						serviceUI.setPackageName(missionConfigIterator.getYamlServicePackage());
						serviceUI.setServiceId(missionConfigIterator.getPlatform_service_id());
						serviceUI.setRecordEnabled(missionConfigIterator.getRecordEnabled());
						serviceUI.setRequired(missionConfigIterator.isRequired());
						serviceUI.setServiceName(missionConfigIterator.getPlatform_serviceName());
						serviceUI.setServiceType(missionConfigIterator.getPlatform_serviceType());
						serviceNodeList = new ArrayList<>();
						serviceNodeList = getServiceNodeParameters(missionConfigIterator);
						serviceUI.setServiceNodeList(serviceNodeList);
						// add ServiceUIobject to services list and assign value
						// to platformUIclass
						serviceUIObjectList = new ArrayList<>();
						serviceUIObjectList.add(serviceUI);
						platformUI.setServices(serviceUIObjectList);
						// add PlatformUI objects to platformUIobjectList and
						// assign it to mission config ui
						platformUIObjectList.add(platformUI);
						platformName = missionConfigIterator.getPlatform_name();
					}
				}
				missionConfigUIObjectList.add(missionConfigUI);
			}
		}
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+ " :missionConfigobj : " + missionConfigUIObjectList);
		
		}catch(Exception e){
			logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while reading data from database : "+e.getCause());
			return new ResponseEntity(getJsonString("Error while Fetching missions data from database"),HttpStatus.BAD_REQUEST);
		}
		
		ResponseEntity responseEntityObject = new ResponseEntity(HttpStatus.OK);
		return responseEntityObject.status(HttpStatus.OK).body(missionConfigUIObjectList);
		//return missionConfigUIObjectList;
	}

	public List<ServiceNodeParametersUI> getServiceNodeParameters(MissionConfig missionConfig) {
		List<ServiceNodeParameters> serviceParameterList = new ArrayList<>();
		List<UDTValue> udtServiceParamList = missionConfig.getServiceNodeParameters();
		if (udtServiceParamList != null && !udtServiceParamList.isEmpty()) {
			Iterator<UDTValue> udtvalue = udtServiceParamList.iterator();
			while (udtvalue.hasNext()) {
				UDTValue udtValueObj = udtvalue.next();
				ServiceNodeParameters nodeParameters = new ServiceNodeParameters();
				nodeParameters.setNodeName(udtValueObj.getString("nodename"));
				nodeParameters.setProperties(udtValueObj.getMap("properties", String.class, String.class));
				serviceParameterList.add(nodeParameters);
			}

		}

		List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
		for (ServiceNodeParameters serviceNodeParameters : serviceParameterList) {
			ServiceNodeParametersUI serviceNodeParametersUI = new ServiceNodeParametersUI();
			serviceNodeParametersUI.setNodeName(serviceNodeParameters.getNodeName());
			List<PropertiesUI> propertiesUIList = new ArrayList<>();
			Map<String, String> propertiesList = serviceNodeParameters.getProperties();
			Set<Entry<String, String>> propertiesMap = propertiesList.entrySet();
			Iterator<Entry<String, String>> propertiesMapIterator = propertiesMap.iterator();
			while (propertiesMapIterator.hasNext()) {
				Entry<String, String> propertiesMapEntry = propertiesMapIterator.next();
				PropertiesUI propertiesUI = new PropertiesUI();
				propertiesUI.setName(propertiesMapEntry.getKey());
				propertiesUI.setValue(propertiesMapEntry.getValue());
				propertiesUIList.add(propertiesUI);
			}
			serviceNodeParametersUI.setProperties(propertiesUIList);
			serviceNodeList.add(serviceNodeParametersUI);
		}
		return serviceNodeList;
	}

	@RequestMapping(value = "/missionConfigurations")
	public List<MissionConfigUI> getMissionConfigurationsByCurrentUser() {

		String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
		List<MissionConfig> missionConfigList = missionConfigService.missionConfigListByUser(currentLogin);

		List<UUID> missionConfiguidsList = new ArrayList<>();
		List<MissionConfigUI> missionConfigUIObjectList = new ArrayList<>();
		for (MissionConfig missionConfig : missionConfigList) {
			// check whether the mission config id exists or not
			if (!missionConfiguidsList.contains(missionConfig.getMissionConfig_id())) {
				missionConfiguidsList.add(missionConfig.getMissionConfig_id());
				MissionConfigUI missionConfigUI = new MissionConfigUI();
				PlatformUI platformUI = null;
				ServiceUI serviceUI = null;
				List<PlatformUI> platformUIObjectList = null;
				List<ServiceUI> serviceUIObjectList = null;
				List<ServiceNodeParametersUI> serviceNodeList = null;
				String platformName = null;
				List<MissionConfig> tempMissionConfig = missionConfigService
						.getMissionConfigDataById(missionConfig.getMissionConfig_id());
				boolean firstRow = true;
				for (MissionConfig missionConfigIterator : tempMissionConfig) {
					if (firstRow) {
						missionConfigUI.setMissionConfigId(missionConfigIterator.getMissionConfig_id());
						missionConfigUI.setMissionName(missionConfigIterator.getMission_name());
						missionConfigUI.setMissionDes(missionConfigIterator.getDescription());
						missionConfigUI.setMissionUsers(missionConfigIterator.getMission_users());
						missionConfigUI.setEnableReplayMission(missionConfigIterator.isEnableReplayMission());
						missionConfigUI.setCreatedDate(missionConfigIterator.getCreatedDate());
						platformUIObjectList = new ArrayList<>();
						missionConfigUI.setPlatforms(platformUIObjectList);
						firstRow = false;
					}
					// create object and assign values for ServiceUI class
					serviceUI = new ServiceUI();
					serviceUI.setServiceId(missionConfigIterator.getPlatform_service_id());
					serviceUI.setName(missionConfigIterator.getYamlServicename());
					serviceUI.setPackageName(missionConfigIterator.getYamlServicePackage());
					serviceUI.setServiceName(missionConfigIterator.getPlatform_serviceName());
					serviceUI.setLogEnabled(missionConfigIterator.getLogEnabled());
					serviceUI.setRecordEnabled(missionConfigIterator.getRecordEnabled());
					serviceUI.setServiceType(missionConfigIterator.getPlatform_serviceType());
					serviceNodeList = getServiceNodeParameters(missionConfigIterator);
					serviceUI.setServiceNodeList(serviceNodeList);
					// add ServiceUIobject to services list and assign value to
					// platformUIclass
					if (missionConfigIterator.getPlatform_name().equals(platformName)) {
						serviceUIObjectList.add(serviceUI);
					} else {
						platformUI = new PlatformUI();
						platformUI.setPlatformId(missionConfigIterator.getPlatform_id());
						platformUI.setType(missionConfigIterator.getPlatform_type());
						platformUI.setName(missionConfigIterator.getPlatform_name());
						// create object and assign values for ServiceUI class
						serviceUI = new ServiceUI();
						serviceUI.setLogEnabled(missionConfigIterator.getLogEnabled());
						serviceUI.setName(missionConfigIterator.getYamlServicename());
						serviceUI.setPackageName(missionConfigIterator.getYamlServicePackage());
						serviceUI.setServiceId(missionConfigIterator.getPlatform_service_id());
						serviceUI.setRecordEnabled(missionConfigIterator.getRecordEnabled());
						serviceUI.setServiceName(missionConfigIterator.getPlatform_serviceName());
						serviceUI.setServiceType(missionConfigIterator.getPlatform_serviceType());
						serviceNodeList = new ArrayList<>();
						serviceNodeList = getServiceNodeParameters(missionConfigIterator);
						serviceUI.setServiceNodeList(serviceNodeList);
						// add ServiceUIobject to services list and assign value
						// to platformUIclass
						serviceUIObjectList = new ArrayList<>();
						serviceUIObjectList.add(serviceUI);
						platformUI.setServices(serviceUIObjectList);
						// add PlatformUI objects to platformUIobjectList and
						// assign it to mission config ui
						platformUIObjectList.add(platformUI);
						platformName = missionConfigIterator.getPlatform_name();
					}
				}
				missionConfigUIObjectList.add(missionConfigUI);
			}
		}
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+ ":missionConfigUIObjectList : " + missionConfigUIObjectList);
		return missionConfigUIObjectList;
	}

	
  /**
   * Insert Global platform configuration Data
   * @param platformConfigUI
   * @param errors
   * @return {@link ResponseEntity}
   */
	@RequestMapping(value="/savePlatformConfigData",method=RequestMethod.POST)
	public ResponseEntity<?> savePlatformConfig(@RequestBody PlatformConfigUI platformConfigUI,Errors errors){
		platformConfigValidator.validate(platformConfigUI, errors);
		if (errors.hasErrors()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errors.getAllErrors());
		}
		List<PlatformConfig> platformConfigData = new ArrayList<>();
		PlatformConfig tempPlatformConfig = new PlatformConfig();
		UUID platformConfigId = UUIDs.timeBased();
		tempPlatformConfig.setPlatform_id(platformConfigId);
		tempPlatformConfig.setPlatform_name(platformConfigUI.getName());
		tempPlatformConfig.setPlatform_type(platformConfigUI.getType());
		tempPlatformConfig.setCreatedDate(new Date());
		List<ServiceUI> servicesList = platformConfigUI.getServices();
		for(ServiceUI serviceUI : servicesList){
			UUID serviceId =UUIDs.timeBased();
			tempPlatformConfig.setPlatform_service_id(serviceId);
			tempPlatformConfig.setPlatform_serviceName(serviceUI.getServiceName());
			tempPlatformConfig.setYamlServicePackage(serviceUI.getPackageName());
			tempPlatformConfig.setYamlServicename(serviceUI.getName());
			if(serviceUI.getServiceName().equalsIgnoreCase("marf_Log_Service_interface")){
				tempPlatformConfig.setPlatform_serviceType(ServiceProperties.CONTINUOUS.name());	
			}
			else{
				tempPlatformConfig.setPlatform_serviceType(ServiceProperties.DISCRETE.name());	
			}
			tempPlatformConfig.setLogEnabled(serviceUI.isLogEnabled());
			tempPlatformConfig.setRecordEnabled(serviceUI.isRecordEnabled());
			tempPlatformConfig.setEnabled(serviceUI.isEnabled());
			tempPlatformConfig.setRequired(serviceUI.isRequired());
			List<ServiceNodeParametersUI> serviceNodeList=serviceUI.getServiceNodeList();
	        Iterator<ServiceNodeParametersUI> serviceNodeListIterator=serviceNodeList.iterator();
	        List<UDTValue> udtList=new ArrayList<>();
	        while(serviceNodeListIterator.hasNext())
	        {
	        	ServiceNodeParametersUI serviceNodeObj=serviceNodeListIterator.next();
	        	List<PropertiesUI> properiesList=serviceNodeObj.getProperties();
	        	
	        	Map<String, String> serviceNodeProperties = new HashMap<>();
	        	
		        Iterator<PropertiesUI> propertyListIterator=properiesList.iterator();
		        while(propertyListIterator.hasNext())
		        {
		        	PropertiesUI propertyObj=propertyListIterator.next();
		        	serviceNodeProperties.put(propertyObj.getName(),propertyObj.getValue());
		        }
		        
		        
		        Session session=cassandraOperation.getSession();
				UDTValue udtValue=session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace()).getUserType("servicenodeparameters").newValue();
				udtValue.setString("nodeName",serviceNodeObj.getNodeName());
				udtValue.setMap("properties",serviceNodeProperties);
				udtList.add(udtValue);
	        }
			tempPlatformConfig.setServiceNodeParameters(udtList);
			try {
				PlatformConfig platformConfig = null;
				platformConfig = missionConfigService.insertPlatformConfig(tempPlatformConfig);
				
				platformConfigData.add(platformConfig);
			} catch (Exception e) {
				logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error while inserting missionConfiguration data", e.getCause());
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(platformConfigData, HttpStatus.OK);	
	}
	/**
	 * to fetch the global platform data from database
	 * @return {@link List}{@link PlatformConfigUI}
	 */
	@RequestMapping(value="/getPlatformConfigData",method=RequestMethod.GET)
	public List<PlatformConfigUI> getAllPlatformConfigUIData(){
		List<PlatformConfig> platformConfigList = missionConfigService.getAllPlatformConfigUIData();
		List<PlatformConfigUI> platformConfigUIsList = new ArrayList<>();
		List<UUID> platformConfigsUIIds = new ArrayList<>();
		for (PlatformConfig platformConfig : platformConfigList) {
			if(!platformConfigsUIIds.contains(platformConfig.getPlatform_id())){
				platformConfigsUIIds.add(platformConfig.getPlatform_id());
				PlatformConfigUI platformConfigUI =  new PlatformConfigUI();
				ServiceUI serviceUI = null;
				List<ServiceUI> serviceUIObjectList = null;
				List<ServiceNodeParametersUI> serviceNodeList = null;
				List<PlatformConfig> tempPlatformConfig = missionConfigService.getPlatformConfigDataById(platformConfig.getPlatform_id());
				boolean firstRow = true;
				for(PlatformConfig platformConfigIterator : tempPlatformConfig){
					    if(firstRow){
						platformConfigUI.setPlatformId(platformConfigIterator.getPlatform_id());
						platformConfigUI.setName(platformConfigIterator.getPlatform_name());
						platformConfigUI.setType(platformConfigIterator.getPlatform_type());
						platformConfigUI.setCreatedDate(platformConfigIterator.getCreatedDate());
						serviceUIObjectList = new ArrayList<>();
						platformConfigUI.setServices(serviceUIObjectList);
						firstRow = false;
					    }
					    serviceUI = new ServiceUI();
					    serviceUI.setServiceId(platformConfigIterator.getPlatform_service_id());
					    serviceUI.setServiceName(platformConfigIterator.getPlatform_serviceName());
					    serviceUI.setName(platformConfigIterator.getYamlServicename());
					    serviceUI.setPackageName(platformConfigIterator.getYamlServicePackage());
					    serviceUI.setRequired(platformConfigIterator.isRequired());
					    serviceUI.setServiceType(platformConfigIterator.getPlatform_serviceType());
					    serviceUI.setLogEnabled(platformConfigIterator.isLogEnabled());
					    serviceUI.setRecordEnabled(platformConfigIterator.isRecordEnabled());
					    serviceUI.setEnabled(platformConfigIterator.isEnabled());
					    serviceNodeList = new ArrayList<>();
						 serviceNodeList= getServiceNodeParametersforPlatformConfig(platformConfigIterator);
						 serviceUI.setServiceNodeList(serviceNodeList);
					    serviceUIObjectList.add(serviceUI);
				}
				platformConfigUIsList.add(platformConfigUI);	
			}
		}
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :platform config list : "+platformConfigUIsList);
		return platformConfigUIsList;	
	}
	
	public List<ServiceNodeParametersUI> getServiceNodeParametersforPlatformConfig(PlatformConfig platformConfig){
		List<ServiceNodeParameters> serviceParameterList=new ArrayList<>();
		List<UDTValue> udtServiceParamList=platformConfig.getServiceNodeParameters();
		if(udtServiceParamList!=null&&!udtServiceParamList.isEmpty())
		{
			Iterator<UDTValue>  udtvalue=udtServiceParamList.iterator();
			while(udtvalue.hasNext())
			{
				UDTValue udtValueObj=udtvalue.next();
				ServiceNodeParameters nodeParameters=new ServiceNodeParameters();
				nodeParameters.setNodeName(udtValueObj.getString("nodename"));
				nodeParameters.setProperties(udtValueObj.getMap("properties",String.class,String.class));
				serviceParameterList.add(nodeParameters);
			}
			
		}
		
		List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
		for(ServiceNodeParameters serviceNodeParameters : serviceParameterList){
			ServiceNodeParametersUI serviceNodeParametersUI = new ServiceNodeParametersUI();
			serviceNodeParametersUI.setNodeName(serviceNodeParameters.getNodeName());
			List<PropertiesUI> propertiesUIList = new ArrayList<>();
			Map<String, String> propertiesList = serviceNodeParameters.getProperties();
			Set<Entry<String, String>> propertiesMap = propertiesList.entrySet();
			Iterator<Entry<String, String>> propertiesMapIterator = propertiesMap.iterator();
			while (propertiesMapIterator.hasNext()) {
				Entry<String, String> propertiesMapEntry = propertiesMapIterator.next();
				PropertiesUI propertiesUI = new PropertiesUI();
				propertiesUI.setName(propertiesMapEntry.getKey());
				propertiesUI.setValue(propertiesMapEntry.getValue());
				propertiesUIList.add(propertiesUI);
			}
			serviceNodeParametersUI.setProperties(propertiesUIList);
			serviceNodeList.add(serviceNodeParametersUI);
		}
		return serviceNodeList;
	}

	@RequestMapping(value = "/runningMission")
	public List<MissionRunUI> getAllRunningMissions() {
		List<UUID> missionRunUidsList = new ArrayList<>();
		List<MissionRunUI> missionRunUIObjectList = new ArrayList<>();

		List<Mission> missionRunningList = missionConfigService.getAllRunningMissions();
		//iterating runningMissionsList data
		for(Mission runningMission : missionRunningList) {
			// check whether the current iterated mission already checked-in or not
			if (!missionRunUidsList.contains(runningMission.getMissionId())) {
				missionRunUidsList.add(runningMission.getMissionId());
				
				MissionRunUI missionRunUI = new MissionRunUI();
				PlatformUI platformUI = null;
				ServiceUI serviceUI = null;
				List<PlatformUI> platformUIObjectList = new ArrayList<>();
				List<ServiceUI> serviceUIObjectList = new ArrayList<>();
				List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
				List<String> platformIdsList = new ArrayList<>();
				//fetching running mission data by missionId
				List<Mission> tempMissionRun = missionConfigService.getMissionRunDataById(runningMission.getMissionId());
				boolean firstRow = true;
				for (Mission missionRunIterator : tempMissionRun) {
					if (firstRow) {
						missionRunUI.setMissionRunId(missionRunIterator.getMissionId());
						missionRunUI.setMissionConfigId(missionRunIterator.getMissionConfigId());
						missionRunUI.setMissionName(missionRunIterator.getMissionName());
						missionRunUI.setMissionDes(missionRunIterator.getUserComment());
						missionRunUI.setMissionUsers(missionRunIterator.getMission_users());
						missionRunUI.setMissionStartDate(missionRunIterator.getMissionStartDate());
						missionRunUI.setMissionStatus(missionRunIterator.getMissionStatus());
						//platformUIObjectList = new ArrayList<>();
						//missionRunUI.setPlatforms(platformUIObjectList);
						platformUI = new PlatformUI();
						//platformUI.setPlatformId(UUID.fromString(missionRunIterator.getPlatfo));
						platformIdsList.add(missionRunIterator.getPlatformHostname());
						platformUI.setType(missionRunIterator.getPlatformType());
						platformUI.setName(missionRunIterator.getPlatformConfigHostName());
						
						serviceUIObjectList = new ArrayList<>();
						List<UUID> platformServiceIds = missionRunIterator.getPlatformServices();
						for (UUID serviceId : platformServiceIds) {
							ServicesByPlatform serviceObj = missionConfigService.getPlatformServiceDataById(serviceId);
							// create object and assign values for ServiceUI class
							serviceUI = new ServiceUI();
							serviceUI.setServiceId(serviceObj.getServiceId());
							serviceUI.setServiceName(serviceObj.getServiceName());
							serviceUI.setName(serviceObj.getServiceName().replace("/", "_"));
							serviceUI.setServiceType(serviceObj.getServiceType());
							serviceUI.setLogEnabled(serviceObj.isLogEnabled());
							//fetching serviceNodeParameters for a running mission.
							serviceNodeList = getRunningMissionServiceNodeParameters(serviceObj);
							serviceUI.setServiceNodeList(serviceNodeList);
							serviceUI.setServiceNodeList(serviceNodeList);
							// add ServiceUIobject to services list and assign value to platformUIclass
							serviceUIObjectList.add(serviceUI);
						}
						platformUI.setServices(serviceUIObjectList);
						//Add PlatformUI objects to platformUIobjectList and assign it to mission run ui.
						platformUIObjectList.add(platformUI);
						//added code  for checking
						firstRow = false;
					}
					if (!platformIdsList.contains(missionRunIterator.getPlatformHostname())) {
						platformUI = new PlatformUI();
						//platformUI.setPlatformId(UUID.fromString(missionRunIterator.getPlatformHostname()));
						platformIdsList.add(missionRunIterator.getPlatformHostname());
						platformUI.setType(missionRunIterator.getPlatformType());
						platformUI.setName(missionRunIterator.getPlatformConfigHostName());
						
						serviceUIObjectList = new ArrayList<>();
						List<UUID> platformServiceIds = missionRunIterator.getPlatformServices();
						for (UUID serviceId : platformServiceIds) {
							ServicesByPlatform serviceObj = missionConfigService.getPlatformServiceDataById(serviceId);
							// create object and assign values for ServiceUI class
							serviceUI = new ServiceUI();
							serviceUI.setServiceId(serviceObj.getServiceId());
							serviceUI.setServiceName(serviceObj.getServiceName());
							serviceUI.setServiceType(serviceObj.getServiceType());
							serviceUI.setLogEnabled(serviceObj.isLogEnabled());
							//fetching serviceNodeParameters for a running mission.
							serviceNodeList = getRunningMissionServiceNodeParameters(serviceObj);
							serviceUI.setServiceNodeList(serviceNodeList);
							serviceUI.setServiceNodeList(serviceNodeList);
							// add ServiceUIobject to services list and assign value to platformUIclass
							serviceUIObjectList.add(serviceUI);
						}
						platformUI.setServices(serviceUIObjectList);
						//Add PlatformUI objects to platformUIobjectList and assign it to mission run ui.
						platformUIObjectList.add(platformUI);
					}
					missionRunUI.setPlatforms(platformUIObjectList);
				}
				missionRunUIObjectList.add(missionRunUI);
			}
		}
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :missionRunObjLists..... : " + missionRunUIObjectList);
		return missionRunUIObjectList;
	}
	
	
	/**
	 * fetch the serviceNode parameter values based on missionconfig
	 * Service
	 * @param serviceObj
	 * @return
	 */
	public List<ServiceNodeParametersUI> getRunningMissionServiceNodeParameters(ServicesByPlatform serviceObj) {
		List<ServiceNodeParameters> serviceParameterList = new ArrayList<>();
		List<UDTValue> udtServiceParamList = serviceObj.getServiceNodeParameters();
		if (udtServiceParamList != null && !udtServiceParamList.isEmpty()) {
			Iterator<UDTValue> udtvalue = udtServiceParamList.iterator();
			while (udtvalue.hasNext()) {
				UDTValue udtValueObj = udtvalue.next();
				ServiceNodeParameters nodeParameters = new ServiceNodeParameters();
				nodeParameters.setNodeName(udtValueObj.getString("nodename"));
				nodeParameters.setProperties(udtValueObj.getMap("properties", String.class, String.class));
				serviceParameterList.add(nodeParameters);
			}

		}
		List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
		for (ServiceNodeParameters serviceNodeParameters : serviceParameterList) {
			ServiceNodeParametersUI serviceNodeParametersUI = new ServiceNodeParametersUI();
			serviceNodeParametersUI.setNodeName(serviceNodeParameters.getNodeName());
			List<PropertiesUI> propertiesUIList = new ArrayList<>();
			Map<String, String> propertiesList = serviceNodeParameters.getProperties();
			Set<Entry<String, String>> propertiesMap = propertiesList.entrySet();
			Iterator<Entry<String, String>> propertiesMapIterator = propertiesMap.iterator();

			while(propertiesMapIterator.hasNext())
			{

				Entry<String, String> propertiesMapEntry = propertiesMapIterator.next();
				PropertiesUI propertiesUI = new PropertiesUI();
				propertiesUI.setName(propertiesMapEntry.getKey());
				propertiesUI.setValue(propertiesMapEntry.getValue());
				propertiesUIList.add(propertiesUI);
			}
			serviceNodeParametersUI.setProperties(propertiesUIList);
			serviceNodeList.add(serviceNodeParametersUI);
		}
		return serviceNodeList;
	}
	/**
	 * delete existing global platform data based on id
	 * @param id
	 * @return ResponseEntity status
	 */
	@RequestMapping(value = "/deleteGlobalPlatform/{platformId}", method = RequestMethod.DELETE)
	public ResponseEntity delete(@PathVariable("platformId") UUID platformId) {
		missionConfigService.deleteGlobalPlatformConfigDataById(platformId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	/**
	 * archive the existing mission configuration data based on mission config id
	 * in delete mode
	 * @param missionConfigId
	 * @return ResponseEntity status
	 */
	@RequestMapping(value = "/deleteMission/{missionConfigId}")
	@PreAuthorize("hasRole('DELETE_MISSION')")
	public ResponseEntity deleteMission(@PathVariable("missionConfigId") UUID missionConfigId){
		missionConfigService.updateMissionConfigDataById(true, missionConfigId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
     
	/**
	 * update mission config archived value based on existing MissionId
	 * @param missionConfigId
	 */
	public void updateMissionConfigData(UUID missionConfigId,String missionName){
	//missionConfigService.updateMissionConfigDataById(true, missionConfigId);
		List<MissionConfig> missionConfigDataList = missionConfigService.getMissionConfigDataById(missionConfigId);
		int archivedMissionRows = 0;
		for(MissionConfig missionConfig : missionConfigDataList){
			if(missionConfig.getArchived())
				archivedMissionRows++;
		}
		if(archivedMissionRows==missionConfigDataList.size()){
			List<MissionConfig> missionConfigData = missionConfigService.getMissionConfigIdsBasedOnMissionName(missionName);
			missionConfigId = missionConfigData.get(0).getMissionConfig_id();
		}
		missionConfigService.archiveTheMissionById(true, missionConfigId);
		
	}
	
	@RequestMapping(value="/missionNameValidation")
	public ResponseEntity validateMissionNameDetails(@RequestBody MissionConfigUI missionConfigUI,Errors errors){
	
		missionNameValidator.validate(missionConfigUI, errors);
		if(errors.hasErrors()){
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errors.getAllErrors());
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(value="/platformNameValidation")
	public ResponseEntity validateMissionPlatformNameDetails(@RequestBody PlatformUI missionPlatformUI,Errors errors){
		platformNameValidator.validate(missionPlatformUI, errors);
		/*missionNameValidator.validate(missionConfigUI, errors);*/
		if(errors.hasErrors()){
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errors.getAllErrors());
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * fetch the platform types from platform management
	 * @return {@link ResponseEntity Capabilities}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/getPlatformTypes")
	public ResponseEntity<?> getAllPlatformCapabilities() {
		List<Capability> capabilities = missionConfigService.getPlatformServices("Marf");
		try {
			ArrayList<Capabilities> platformCpasList = (ArrayList<Capabilities>) capabilitiesDao
					.getAllPlatformCapabilities();
			for(Capabilities platformTypes : platformCpasList){
				List<Capability> capabilityList = platformTypes.getCapabilitylist();
				for(Capability capability : capabilities){
				capabilityList.add(capability);
				}
			}
			return new ResponseEntity(platformCpasList, HttpStatus.OK);
		} catch (Exception e) {
			logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Error due to: "+e.getMessage());
			return new ResponseEntity(getJsonString("Unable to fetch platforms"),HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@RequestMapping(value = "/getReplyMissionNames", method = RequestMethod.GET)
	public Set<String> getReplyMissionNames() {
		String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
		
		return missionConfigService.getReplyMissionsData(currentLogin);
	}
	
	@RequestMapping(value = "/getReplyMissionRunIds/{missionName}", method = RequestMethod.GET)
	public List<Mission> getReplyMissionRunIds(@PathVariable("missionName") String missionName) {
		
		return missionConfigService.getmissionReplyRunIds(missionName);
	}
	
	@RequestMapping(value = "/getReplyMissionData/{missionRunId}")
	public List<MissionRunUI> getReplyMissionsData(@PathVariable("missionRunId") String missionRunId) {
		
		List<UUID> missionRunUidsList = new ArrayList<>();
		List<MissionRunUI> missionRunUIObjectList = new ArrayList<>();
		
		/*List<MissionDataManagement> missionReplyData = missionConfigService.getReplyMissionsData(currentLogin);
		
		List<UUID> missionRunIdList=missionReplyData.stream().map(MissionDataManagement::getMissionRunId).collect(Collectors.toList());
		
		List<Mission> missionRunningList = missionConfigService.getMissionDataByUser(currentLogin);
		
		List<Mission> missionAssociatedData=missionRunningList.stream().filter(e->missionRunIdList.contains(e.getMissionId())).collect(Collectors.toList());
		*/
		List<Mission> selectedReplyMissionList=missionConfigService.getMissionDataByMissionId(UUID.fromString(missionRunId));
		//iterating runningMissionsList data
		for(Mission runningMission : selectedReplyMissionList) {
			// check whether the current iterated mission already checked-in or not
			if (!missionRunUidsList.contains(runningMission.getMissionId())) {
				missionRunUidsList.add(runningMission.getMissionId());
				
				MissionRunUI missionRunUI = new MissionRunUI();
				PlatformUI platformUI = null;
				ServiceUI serviceUI = null;
				List<PlatformUI> platformUIObjectList = new ArrayList<>();
				List<ServiceUI> serviceUIObjectList = new ArrayList<>();
				List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
				List<String> platformIdsList = new ArrayList<>();
				//fetching running mission data by missionId
				List<Mission> tempMissionRun = missionConfigService.getMissionRunDataById(runningMission.getMissionId());
				boolean firstRow = true;
				for (Mission missionRunIterator : tempMissionRun) {
					if (firstRow) {
						missionRunUI.setMissionRunId(missionRunIterator.getMissionId());
						missionRunUI.setMissionConfigId(missionRunIterator.getMissionConfigId());
						missionRunUI.setMissionName(missionRunIterator.getMissionName());
						missionRunUI.setMissionDes(missionRunIterator.getUserComment());
						missionRunUI.setMissionUsers(missionRunIterator.getMission_users());
						missionRunUI.setMissionStartDate(missionRunIterator.getMissionStartDate());
						missionRunUI.setMissionEndDate(missionRunIterator.getMissionEndDate());
						missionRunUI.setMissionStatus(missionRunIterator.getMissionStatus());
						//platformUIObjectList = new ArrayList<>();
						//missionRunUI.setPlatforms(platformUIObjectList);
						platformUI = new PlatformUI();
						//platformUI.setPlatformId(UUID.fromString(missionRunIterator.getPlatfo));
						platformIdsList.add(missionRunIterator.getPlatformHostname());
						platformUI.setType(missionRunIterator.getPlatformType());
						platformUI.setName(missionRunIterator.getPlatformConfigHostName());
						
						serviceUIObjectList = new ArrayList<>();
						List<UUID> platformServiceIds = missionRunIterator.getPlatformServices();
						for (UUID serviceId : platformServiceIds) {
							ServicesByPlatform serviceObj = missionConfigService.getPlatformServiceDataById(serviceId);
							// create object and assign values for ServiceUI class
							serviceUI = new ServiceUI();
							serviceUI.setServiceId(serviceObj.getServiceId());
							serviceUI.setServiceName(serviceObj.getServiceName());
							serviceUI.setName(serviceObj.getServiceName().replace("/", "_"));
							serviceUI.setServiceType(serviceObj.getServiceType());
							serviceUI.setLogEnabled(serviceObj.isLogEnabled());
							//fetching serviceNodeParameters for a running mission.
							serviceNodeList = getRunningMissionServiceNodeParameters(serviceObj);
							serviceUI.setServiceNodeList(serviceNodeList);
							serviceUI.setServiceNodeList(serviceNodeList);
							// add ServiceUIobject to services list and assign value to platformUIclass
							serviceUIObjectList.add(serviceUI);
						}
						platformUI.setServices(serviceUIObjectList);
						//Add PlatformUI objects to platformUIobjectList and assign it to mission run ui.
						platformUIObjectList.add(platformUI);
						//added code  for checking
						firstRow = false;
					}
					if (!platformIdsList.contains(missionRunIterator.getPlatformHostname())) {
						platformUI = new PlatformUI();
						//platformUI.setPlatformId(UUID.fromString(missionRunIterator.getPlatformHostname()));
						platformIdsList.add(missionRunIterator.getPlatformHostname());
						platformUI.setType(missionRunIterator.getPlatformType());
						platformUI.setName(missionRunIterator.getPlatformConfigHostName());
						
						serviceUIObjectList = new ArrayList<>();
						List<UUID> platformServiceIds = missionRunIterator.getPlatformServices();
						for (UUID serviceId : platformServiceIds) {
							ServicesByPlatform serviceObj = missionConfigService.getPlatformServiceDataById(serviceId);
							// create object and assign values for ServiceUI class
							serviceUI = new ServiceUI();
							serviceUI.setServiceId(serviceObj.getServiceId());
							serviceUI.setServiceName(serviceObj.getServiceName());
							serviceUI.setServiceType(serviceObj.getServiceType());
							serviceUI.setLogEnabled(serviceObj.isLogEnabled());
							//fetching serviceNodeParameters for a running mission.
							serviceNodeList = getRunningMissionServiceNodeParameters(serviceObj);
							serviceUI.setServiceNodeList(serviceNodeList);
							serviceUI.setServiceNodeList(serviceNodeList);
							// add ServiceUIobject to services list and assign value to platformUIclass
							serviceUIObjectList.add(serviceUI);
						}
						platformUI.setServices(serviceUIObjectList);
						//Add PlatformUI objects to platformUIobjectList and assign it to mission run ui.
						platformUIObjectList.add(platformUI);
					}
					missionRunUI.setPlatforms(platformUIObjectList);
				}
				missionRunUIObjectList.add(missionRunUI);
			}
		}
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :missionRunObjLists..... : " + missionRunUIObjectList);
		return missionRunUIObjectList;
	}
	/**
	 * convert normal string value to JSON string 
	 * @param javaObject
	 * @return JsonString data
	 */
	public String getJsonString(Object javaObject) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(javaObject);
			logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :json string value...........{}"+  jsonString);
		} catch (Exception e) {
			logger.log(logLevels.ALARM,logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+" :Exception converting to JSON : ", e);
		}
		return jsonString;
	} 
	

	/*@RequestMapping(value = "/getmissionReplyConfigurationNames")
	public List<MissionConfig> getmissionReplyConfigurationNames() {

		String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

		List<MissionConfig> missionObj = missionConfigService.getmissionNameListByUser(currentLogin);
		
		return missionObj;
	}*/

	@RequestMapping(value = "/missionReplyConfigurations")
	public List<MissionConfigUI> getmissionReplyConfigurationsByCurrentUser() {

		String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
		List<MissionConfig> missionConfigList = missionConfigService.getmissionNameListByUser(currentLogin);

		List<UUID> missionConfiguidsList = new ArrayList<>();
		List<MissionConfigUI> missionConfigUIObjectList = new ArrayList<>();
		for (MissionConfig missionConfig : missionConfigList) {
			// check whether the mission config id exists or not
			if (!missionConfiguidsList.contains(missionConfig.getMissionConfig_id())) {
				missionConfiguidsList.add(missionConfig.getMissionConfig_id());
				MissionConfigUI missionConfigUI = new MissionConfigUI();
				PlatformUI platformUI = null;
				ServiceUI serviceUI = null;
				List<PlatformUI> platformUIObjectList = null;
				List<ServiceUI> serviceUIObjectList = null;
				List<ServiceNodeParametersUI> serviceNodeList = null;
				String platformName = null;
				List<MissionConfig> tempMissionConfig = missionConfigService
						.getMissionConfigDataById(missionConfig.getMissionConfig_id());
				boolean firstRow = true;
				for (MissionConfig missionConfigIterator : tempMissionConfig) {
					if (firstRow) {
						missionConfigUI.setMissionConfigId(missionConfigIterator.getMissionConfig_id());
						missionConfigUI.setMissionName(missionConfigIterator.getMission_name());
						missionConfigUI.setMissionDes(missionConfigIterator.getDescription());
						missionConfigUI.setMissionUsers(missionConfigIterator.getMission_users());
						missionConfigUI.setEnableReplayMission(missionConfigIterator.isEnableReplayMission());
						missionConfigUI.setCreatedDate(missionConfigIterator.getCreatedDate());
						platformUIObjectList = new ArrayList<>();
						missionConfigUI.setPlatforms(platformUIObjectList);
						firstRow = false;
					}
					// create object and assign values for ServiceUI class
					serviceUI = new ServiceUI();
					serviceUI.setServiceId(missionConfigIterator.getPlatform_service_id());
					serviceUI.setName(missionConfigIterator.getYamlServicename());
					serviceUI.setPackageName(missionConfigIterator.getYamlServicePackage());
					serviceUI.setServiceName(missionConfigIterator.getPlatform_serviceName());
					serviceUI.setLogEnabled(missionConfigIterator.getLogEnabled());
					serviceUI.setRecordEnabled(missionConfigIterator.getRecordEnabled());
					serviceUI.setServiceType(missionConfigIterator.getPlatform_serviceType());
					serviceNodeList = getServiceNodeParameters(missionConfigIterator);
					serviceUI.setServiceNodeList(serviceNodeList);
					// add ServiceUIobject to services list and assign value to
					// platformUIclass
					if (missionConfigIterator.getPlatform_name().equals(platformName)) {
						serviceUIObjectList.add(serviceUI);
					} else {
						platformUI = new PlatformUI();
						platformUI.setPlatformId(missionConfigIterator.getPlatform_id());
						platformUI.setType(missionConfigIterator.getPlatform_type());
						platformUI.setName(missionConfigIterator.getPlatform_name());
						// create object and assign values for ServiceUI class
						serviceUI = new ServiceUI();
						serviceUI.setLogEnabled(missionConfigIterator.getLogEnabled());
						serviceUI.setName(missionConfigIterator.getYamlServicename());
						serviceUI.setPackageName(missionConfigIterator.getYamlServicePackage());
						serviceUI.setServiceId(missionConfigIterator.getPlatform_service_id());
						serviceUI.setRecordEnabled(missionConfigIterator.getRecordEnabled());
						serviceUI.setServiceName(missionConfigIterator.getPlatform_serviceName());
						serviceUI.setServiceType(missionConfigIterator.getPlatform_serviceType());
						serviceNodeList = new ArrayList<>();
						serviceNodeList = getServiceNodeParameters(missionConfigIterator);
						serviceUI.setServiceNodeList(serviceNodeList);
						// add ServiceUIobject to services list and assign value
						// to platformUIclass
						serviceUIObjectList = new ArrayList<>();
						serviceUIObjectList.add(serviceUI);
						platformUI.setServices(serviceUIObjectList);
						// add PlatformUI objects to platformUIobjectList and
						// assign it to mission config ui
						platformUIObjectList.add(platformUI);
						platformName = missionConfigIterator.getPlatform_name();
					}
				}
				missionConfigUIObjectList.add(missionConfigUI);
			}
		}
		logger.info(logLevels.mcsHostName+"."+logLevels.mcsLogServiceName+ ":missionConfigUIObjectList : " + missionConfigUIObjectList);
		return missionConfigUIObjectList;
	}
	
}