package com.example.connectassistantforandroid;

// This class describes each object of the Devices class
// Each device object consists of an icon, ID, name and status
// The setters set the device attributes
// The getters retrieve the device attributes
public class Devices {
	private int iconId;
	private int deviceId;
	private String name;
	private String status;
	
	// This method is a constructor of the Devices class
	public Devices(int iconId, int deviceId, String name, String status) {
		super();
		this.iconId = iconId;
		this.deviceId = deviceId;
		this.name = name;
		this.status = status;
	}
	
	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
