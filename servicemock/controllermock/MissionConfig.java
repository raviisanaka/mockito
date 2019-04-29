package drdo.cair.isrd.icrs.mcs.entity;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.Table;

/**
 * 
 * @author user
 *
 */
@Table(keyspace = "mcs",name = "missionconfig")
public class MissionConfig {

	public MissionConfig() {
		super();
	}

	@PrimaryKeyColumn(name = "missionconfig_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID missionConfig_id;

	@PrimaryKeyColumn(name = "platform_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID platform_id;

	@PrimaryKeyColumn(name = "platform_service_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	private UUID platform_service_id;

	@Column(value = "mission_name")
	private String mission_name;

	@Column(value = "platform_name")
	private String platform_name;

	@Column(value = "platform_type")
	private String platform_type;

	@Column(value = "mission_MapConfig")
	private Map<String, String> mission_MapConfig;

	@Column(value = "mission_users")
	private Set<String> mission_users;

	@Column(value = "createdDate")
	private Date createdDate;

	@Column(value = "description")
	private String description;

	@Column(value = "platform_serviceName")
	private String platform_serviceName;

	@Column(value = "platform_serviceType")
	private String platform_serviceType;
	
	@Frozen
	@Column(value = "servicenodeparameters")
	private List<UDTValue> serviceNodeParameters;

	@Column(value = "package_name")
	private String yamlServicePackage;

	@Column(value = "name")
	private String yamlServicename;

	@Column(value = "log_enabled")
	private boolean logEnabled;

	@Column(value = "record_enabled")
	private boolean recordEnabled;

	@Column(value = "is_required")
	private boolean isRequired;
	
	@Column(value = "archived")
	private boolean archived;

	@Column(value = "capability_info")
	private String capabilityInfo;
	
	@Column(value = "enableReplayMission")
	private boolean enableReplayMission;


	/**
	 * 
	 * @return mission_id
	 */
	public UUID getMissionConfig_id() {
		return missionConfig_id;
	}

	/**
	 * return mission_id the mission_id is to set
	 * 
	 * @param missionConfig_id
	 * 
	 */
	public void setMissionConfig_id(UUID missionConfig_id) {
		this.missionConfig_id = missionConfig_id;
	}


	/**
	 * 
	 * @return platform_id
	 */
	public UUID getPlatform_id() {
		return platform_id;
	}

	/**
	 * return platform_id the platform_id is to set
	 * 
	 * @param platform_id
	 */
	public void setPlatform_id(UUID platform_id) {
		this.platform_id = platform_id;
	}
	
	/**
	 * 
	 * @return int
	 */
	public UUID getPlatform_service_id() {
		return platform_service_id;
	}
	
	/**
	 * 
	 * @param platform_service_id
	 */

	public void setPlatform_service_id(UUID platform_service_id) {
		this.platform_service_id = platform_service_id;
	}

	/**
	 * 
	 * @return mission_name
	 */
	public String getMission_name() {
		return mission_name;
	}

	/**
	 * return mission_name the mission_name is to set
	 * 
	 * @param mission_name
	 */
	public void setMission_name(String mission_name) {
		this.mission_name = mission_name;
	}

	/**
	 * 
	 * @return platform_name
	 */
	public String getPlatform_name() {
		return platform_name;
	}

	/**
	 * return platform_name the platform_name is to set
	 * 
	 * @param platform_name
	 */
	public void setPlatform_name(String platform_name) {
		this.platform_name = platform_name;
	}

	/**
	 * 
	 * @return platform_type
	 */
	public String getPlatform_type() {
		return platform_type;
	}

	/**
	 * return platform_type the platform_type is to set
	 * 
	 * @param platform_type
	 */
	public void setPlatform_type(String platform_type) {
		this.platform_type = platform_type;
	}
	/**
	 * 
	 * @return mission_MapConfig
	 */
	public Map<String, String> getMission_MapConfig() {
		return mission_MapConfig;
	}

	/**
	 * return mission_MapConfig the mission_MapConfig is to set
	 * 
	 * @param mission_MapConfig
	 */
	public void setMission_MapConfig(Map<String, String> mission_MapConfig) {
		this.mission_MapConfig = mission_MapConfig;
	}

	/**
	 * 
	 * @return mission_users
	 */
	public Set<String> getMission_users() {
		return mission_users;
	}

	/**
	 * return mission_users the mission_users is to set
	 * 
	 * @param mission_users
	 */
	public void setMission_users(Set<String> mission_users) {
		this.mission_users = mission_users;
	}
	
	/**
	 * 
	 * @return createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * return createdDate the createdDate is to set
	 * 
	 * @param createdDate
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	/**
	 * 
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * return description the description is to set
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 * @return platform_serviceName
	 */
	public String getPlatform_serviceName() {
		return platform_serviceName;
	}
	
	/**
	 * return platform_serviceName the platform_serviceName is to set
	 * 
	 * @param platform_serviceName
	 */
	public void setPlatform_serviceName(String platform_serviceName) {
		this.platform_serviceName = platform_serviceName;
	}
	
	/**
	 * 
	 * @return platform_serviceType
	 */
	public String getPlatform_serviceType() {
		return platform_serviceType;
	}
	
	public boolean getArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public String getYamlServicePackage() {
		return yamlServicePackage;
	}

	public void setYamlServicePackage(String yamlServicePackage) {
		this.yamlServicePackage = yamlServicePackage;
	}
	
	

	public boolean isEnableReplayMission() {
		return enableReplayMission;
	}

	public void setEnableReplayMission(boolean enableReplayMission) {
		this.enableReplayMission = enableReplayMission;
	}

	public String getYamlServicename() {
		return yamlServicename;
	}

	public void setYamlServicename(String yamlServicename) {
		this.yamlServicename = yamlServicename;
	}

	public void setPlatform_serviceType(String platform_serviceType) {
		this.platform_serviceType = platform_serviceType;
	}

	public  List<UDTValue> getServiceNodeParameters() {
		return serviceNodeParameters;
	}

	public void setServiceNodeParameters( List<UDTValue> serviceparameters) {
		this.serviceNodeParameters = serviceparameters;
	}

	public boolean getLogEnabled() {
		return logEnabled;
	}

	public void setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
	}

	public boolean getRecordEnabled() {
		return recordEnabled;
	}

	public void setRecordEnabled(boolean recordEnabled) {
		this.recordEnabled = recordEnabled;
	}
	
	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	
	public String getCapabilityInfo() {
		return capabilityInfo;
	}

	public void setCapabilityInfo(String capabilityInfo) {
		this.capabilityInfo = capabilityInfo;
	}

	@Override
	public String toString() {
		return "MissionConfig [missionConfig_id=" + missionConfig_id + ", platform_id=" + platform_id
				+ ", platform_service_id=" + platform_service_id + ", mission_name=" + mission_name + ", platform_name="
				+ platform_name + ", platform_type=" + platform_type + ", mission_MapConfig=" + mission_MapConfig
				+ ", mission_users=" + mission_users + ", createdDate=" + createdDate + ", description=" + description
				+ ", platform_serviceName=" + platform_serviceName + ", platform_serviceType=" + platform_serviceType
				+ ", serviceNodeParameters=" + serviceNodeParameters + ", yamlServicePackage=" + yamlServicePackage
				+ ", yamlServicename=" + yamlServicename + ", logEnabled=" + logEnabled + ", recordEnabled="
				+ recordEnabled + ", isRequired=" + isRequired + ", archived=" + archived + ", capabilityInfo="
				+ capabilityInfo + ", enableReplayMission=" + enableReplayMission + "]";
	}

	

}
