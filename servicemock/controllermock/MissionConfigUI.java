package drdo.cair.isrd.icrs.mcs.entity;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;



public class MissionConfigUI {

	@NotEmpty 
	@NotNull
	String missionName;
	
	UUID missionConfigId;
	
	@NotEmpty
	@NotNull
	String missionDes;
	
	Date createdDate;
	
	boolean archived;
	
	boolean enableReplayMission;
	
	@NotEmpty
	Set<String> missionUsers;
	
	MapSettings mapSettings;
	
	List<PlatformUI> platforms;
	
	
	public Set<String> getMissionUsers() {
		return missionUsers;
	}

	public void setMissionUsers(Set<String> missionUsers) {
		this.missionUsers = missionUsers;
	}

	public List<PlatformUI> getPlatforms() {
		return platforms;
	}
	public void setPlatforms(List<PlatformUI> platforms) {
		this.platforms = platforms;
	}
	public String getMissionName() {
		return missionName;
	}
	public void setMissionName(String missionName) {
		this.missionName = missionName;
	}
	public String getMissionDes() {
		return missionDes;
	}
	public void setMissionDes(String missionDes) {
		this.missionDes = missionDes;
	}
	public MapSettings getMapSettings() {
		return mapSettings;
	}
	public void setMapSettings(MapSettings mapSettings) {
		this.mapSettings = mapSettings;
	}
	
	public UUID getMissionConfigId() {
		return missionConfigId;
	}

	public void setMissionConfigId(UUID missionConfigId) {
		this.missionConfigId = missionConfigId;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public boolean isEnableReplayMission() {
		return enableReplayMission;
	}

	public void setEnableReplayMission(boolean enableReplayMission) {
		this.enableReplayMission = enableReplayMission;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public MissionConfigUI() {
		super();
	}
   
	@Override
	public String toString() {
		return "MissionConfigUI [missionName=" + missionName + ", missionConfigId=" + missionConfigId + ", missionDes="
				+ missionDes + ", createdDate=" + createdDate + ", archived=" + archived + ", enableReplayMission="
				+ enableReplayMission + ", missionUsers=" + missionUsers + ", mapSettings=" + mapSettings
				+ ", platforms=" + platforms + "]";
	}

}
