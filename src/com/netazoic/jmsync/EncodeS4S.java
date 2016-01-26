package com.netazoic.jmsync;



public class EncodeS4S extends JMSync{
	public static String MY_phoneDir = "storage/sdcard1/Music/JTM/Meet The Moores";
	public static String MY_propFileName = "conf/s4s_sync.properties";



	public static void setParams(){
		mtpPath = MY_phoneDir;
		propFileName = MY_propFileName;
		flgEncode = true;
	}
	
	public static void main(String[]args) throws Throwable{
		setParams();
		setProjectPaths();
		encodeFiles(locPath,encPath,false);
		pushMTPFiles();	
		writeProperties();
		System.exit(0);
	}





























}
