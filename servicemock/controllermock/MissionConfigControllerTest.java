package drdo.cair.isrd.icrs.mcs.controller;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.UserTypeResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Frozen;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import drdo.cair.isrd.icrs.loggers.CustomLevels;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfig;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfigUI;
import drdo.cair.isrd.icrs.mcs.entity.MissionConfigValidator;
import drdo.cair.isrd.icrs.mcs.entity.PlatformUI;
import drdo.cair.isrd.icrs.mcs.entity.PropertiesUI;
import drdo.cair.isrd.icrs.mcs.entity.Service;
import drdo.cair.isrd.icrs.mcs.entity.ServiceNodeParametersUI;
import drdo.cair.isrd.icrs.mcs.entity.ServiceUI;
import drdo.cair.isrd.icrs.mcs.entity.UpdateMissionConfigValidator;
import drdo.cair.isrd.icrs.mcs.entity.platform.Capabilities;
import drdo.cair.isrd.icrs.mcs.entity.platform.Capability;
import drdo.cair.isrd.icrs.mcs.entity.platform.Node;
import drdo.cair.isrd.icrs.mcs.entity.platform.Parameters;
import drdo.cair.isrd.icrs.mcs.entity.platform.Provides;
import drdo.cair.isrd.icrs.mcs.entity.platform.Requires;
import drdo.cair.isrd.icrs.mcs.entity.platform.Services;
import drdo.cair.isrd.icrs.mcs.entity.platform.Topics;
import drdo.cair.isrd.icrs.mcs.services.MissionConfigService;
import drdo.cair.isrd.icrs.mcs.services.platform.CapabilitiesDao;
import drdo.cair.isrd.icrs.mcs.services.platform.FileService;
@RunWith(MockitoJUnitRunner.class)
public class MissionConfigControllerTest {
	@Mock
	MissionConfigService missionConfigService;
	@Mock
	MissionConfigValidator missionConfigValidator;
	@Mock
	UpdateMissionConfigValidator updateMissionConfigValidator;
	@Mock
	CapabilitiesDao capabilitiesDao;
	@Mock
	CassandraOperations cassandraOperation;
	@Mock
	Session session;
	@Mock
	Cluster cluster;
	@Mock
	Metadata metadata;
	@Mock
	KeyspaceMetadata keySpace;
	@Mock
	UserType userType;
	@Mock	
	SecurityContextHolder securityContextHolder;
	@Mock
	Authentication authentication;
	@Mock
	SecurityContext securityContext;
	@Mock
	CustomLevels logLevels;
	@Mock
	UDTValue udtValue;
	@Mock
	FileService fileService;
	@Mock
	Errors errors;
	@Spy
	@InjectMocks
	MissionConfigController missionConfigController;
	
	
	MockMvc mockMvc;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(cassandraOperation.getSession()).thenReturn(session);
		Mockito.when(session.getCluster()).thenReturn(cluster);
		Mockito.when(cluster.getMetadata()).thenReturn(metadata);
		Mockito.when(session.getLoggedKeyspace()).thenReturn("mcs");
		Mockito.when(metadata.getKeyspace("mcs")).thenReturn(keySpace);
		Mockito.when(keySpace.getUserType("servicenodeparameters")).thenReturn(userType);
		Mockito.when(userType.contains("servicenodeparameters")).thenReturn(true);
		Mockito.when(metadata.getKeyspace(session.getLoggedKeyspace()).getUserType("servicenodeparameters").newValue()).thenReturn(udtValue);
	//	udtValue.setString("hjd", "hydhsjh");
		mockMvc = MockMvcBuilders.standaloneSetup(missionConfigController).build();
	}
	@Test
	public void testGetPlatformTypesOkResponse() throws Exception {
		Capabilities capabilities = new Capabilities();
		capabilities.setType_name("Sentry12");
		capabilities.setArchived(false);
		capabilities.setCreated_date(new Date());
		capabilities.setImage("image 1");
		capabilities.setUpdated_date(new Date());
		List<Capability> capabilityList = new ArrayList<>();
		Capability capability = new Capability();
		capability.setRequired("true");
		capability.setEnabled(true);
		capability.setDefault_provider("icrs_loam/icrs_loam_provider");
		capability.setSpec_version("1");
		List<String> childDns = new ArrayList<>();
		childDns.add("loam_node");
		capability.setChild_dn(childDns);
		capability.setDescription("Interface for capability 3D Slam Part A");
		capability.setSpec_type("interface");
		Map<String, String> map = new HashMap<>();
		capability.setInterfaceValue(map);
		capability.setName("lsd_slam_core_interface");
		capability.setPackageName("icrs_loam");
		List<Node> nodes = new ArrayList<>();
		Node node = new Node();
		node.setName("loam_node");
		List<String> dynamicReconfigureList  = new ArrayList<>();
		dynamicReconfigureList.add("filter_ground");
		dynamicReconfigureList.add("ground_filter/angle");
		dynamicReconfigureList.add("ground_filter/distance");
		dynamicReconfigureList.add("ground_filter/plane_distance");
		node.setDynamic_reconfigure(dynamicReconfigureList);
		node.setName("loam_node");
		Services services = new Services();
		List<Provides> provideList = new ArrayList<>();
		Provides serviceProvides = new Provides();
		provideList.add(serviceProvides);
		services.setProvides(provideList);
		List<Requires> requiresList = new ArrayList<>();
		Requires serviceRequires = new Requires();
		requiresList.add(serviceRequires);
		services.setRequires(requiresList);
		node.setServices(services);
		node.setParameter_file("icrs_loam_param.yaml");
		Topics topics = new Topics();
		List<Provides> topicsProvidesList = new ArrayList<>();
		Provides topicsProvides = new Provides();
		topicsProvides.setName("/tf");
		Map<String, String> topicsProvidesMap = new HashMap<>();
		topicsProvidesMap.put("description", "Odometry output of 3d Lidar slam");
		topicsProvidesMap.put("type", "nav_msgs/Odometry");
		topicsProvides.setProvideslist(topicsProvidesMap);
		topicsProvidesList.add(topicsProvides);
		topics.setProvides(topicsProvidesList);
		node.setTopics(topics);
		Parameters parameters = new Parameters();
		Provides parameterProvides = new Provides();
		List<Provides> parameterProvidesList = new ArrayList<>();
		parameterProvidesList.add(parameterProvides);
		parameters.setProvides(parameterProvidesList);
		List<Requires> parameterRequires = new ArrayList<>();
		Requires requires = new Requires();
		requires.setName("ground_filter/plane_distance");
		Map<String, String> requiresListData= new HashMap<>();
		requiresListData.put("default", "200.0");
		requiresListData.put("range","[100.0,300.0]");
		requiresListData.put("type","float");
		requires.setRequireslist(requiresListData);
		parameterRequires.add(requires);
		parameters.setRequires(parameterRequires);
		node.setParameters(parameters);
		nodes.add(node);
		capability.setNodes(nodes);
		capabilityList.add(capability);
		capabilities.setCapabilitylist(capabilityList);
		
		List<Capabilities> capabilitiesList = new ArrayList<>();
		capabilitiesList.add(capabilities);
		
		Mockito.when(capabilitiesDao.getAllPlatformCapabilities()).thenReturn(capabilitiesList);
		
		Capability marfCapability = new Capability();
		marfCapability.setRequired("true");
		marfCapability.setEnabled(true);
		marfCapability.setDefault_provider("icrs_loam/icrs_loam_provider");
		marfCapability.setSpec_version("1");
		List<String> marfChildDns = new ArrayList<>();
		marfChildDns.add("loam_node");
		marfCapability.setChild_dn(marfChildDns);
		marfCapability.setDescription("Interface for capability 3D Slam Part A");
		marfCapability.setSpec_type("interface");
		Map<String, String> marfInterfaceMap = new HashMap<>();
		marfCapability.setInterfaceValue(marfInterfaceMap);
		marfCapability.setName("marf_Log_Service_interface");
		marfCapability.setPackageName("marf_Log_Service");
		List<Node> marfLognodes = new ArrayList<>();
		Node marfLognode = new Node();
		marfLognode.setName("diagnostic_node");
		List<String> marfDynamicReconfigureList  = new ArrayList<>();
		marfDynamicReconfigureList.add("trace_level");
		marfDynamicReconfigureList.add("diagnostics_level");
		marfLognode.setDynamic_reconfigure(marfDynamicReconfigureList);
		Services marfServices = new Services();
		List<Provides> marfProvideList = new ArrayList<>();
		Provides marfServiceProvides = new Provides();
		marfProvideList.add(marfServiceProvides);
		marfServices.setProvides(marfProvideList);
		List<Requires> marfRequiresList = new ArrayList<>();
		Requires marfServiceRequires = new Requires();
		marfRequiresList.add(marfServiceRequires);
		marfServices.setRequires(marfRequiresList);
		marfLognode.setServices(marfServices);
		node.setParameter_file("icrs_marf_param.yaml");
		Topics marfTopics = new Topics();
		List<Provides> marfTopicsProvidesList = new ArrayList<>();
		Provides marfTopicsProvides = new Provides();
		topicsProvides.setName("/tf");
		Map<String, String> marfTopicsProvidesMap = new HashMap<>();
		marfTopicsProvidesMap.put("description", "Odometry output of 3d Lidar slam");
		marfTopicsProvidesMap.put("type", "nav_msgs/Odometry");
		marfTopicsProvides.setProvideslist(marfTopicsProvidesMap);
		marfTopicsProvidesList.add(marfTopicsProvides);
		marfTopics.setProvides(marfTopicsProvidesList);
		marfLognode.setTopics(marfTopics);
		Parameters marfParameters = new Parameters();
		Provides marfParameterProvides = new Provides();
		List<Provides> marfParameterProvidesList = new ArrayList<>();
		marfParameterProvidesList.add(marfParameterProvides);
		marfParameters.setProvides(marfParameterProvidesList);
		List<Requires> marfParameterRequires = new ArrayList<>();
		Requires marfParmrequires = new Requires();
		marfParmrequires.setName("diagnostics_level");
		Map<String, String> marfRequiresListData= new HashMap<>();
		marfRequiresListData.put("default", "1");
		marfRequiresListData.put("range","[0,3]");
		marfRequiresListData.put("type","integer");
		marfParmrequires.setRequireslist(marfRequiresListData);
		marfParameterRequires.add(marfParmrequires);
		marfParameters.setRequires(marfParameterRequires);
		marfLognode.setParameters(marfParameters);
		marfLognodes.add(marfLognode);
		marfCapability.setNodes(marfLognodes);
		
		List<Capability> capabilitiesFromXml = new ArrayList<>();
		capabilitiesFromXml.add(marfCapability);
		
		Mockito.when(missionConfigService.getPlatformServices(anyString())).thenReturn(capabilitiesFromXml);
		mockMvc.perform(get("/getPlatformTypes").accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
		capabilityList.add(marfCapability);
		String str = mockMvc.perform(MockMvcRequestBuilders.get("/getPlatformTypes").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn()
				.getResponse().getContentAsString();

		String capabilityJsonString = getJsonString(capabilitiesList);
		assertEquals(capabilityJsonString, str);
	}
	
	@Test
	public void testGetPlatformTypesSuccesResponse() throws Exception {
		Capabilities capabilities = new Capabilities();
		capabilities.setType_name("Sentry12");
		capabilities.setArchived(false);
		capabilities.setCreated_date(new Date());
		capabilities.setImage("image 1");
		capabilities.setUpdated_date(new Date());
		List<Capability> capabilityList = new ArrayList<>();
		Capability capability = new Capability();
		capability.setRequired("true");
		capability.setEnabled(true);
		capability.setDefault_provider("icrs_loam/icrs_loam_provider");
		capability.setSpec_version("1");
		List<String> childDns = new ArrayList<>();
		childDns.add("loam_node");
		capability.setChild_dn(childDns);
		capability.setDescription("Interface for capability 3D Slam Part A");
		capability.setSpec_type("interface");
		Map<String, String> map = new HashMap<>();
		capability.setInterfaceValue(map);
		capability.setName("lsd_slam_core_interface");
		capability.setPackageName("icrs_loam");
		List<Node> nodes = new ArrayList<>();
		Node node = new Node();
		node.setName("loam_node");
		List<String> dynamicReconfigureList  = new ArrayList<>();
		dynamicReconfigureList.add("filter_ground");
		dynamicReconfigureList.add("ground_filter/angle");
		dynamicReconfigureList.add("ground_filter/distance");
		dynamicReconfigureList.add("ground_filter/plane_distance");
		node.setDynamic_reconfigure(dynamicReconfigureList);
		node.setName("loam_node");
		Services services = new Services();
		List<Provides> provideList = new ArrayList<>();
		Provides serviceProvides = new Provides();
		provideList.add(serviceProvides);
		services.setProvides(provideList);
		List<Requires> requiresList = new ArrayList<>();
		Requires serviceRequires = new Requires();
		requiresList.add(serviceRequires);
		services.setRequires(requiresList);
		node.setServices(services);
		node.setParameter_file("icrs_loam_param.yaml");
		Topics topics = new Topics();
		List<Provides> topicsProvidesList = new ArrayList<>();
		Provides topicsProvides = new Provides();
		topicsProvides.setName("/tf");
		Map<String, String> topicsProvidesMap = new HashMap<>();
		topicsProvidesMap.put("description", "Odometry output of 3d Lidar slam");
		topicsProvidesMap.put("type", "nav_msgs/Odometry");
		topicsProvides.setProvideslist(topicsProvidesMap);
		topicsProvidesList.add(topicsProvides);
		topics.setProvides(topicsProvidesList);
		node.setTopics(topics);
		Parameters parameters = new Parameters();
		Provides parameterProvides = new Provides();
		List<Provides> parameterProvidesList = new ArrayList<>();
		parameterProvidesList.add(parameterProvides);
		parameters.setProvides(parameterProvidesList);
		List<Requires> parameterRequires = new ArrayList<>();
		Requires requires = new Requires();
		requires.setName("ground_filter/plane_distance");
		Map<String, String> requiresListData= new HashMap<>();
		requiresListData.put("default", "200.0");
		requiresListData.put("range","[100.0,300.0]");
		requiresListData.put("type","float");
		requires.setRequireslist(requiresListData);
		parameterRequires.add(requires);
		parameters.setRequires(parameterRequires);
		node.setParameters(parameters);
		nodes.add(node);
		capability.setNodes(nodes);
		capabilityList.add(capability);
		capabilities.setCapabilitylist(capabilityList);
		
		List<Capabilities> capabilitiesList = new ArrayList<>();
		capabilitiesList.add(capabilities);
		
		Mockito.when(capabilitiesDao.getAllPlatformCapabilities()).thenReturn(capabilitiesList);
		
		Capability marfCapability = new Capability();
		marfCapability.setRequired("true");
		marfCapability.setEnabled(true);
		marfCapability.setDefault_provider("icrs_loam/icrs_loam_provider");
		marfCapability.setSpec_version("1");
		List<String> marfChildDns = new ArrayList<>();
		marfChildDns.add("loam_node");
		marfCapability.setChild_dn(marfChildDns);
		marfCapability.setDescription("Interface for capability 3D Slam Part A");
		marfCapability.setSpec_type("interface");
		Map<String, String> marfInterfaceMap = new HashMap<>();
		marfCapability.setInterfaceValue(marfInterfaceMap);
		marfCapability.setName("marf_Log_Service_interface");
		marfCapability.setPackageName("marf_Log_Service");
		List<Node> marfLognodes = new ArrayList<>();
		Node marfLognode = new Node();
		marfLognode.setName("diagnostic_node");
		List<String> marfDynamicReconfigureList  = new ArrayList<>();
		marfDynamicReconfigureList.add("trace_level");
		marfDynamicReconfigureList.add("diagnostics_level");
		marfLognode.setDynamic_reconfigure(marfDynamicReconfigureList);
		Services marfServices = new Services();
		List<Provides> marfProvideList = new ArrayList<>();
		Provides marfServiceProvides = new Provides();
		marfProvideList.add(marfServiceProvides);
		marfServices.setProvides(marfProvideList);
		List<Requires> marfRequiresList = new ArrayList<>();
		Requires marfServiceRequires = new Requires();
		marfRequiresList.add(marfServiceRequires);
		marfServices.setRequires(marfRequiresList);
		marfLognode.setServices(marfServices);
		node.setParameter_file("icrs_marf_param.yaml");
		Topics marfTopics = new Topics();
		List<Provides> marfTopicsProvidesList = new ArrayList<>();
		Provides marfTopicsProvides = new Provides();
		topicsProvides.setName("/tf");
		Map<String, String> marfTopicsProvidesMap = new HashMap<>();
		marfTopicsProvidesMap.put("description", "Odometry output of 3d Lidar slam");
		marfTopicsProvidesMap.put("type", "nav_msgs/Odometry");
		marfTopicsProvides.setProvideslist(marfTopicsProvidesMap);
		marfTopicsProvidesList.add(marfTopicsProvides);
		marfTopics.setProvides(marfTopicsProvidesList);
		marfLognode.setTopics(marfTopics);
		Parameters marfParameters = new Parameters();
		Provides marfParameterProvides = new Provides();
		List<Provides> marfParameterProvidesList = new ArrayList<>();
		marfParameterProvidesList.add(marfParameterProvides);
		marfParameters.setProvides(marfParameterProvidesList);
		List<Requires> marfParameterRequires = new ArrayList<>();
		Requires marfParmrequires = new Requires();
		marfParmrequires.setName("diagnostics_level");
		Map<String, String> marfRequiresListData= new HashMap<>();
		marfRequiresListData.put("default", "1");
		marfRequiresListData.put("range","[0,3]");
		marfRequiresListData.put("type","integer");
		marfParmrequires.setRequireslist(marfRequiresListData);
		marfParameterRequires.add(marfParmrequires);
		marfParameters.setRequires(marfParameterRequires);
		marfLognode.setParameters(marfParameters);
		marfLognodes.add(marfLognode);
		marfCapability.setNodes(marfLognodes);
		
		List<Capability> capabilitiesFromXml = new ArrayList<>();
		capabilitiesFromXml.add(marfCapability);
		
		Mockito.when(missionConfigService.getPlatformServices(anyString())).thenReturn(capabilitiesFromXml);
		capabilityList.add(marfCapability);
		String platormTypesResponseString = mockMvc.perform(MockMvcRequestBuilders.get("/getPlatformTypes").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn()
				.getResponse().getContentAsString();

		String capabilityJsonString = getJsonString(capabilitiesList);
		assertEquals(capabilityJsonString, platormTypesResponseString);
	}

	@Test
	public void testGetPlatformServices() {
		
	}

	@Test
	public void testInsertMissionConfigSuccess() throws Exception {
		MissionConfigUI missionConfigUI = new MissionConfigUI();
		UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfigUI.setMissionConfigId(uuid);
		missionConfigUI.setMissionName("missionABC");
		missionConfigUI.setMissionDes("mission for sentry");
		Set<String> missionusers = new HashSet<>();
		missionusers.add("test");
		missionusers.add("admin");
		missionConfigUI.setMissionUsers(missionusers);
		List<PlatformUI> platformUIsList = new ArrayList<>();
		//-------------------------------------------platformUI
		PlatformUI platformUI  = new PlatformUI();
		platformUI.setName("sentry1");
		platformUI.setType("Sentry");
		List<ServiceUI> serviceUIList = new ArrayList<>();
		//*******************Service***********
		ServiceUI serviceUI = new ServiceUI();
		serviceUI.setName("icrs_diagnostic_interface");
		serviceUI.setPackageName("icrs_diagnostic");
		serviceUI.setLogEnabled(false);
		serviceUI.setRecordEnabled(true);
		serviceUI.setRequired(false);
		serviceUI.setEnabled(true);
		serviceUI.setServiceName("icrs_diagnostic/icrs_diagnostic_interface");
		List<ServiceNodeParametersUI> serviceNodeParametersUIsList = new ArrayList<>();
		//************ServiceNodeParameters***************
		ServiceNodeParametersUI servcieNodeParametersUI = new ServiceNodeParametersUI();
		servcieNodeParametersUI.setNodeName("diagnostic_node");
		List<PropertiesUI> propertiesUIList = new ArrayList<>();
		PropertiesUI propertiesUIDiag = new PropertiesUI();
		propertiesUIDiag.setName("diagnostics_level");
		propertiesUIDiag.setValue("1");
		propertiesUIList.add(propertiesUIDiag);
		PropertiesUI propertiesUITrace = new PropertiesUI();
		propertiesUITrace.setName("trace_level");
		propertiesUITrace.setValue("0");
		propertiesUIList.add(propertiesUITrace);
		servcieNodeParametersUI.setProperties(propertiesUIList);
		serviceNodeParametersUIsList.add(servcieNodeParametersUI);
		serviceUI.setServiceNodeList(serviceNodeParametersUIsList);
		serviceUIList.add(serviceUI);
		platformUI.setServices(serviceUIList);
		
		platformUIsList.add(platformUI);
		missionConfigUI.setPlatforms(platformUIsList);
		
		//*************get the capability file*************
				Capabilities caps = new Capabilities();
				caps.setArchived(false);
				caps.setImage("sentry.png");
				caps.setType_name("Sentry");
				List<Capability> capability = new ArrayList<>();
				//
				Capability cap = new Capability();
				cap.setRequired("false");
				cap.setDescription("capability for diagnostic_interface");
				cap.setEnabled(true);
				cap.setName("icrs_diagnostic_interface");
				cap.setPackageName("icrs_diagnostic");
				
				List<Node> nodes = new ArrayList<>();
				//*
				Node node = new Node();
				node.setName("diagnostic_node");
				Parameters parameters = new Parameters();
				List<Requires> requiresElementList  = new ArrayList<>(); 
				Requires requiresDiag = new Requires();
				requiresDiag.setName("diagnostics_level");
				Map<String,String> requiresMap = new HashMap<String, String>();
				requiresMap.put("default", "1");
				requiresMap.put("range", "[0,1]");
				requiresMap.put("type", "integer");
				requiresDiag.setRequireslist(requiresMap);
				Requires requiresTrace = new Requires();
				requiresTrace.setName("trace_level");
				Map<String,String> requiresMapTrace = new HashMap<String, String>();
				requiresMapTrace.put("default", "2");
				requiresMapTrace.put("range", "[2,4]");
				requiresMapTrace.put("type", "integer");
				requiresTrace.setRequireslist(requiresMapTrace);
				requiresElementList.add(requiresTrace);
				requiresElementList.add(requiresDiag);
				parameters.setRequires(requiresElementList);
				node.setParameters(parameters);
				nodes.add(node);
				cap.setNodes(nodes);
				capability.add(cap);
				caps.setCapabilitylist(capability);
				Mockito.when(fileService.getCapByPlatformType("Sentry", "icrs_diagnostic_interface")).thenReturn(caps);
				
				MissionConfig missionConfig = new MissionConfig();
				missionConfig.setMissionConfig_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setArchived(false);
				missionConfig.setDescription("mission for sentry");
				missionConfig.setCreatedDate(new Date());
				missionConfig.setLogEnabled(false);
				missionConfig.setRecordEnabled(true);
				missionConfig.setMission_name("missionABC");
				/*Set<String> missionusers = new HashSet();
				missionusers.add("admin");
				missionusers.add("test");*/
				missionConfig.setMission_users(missionusers);
				missionConfig.setPlatform_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setPlatform_name("sentry1");
				missionConfig.setPlatform_type("Sentry");
				missionConfig.setPlatform_service_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setPlatform_serviceName("icrs_diagnostic/icrs_diagnostic_interface");
				missionConfig.setYamlServicename("icrs_diagnostic_interface");
				missionConfig.setYamlServicePackage("icrs_diagnostic");
				//missionConfig
				
				missionConfig.setPlatform_serviceType("DISCRETE");
				missionConfig.setRequired(false);
			
				Map<String, String> serviceNodeProperties = new HashMap<>();
				serviceNodeProperties.put("diagnostics_level","1");
				serviceNodeProperties.put("trace_level", "0");
				/*Mockito.when(udtValue.getString("nodename")).thenReturn("diagnostic_node");
				Mockito.when(udtValue.getMap("properties", String.class, String.class)).thenReturn(serviceNodeProperties);
				List<UDTValue> udtList = new ArrayList<>();
				udtList.add(udtValue);
				missionConfig.setServiceNodeParameters(udtList);*/
				Capability cap1 = caps.getCapabilitylist().isEmpty()?new Capability():caps.getCapabilitylist().get(0);
				ObjectMapper objectMapper = new ObjectMapper();
				String capString = null;
				capString = objectMapper.writeValueAsString(cap1);
				missionConfig.setCapabilityInfo(capString);
				List<MissionConfig> missionConfigData = new ArrayList<>();
				missionConfigData.add(missionConfig);
				missionConfigData.add(missionConfig);
				Mockito.when(missionConfigService.insertMissionConfig(anyObject())).thenReturn(missionConfig);
		
		
				String json = getJsonString(missionConfigUI);
				mockMvc.perform(post("/saveMissionAndPlatformData").content(json).contentType(org.springframework.http.MediaType.APPLICATION_JSON).with(csrf()))
				.andExpect(status().is2xxSuccessful());
	}
	
	@Test
    public void testInsertMissionConfigFailureValidateCase() throws Exception{
    	MissionConfigUI missionConfigUI = new MissionConfigUI();
    	UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfigUI.setMissionConfigId(uuid);
		missionConfigUI.setMissionName("missionABC");
		Mockito.when(errors.hasErrors()).thenReturn(true);
		errors.rejectValue("missionName", "Mission Name is already exist ");
		String json = getJsonString(missionConfigUI);
		mockMvc.perform(post("/saveMissionAndPlatformData").content(json).contentType(org.springframework.http.MediaType.APPLICATION_JSON).with(csrf()))
		.andExpect(status().is4xxClientError());
    }

	@Test
	public void testUpdateMissionConfigOkResponseNotArchived() throws Exception {
		MissionConfigUI missionConfigUI = new MissionConfigUI();
		UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfigUI.setMissionConfigId(uuid);
		missionConfigUI.setMissionName("missionABC");
		missionConfigUI.setMissionDes("mission for sentry");
		Set<String> missionusers = new HashSet<>();
		missionusers.add("test");
		missionusers.add("admin");
		missionConfigUI.setMissionUsers(missionusers);
		List<PlatformUI> platformUIsList = new ArrayList<>();
		//-------------------------------------------platformUI
		PlatformUI platformUI  = new PlatformUI();
		platformUI.setName("sentry1");
		platformUI.setType("Sentry");
		List<ServiceUI> serviceUIList = new ArrayList<>();
		//*******************Service***********
		ServiceUI serviceUI = new ServiceUI();
		serviceUI.setName("icrs_diagnostic_interface");
		serviceUI.setPackageName("icrs_diagnostic");
		serviceUI.setLogEnabled(false);
		serviceUI.setRecordEnabled(true);
		serviceUI.setRequired(false);
		serviceUI.setEnabled(true);
		serviceUI.setServiceName("icrs_diagnostic/icrs_diagnostic_interface");
		List<ServiceNodeParametersUI> serviceNodeParametersUIsList = new ArrayList<>();
		//************ServiceNodeParameters***************
		ServiceNodeParametersUI servcieNodeParametersUI = new ServiceNodeParametersUI();
		servcieNodeParametersUI.setNodeName("diagnostic_node");
		List<PropertiesUI> propertiesUIList = new ArrayList<>();
		PropertiesUI propertiesUIDiag = new PropertiesUI();
		propertiesUIDiag.setName("diagnostics_level");
		propertiesUIDiag.setValue("1");
		propertiesUIList.add(propertiesUIDiag);
		PropertiesUI propertiesUITrace = new PropertiesUI();
		propertiesUITrace.setName("trace_level");
		propertiesUITrace.setValue("0");
		propertiesUIList.add(propertiesUITrace);
		servcieNodeParametersUI.setProperties(propertiesUIList);
		serviceNodeParametersUIsList.add(servcieNodeParametersUI);
		serviceUI.setServiceNodeList(serviceNodeParametersUIsList);
		serviceUIList.add(serviceUI);
		platformUI.setServices(serviceUIList);
		
		platformUIsList.add(platformUI);
		missionConfigUI.setPlatforms(platformUIsList);
		
		//*************get the capability file*************
				Capabilities caps = new Capabilities();
				caps.setArchived(false);
				caps.setImage("sentry.png");
				caps.setType_name("Sentry");
				List<Capability> capability = new ArrayList<>();
				//
				Capability cap = new Capability();
				cap.setRequired("false");
				cap.setDescription("capability for diagnostic_interface");
				cap.setEnabled(true);
				cap.setName("icrs_diagnostic_interface");
				cap.setPackageName("icrs_diagnostic");
				
				List<Node> nodes = new ArrayList<>();
				//*
				Node node = new Node();
				node.setName("diagnostic_node");
				Parameters parameters = new Parameters();
				List<Requires> requiresElementList  = new ArrayList<>(); 
				Requires requiresDiag = new Requires();
				requiresDiag.setName("diagnostics_level");
				Map<String,String> requiresMap = new HashMap<String, String>();
				requiresMap.put("default", "1");
				requiresMap.put("range", "[0,1]");
				requiresMap.put("type", "integer");
				requiresDiag.setRequireslist(requiresMap);
				Requires requiresTrace = new Requires();
				requiresTrace.setName("trace_level");
				Map<String,String> requiresMapTrace = new HashMap<String, String>();
				requiresMapTrace.put("default", "2");
				requiresMapTrace.put("range", "[2,4]");
				requiresMapTrace.put("type", "integer");
				requiresTrace.setRequireslist(requiresMapTrace);
				requiresElementList.add(requiresTrace);
				requiresElementList.add(requiresDiag);
				parameters.setRequires(requiresElementList);
				node.setParameters(parameters);
				nodes.add(node);
				cap.setNodes(nodes);
				capability.add(cap);
				caps.setCapabilitylist(capability);
				Mockito.when(fileService.getCapByPlatformType("Sentry", "icrs_diagnostic_interface")).thenReturn(caps);
				
				MissionConfig missionConfig = new MissionConfig();
				missionConfig.setMissionConfig_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setArchived(false);
				missionConfig.setDescription("mission for sentry");
				missionConfig.setCreatedDate(new Date());
				missionConfig.setLogEnabled(false);
				missionConfig.setRecordEnabled(true);
				missionConfig.setMission_name("missionABC");
				/*Set<String> missionusers = new HashSet();
				missionusers.add("admin");
				missionusers.add("test");*/
				missionConfig.setMission_users(missionusers);
				missionConfig.setPlatform_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setPlatform_name("sentry1");
				missionConfig.setPlatform_type("Sentry");
				missionConfig.setPlatform_service_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setPlatform_serviceName("icrs_diagnostic/icrs_diagnostic_interface");
				missionConfig.setYamlServicename("icrs_diagnostic_interface");
				missionConfig.setYamlServicePackage("icrs_diagnostic");
				//missionConfig
				
				missionConfig.setPlatform_serviceType("DISCRETE");
				missionConfig.setRequired(false);
			
				Map<String, String> serviceNodeProperties = new HashMap<>();
				serviceNodeProperties.put("diagnostics_level","1");
				serviceNodeProperties.put("trace_level", "0");
				/*Mockito.when(udtValue.getString("nodename")).thenReturn("diagnostic_node");
				Mockito.when(udtValue.getMap("properties", String.class, String.class)).thenReturn(serviceNodeProperties);
				List<UDTValue> udtList = new ArrayList<>();
				udtList.add(udtValue);
				missionConfig.setServiceNodeParameters(udtList);*/
				Capability cap1 = caps.getCapabilitylist().isEmpty()?new Capability():caps.getCapabilitylist().get(0);
				ObjectMapper objectMapper = new ObjectMapper();
				String capString = null;
				capString = objectMapper.writeValueAsString(cap1);
				missionConfig.setCapabilityInfo(capString);
				List<MissionConfig> missionConfigData = new ArrayList<>();
				missionConfigData.add(missionConfig);
				missionConfigData.add(missionConfig);
				Mockito.when(missionConfigService.insertMissionConfig(anyObject())).thenReturn(missionConfig);
				
				Mockito.when(missionConfigService.getMissionConfigDataById(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"))).thenReturn(missionConfigData);
		
				String json = getJsonString(missionConfigUI);
				mockMvc.perform(post("/updateMissionAndPlatformData").content(json).contentType(org.springframework.http.MediaType.APPLICATION_JSON).with(csrf()))
				.andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void testUpdateMissionConfigOkResponseArchived() throws Exception {
		MissionConfigUI missionConfigUI = new MissionConfigUI();
		UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfigUI.setMissionConfigId(uuid);
		missionConfigUI.setMissionName("missionABC");
		missionConfigUI.setMissionDes("mission for sentry");
		Set<String> missionusers = new HashSet<>();
		missionusers.add("test");
		missionusers.add("admin");
		missionConfigUI.setMissionUsers(missionusers);
		List<PlatformUI> platformUIsList = new ArrayList<>();
		//-------------------------------------------platformUI
		PlatformUI platformUI  = new PlatformUI();
		platformUI.setName("sentry1");
		platformUI.setType("Sentry");
		List<ServiceUI> serviceUIList = new ArrayList<>();
		//*******************Service***********
		ServiceUI serviceUI = new ServiceUI();
		serviceUI.setName("icrs_diagnostic_interface");
		serviceUI.setPackageName("icrs_diagnostic");
		serviceUI.setLogEnabled(false);
		serviceUI.setRecordEnabled(true);
		serviceUI.setRequired(false);
		serviceUI.setEnabled(true);
		serviceUI.setServiceName("icrs_diagnostic/icrs_diagnostic_interface");
		List<ServiceNodeParametersUI> serviceNodeParametersUIsList = new ArrayList<>();
		//************ServiceNodeParameters***************
		ServiceNodeParametersUI servcieNodeParametersUI = new ServiceNodeParametersUI();
		servcieNodeParametersUI.setNodeName("diagnostic_node");
		List<PropertiesUI> propertiesUIList = new ArrayList<>();
		PropertiesUI propertiesUIDiag = new PropertiesUI();
		propertiesUIDiag.setName("diagnostics_level");
		propertiesUIDiag.setValue("1");
		propertiesUIList.add(propertiesUIDiag);
		PropertiesUI propertiesUITrace = new PropertiesUI();
		propertiesUITrace.setName("trace_level");
		propertiesUITrace.setValue("0");
		propertiesUIList.add(propertiesUITrace);
		servcieNodeParametersUI.setProperties(propertiesUIList);
		serviceNodeParametersUIsList.add(servcieNodeParametersUI);
		serviceUI.setServiceNodeList(serviceNodeParametersUIsList);
		serviceUIList.add(serviceUI);
		platformUI.setServices(serviceUIList);
		
		platformUIsList.add(platformUI);
		missionConfigUI.setPlatforms(platformUIsList);
		
		//*************get the capability file*************
				Capabilities caps = new Capabilities();
				caps.setArchived(false);
				caps.setImage("sentry.png");
				caps.setType_name("Sentry");
				List<Capability> capability = new ArrayList<>();
				//
				Capability cap = new Capability();
				cap.setRequired("false");
				cap.setDescription("capability for diagnostic_interface");
				cap.setEnabled(true);
				cap.setName("icrs_diagnostic_interface");
				cap.setPackageName("icrs_diagnostic");
				
				List<Node> nodes = new ArrayList<>();
				//*
				Node node = new Node();
				node.setName("diagnostic_node");
				Parameters parameters = new Parameters();
				List<Requires> requiresElementList  = new ArrayList<>(); 
				Requires requiresDiag = new Requires();
				requiresDiag.setName("diagnostics_level");
				Map<String,String> requiresMap = new HashMap<String, String>();
				requiresMap.put("default", "1");
				requiresMap.put("range", "[0,1]");
				requiresMap.put("type", "integer");
				requiresDiag.setRequireslist(requiresMap);
				Requires requiresTrace = new Requires();
				requiresTrace.setName("trace_level");
				Map<String,String> requiresMapTrace = new HashMap<String, String>();
				requiresMapTrace.put("default", "2");
				requiresMapTrace.put("range", "[2,4]");
				requiresMapTrace.put("type", "integer");
				requiresTrace.setRequireslist(requiresMapTrace);
				requiresElementList.add(requiresTrace);
				requiresElementList.add(requiresDiag);
				parameters.setRequires(requiresElementList);
				node.setParameters(parameters);
				nodes.add(node);
				cap.setNodes(nodes);
				capability.add(cap);
				caps.setCapabilitylist(capability);
				Mockito.when(fileService.getCapByPlatformType("Sentry", "icrs_diagnostic_interface")).thenReturn(caps);
				
				MissionConfig missionConfig = new MissionConfig();
				missionConfig.setMissionConfig_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setArchived(true);
				missionConfig.setDescription("mission for sentry");
				missionConfig.setCreatedDate(new Date());
				missionConfig.setLogEnabled(false);
				missionConfig.setRecordEnabled(true);
				missionConfig.setMission_name("missionABC");
				/*Set<String> missionusers = new HashSet();
				missionusers.add("admin");
				missionusers.add("test");*/
				missionConfig.setMission_users(missionusers);
				missionConfig.setPlatform_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setPlatform_name("sentry1");
				missionConfig.setPlatform_type("Sentry");
				missionConfig.setPlatform_service_id(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"));
				missionConfig.setPlatform_serviceName("icrs_diagnostic/icrs_diagnostic_interface");
				missionConfig.setYamlServicename("icrs_diagnostic_interface");
				missionConfig.setYamlServicePackage("icrs_diagnostic");
				//missionConfig
				
				missionConfig.setPlatform_serviceType("DISCRETE");
				missionConfig.setRequired(false);
			
				Map<String, String> serviceNodeProperties = new HashMap<>();
				serviceNodeProperties.put("diagnostics_level","1");
				serviceNodeProperties.put("trace_level", "0");
				/*Mockito.when(udtValue.getString("nodename")).thenReturn("diagnostic_node");
				Mockito.when(udtValue.getMap("properties", String.class, String.class)).thenReturn(serviceNodeProperties);
				List<UDTValue> udtList = new ArrayList<>();
				udtList.add(udtValue);
				missionConfig.setServiceNodeParameters(udtList);*/
				Capability cap1 = caps.getCapabilitylist().isEmpty()?new Capability():caps.getCapabilitylist().get(0);
				ObjectMapper objectMapper = new ObjectMapper();
				String capString = null;
				capString = objectMapper.writeValueAsString(cap1);
				missionConfig.setCapabilityInfo(capString);
				List<MissionConfig> missionConfigData = new ArrayList<>();
				missionConfigData.add(missionConfig);
				missionConfigData.add(missionConfig);
				Mockito.when(missionConfigService.insertMissionConfig(anyObject())).thenReturn(missionConfig);
				
				Mockito.when(missionConfigService.getMissionConfigDataById(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"))).thenReturn(missionConfigData);
				
				Mockito.when(missionConfigService.getMissionConfigIdsBasedOnMissionName("missionABC")).thenReturn(missionConfigData);
		
				String json = getJsonString(missionConfigUI);
				mockMvc.perform(post("/updateMissionAndPlatformData").content(json).contentType(org.springframework.http.MediaType.APPLICATION_JSON).with(csrf()))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testMissionConfigListByUser() {
		
	}

	
	@Test
	public void testGetAllMissionConfigDetailsSuccessOkResponse() throws Exception {
		MissionConfig missionConfig = new MissionConfig();
		UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfig.setMissionConfig_id(uuid);
		missionConfig.setMission_name("MissionABC");
		missionConfig.setPlatform_id(UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb"));
		missionConfig.setPlatform_service_id(UUID.fromString("5fe668e0-e4a2-11e7-aac3-4dc1c653bcdb"));
		missionConfig.setPlatform_name("bb1");
		missionConfig.setPlatform_type("BallBot");
		Set<String> userNameList = new HashSet<String>();
		userNameList.add("admin");
		userNameList.add("test");
		missionConfig.setMission_users(userNameList);
		missionConfig.setCreatedDate(new Date());
		missionConfig.setDescription("Mission for Ballbot");
		missionConfig.setPlatform_serviceName(" icrs_health/icrs_health_interface");
		missionConfig.setPlatform_serviceType("Descrete");
		missionConfig.setArchived(false);
		missionConfig.setYamlServicePackage("icrs_health");
		missionConfig.setYamlServicename("icrs_health_interface");
		missionConfig.setLogEnabled(true);
		missionConfig.setRecordEnabled(false);
		missionConfig.setRequired(true);
		List<MissionConfig> missionConfigData = new ArrayList<>();
		missionConfigData.add(missionConfig);
		Mockito.when(missionConfigService.getAllMissionConfigDetails()).thenReturn(missionConfigData);
		Mockito.when(missionConfigService.getMissionConfigDataById(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"))).thenReturn(missionConfigData);
		mockMvc.perform(get("/getAllMissions").accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
		String str = mockMvc.perform(MockMvcRequestBuilders.get("/getAllMissions").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn()
		.getResponse().getContentAsString();
		System.out.println(str);
	}
	
	@Test
	public void testGetAllMissionConfigDetailsSuccessResponse() throws UnsupportedEncodingException, Exception{
		MissionConfig missionConfig = new MissionConfig();
		UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfig.setMissionConfig_id(uuid);
		missionConfig.setMission_name("MissionABC");
		missionConfig.setPlatform_id(UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb"));
		missionConfig.setPlatform_service_id(UUID.fromString("5fe668e0-e4a2-11e7-aac3-4dc1c653bcdb"));
		missionConfig.setPlatform_name("bb1");
		missionConfig.setPlatform_type("BallBot");
		Set<String> userNameList = new HashSet<String>();
		userNameList.add("admin");
		userNameList.add("test");
		missionConfig.setMission_users(userNameList);
		missionConfig.setCreatedDate(new Date("10-aug-2017"));
		missionConfig.setDescription("Mission for Ballbot");
		missionConfig.setPlatform_serviceName(" icrs_health/icrs_health_interface");
		missionConfig.setPlatform_serviceType("Descrete");
		missionConfig.setArchived(false);
		missionConfig.setYamlServicePackage("icrs_health");
		missionConfig.setYamlServicename("icrs_health_interface");
		missionConfig.setLogEnabled(true);
		missionConfig.setRecordEnabled(false);
		missionConfig.setRequired(true);
		List<MissionConfig> missionConfigData = new ArrayList<>();
		missionConfigData.add(missionConfig);
		Mockito.when(missionConfigService.getAllMissionConfigDetails()).thenReturn(missionConfigData);
		Mockito.when(missionConfigService.getMissionConfigDataById(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"))).thenReturn(missionConfigData);
		String str = mockMvc.perform(MockMvcRequestBuilders.get("/getAllMissions").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn()
				.getResponse().getContentAsString();
				System.out.println(str);
		MissionConfigUI missionConfigUI = new MissionConfigUI();
		missionConfigUI.setMissionConfigId(uuid);
		missionConfigUI.setMissionName("MissionABC");
		missionConfigUI.setMissionDes("Mission for Ballbot");
		missionConfigUI.setCreatedDate(new Date("10-aug-2017"));
		missionConfigUI.setArchived(false);
		missionConfigUI.setMissionUsers(userNameList);
		
		PlatformUI platformUI = new PlatformUI();
		platformUI.setPlatformId(UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb"));
		platformUI.setName("bb1");
		platformUI.setType("BallBot");
		
		ServiceUI serviceUI = new ServiceUI();
		serviceUI.setServiceId(UUID.fromString("5fe668e0-e4a2-11e7-aac3-4dc1c653bcdb"));
		serviceUI.setServiceName(" icrs_health/icrs_health_interface");
		serviceUI.setServiceType("Descrete");
		serviceUI.setName("icrs_health_interface");
		serviceUI.setPackageName("icrs_health");
		serviceUI.setRequired(false);
		serviceUI.setRecordEnabled(false);
		serviceUI.setLogEnabled(true);
		List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
		serviceUI.setServiceNodeList(serviceNodeList);
		List<ServiceUI> services = new ArrayList<>();
		services.add(serviceUI);
		
		platformUI.setServices(services);
		
		List<PlatformUI> platformUIs = new ArrayList<>();
		platformUIs.add(platformUI);
		
		missionConfigUI.setPlatforms(platformUIs);
		
		List<MissionConfigUI> missionConfigUIs = new ArrayList<>();
		missionConfigUIs.add(missionConfigUI);
		
		String jsonString = getJsonString(missionConfigUIs);
		System.out.println(jsonString);
		assertEquals(jsonString, str);
		
		//serviceUI.set
	}


	@Test
	public void testGetMissionConfigurationsByCurrentUserSuccessOkResponse() throws UnsupportedEncodingException, Exception {
		Mockito.when( securityContext.getAuthentication()).thenReturn(authentication);
		Mockito.when(authentication.getName()).thenReturn("test");
		securityContextHolder.setContext(securityContext);
		MissionConfig missionConfig = new MissionConfig();
		UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
		missionConfig.setMissionConfig_id(uuid);
		missionConfig.setMission_name("MissionABC");
		missionConfig.setPlatform_id(UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb"));
		missionConfig.setPlatform_service_id(UUID.fromString("5fe668e0-e4a2-11e7-aac3-4dc1c653bcdb"));
		missionConfig.setPlatform_name("bb1");
		missionConfig.setPlatform_type("BallBot");
		Set<String> userNameList = new HashSet<String>();
		userNameList.add("admin");
		userNameList.add("test");
		missionConfig.setMission_users(userNameList);
		missionConfig.setCreatedDate(new Date());
		missionConfig.setDescription("Mission for Ballbot");
		missionConfig.setPlatform_serviceName(" icrs_health/icrs_health_interface");
		missionConfig.setPlatform_serviceType("Descrete");
		missionConfig.setArchived(false);
		missionConfig.setYamlServicePackage("icrs_health");
		missionConfig.setYamlServicename("icrs_health_interface");
		missionConfig.setLogEnabled(true);
		missionConfig.setRecordEnabled(false);
		missionConfig.setRequired(true);
		List<MissionConfig> missionConfigData = new ArrayList<>();
		missionConfigData.add(missionConfig);
		Mockito.when(missionConfigService.missionConfigListByUser("test")).thenReturn(missionConfigData);
		Mockito.when(missionConfigService.getMissionConfigDataById(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"))).thenReturn(missionConfigData);
		mockMvc.perform(MockMvcRequestBuilders.get("/missionConfigurations").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn()
		.getResponse().getContentAsString();
	}
	
	@Test
	public void testGetMissionConfigurationsByCurrentUserSuccessResponse() throws UnsupportedEncodingException, Exception{
			Mockito.when( securityContext.getAuthentication()).thenReturn(authentication);
			Mockito.when(authentication.getName()).thenReturn("test");
			securityContextHolder.setContext(securityContext);
			MissionConfig missionConfig = new MissionConfig();
			UUID uuid = UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25");
			missionConfig.setMissionConfig_id(uuid);
			missionConfig.setMission_name("MissionABC");
			missionConfig.setPlatform_id(UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb"));
			missionConfig.setPlatform_service_id(UUID.fromString("5fe668e0-e4a2-11e7-aac3-4dc1c653bcdb"));
			missionConfig.setPlatform_name("bb1");
			missionConfig.setPlatform_type("BallBot");
			Set<String> userNameList = new HashSet<String>();
			userNameList.add("admin");
			userNameList.add("test");
			missionConfig.setMission_users(userNameList);
			missionConfig.setCreatedDate(new Date("10-aug-2017"));
			missionConfig.setDescription("Mission for Ballbot");
			missionConfig.setPlatform_serviceName(" icrs_health/icrs_health_interface");
			missionConfig.setPlatform_serviceType("Descrete");
			missionConfig.setArchived(false);
			missionConfig.setYamlServicePackage("icrs_health");
			missionConfig.setYamlServicename("icrs_health_interface");
			missionConfig.setLogEnabled(true);
			missionConfig.setRecordEnabled(false);
			missionConfig.setRequired(true);
			List<MissionConfig> missionConfigData = new ArrayList<>();
			missionConfigData.add(missionConfig);
			Mockito.when(missionConfigService.missionConfigListByUser("test")).thenReturn(missionConfigData);
			Mockito.when(missionConfigService.getMissionConfigDataById(UUID.fromString("63df7c60-e48a-11e7-8c64-e36950bb2d25"))).thenReturn(missionConfigData);
			String str = mockMvc.perform(MockMvcRequestBuilders.get("/missionConfigurations").accept(MediaType.APPLICATION_JSON_UTF8)).andReturn()
					.getResponse().getContentAsString();
			MissionConfigUI missionConfigUI = new MissionConfigUI();
			missionConfigUI.setMissionConfigId(uuid);
			missionConfigUI.setMissionName("MissionABC");
			missionConfigUI.setMissionDes("Mission for Ballbot");
			missionConfigUI.setCreatedDate(new Date("10-aug-2017"));
			missionConfigUI.setArchived(false);
			missionConfigUI.setMissionUsers(userNameList);
			
			PlatformUI platformUI = new PlatformUI();
			platformUI.setPlatformId(UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb"));
			platformUI.setName("bb1");
			platformUI.setType("BallBot");
			
			ServiceUI serviceUI = new ServiceUI();
			serviceUI.setServiceId(UUID.fromString("5fe668e0-e4a2-11e7-aac3-4dc1c653bcdb"));
			serviceUI.setServiceName(" icrs_health/icrs_health_interface");
			serviceUI.setServiceType("Descrete");
			serviceUI.setName("icrs_health_interface");
			serviceUI.setPackageName("icrs_health");
			serviceUI.setRequired(false);
			serviceUI.setRecordEnabled(false);
			serviceUI.setLogEnabled(true);
			List<ServiceNodeParametersUI> serviceNodeList = new ArrayList<>();
			serviceUI.setServiceNodeList(serviceNodeList);
			List<ServiceUI> services = new ArrayList<>();
			services.add(serviceUI);
			
			platformUI.setServices(services);
			
			List<PlatformUI> platformUIs = new ArrayList<>();
			platformUIs.add(platformUI);
			
			missionConfigUI.setPlatforms(platformUIs);
			
			List<MissionConfigUI> missionConfigUIs = new ArrayList<>();
			missionConfigUIs.add(missionConfigUI);
			
			String jsonString = getJsonString(missionConfigUIs);
			assertEquals(jsonString, str);
	}

	@Test
	public void testSavePlatformConfig() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAllPlatformConfigUIData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServiceNodeParametersforPlatformConfig() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAllRunningMissions() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRunningMissionServiceNodeParameters() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelete() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteMission() throws Exception {
		UUID missionConfigId = UUID.fromString("5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb");
		Mockito.when(missionConfigService.updateMissionConfigDataById(true, missionConfigId)).thenReturn(anyObject());
		mockMvc.perform(get("/deleteMission/5fd74db0-e4a2-11e7-aac3-4dc1c653bcdb").accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}
	
	private String getJsonString(Object javaObject) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(javaObject);
		} catch (Exception e) {
		}
		return jsonString;
	}

}
