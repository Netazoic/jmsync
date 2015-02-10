package com.netazoic.jmsync;
import jpmp.manager.DeviceManager;

public class usbtest {

	
	
	public static void main(String[] args) throws Throwable{
		 DeviceManager dm = DeviceManager.getInstance();
		  dm.createInstance();
		  dm.scanDevices();
		  dm.dump();
	}
}
