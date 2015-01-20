package com.netazoic.netamtp;



public class EncodeS4S extends MTPSync{
	public static String MY_phoneDir = "storage/sdcard1/Music/JTM/Meet The Moores";
	public static String MY_propFileName = "conf/s4s_sync.properties";



	public static void setParams(){
		mtpPath = MY_phoneDir;
		propFileName = MY_propFileName;
		flgEncode = true;
	}
	
	public static void main(String[]args) throws Throwable{
		setParams();
		props = getProperties();
		setProjectPaths(true,true,flgEncode);
		encodeFiles(locPath,encPath);
		pushMTPFiles();	
		writeProperties();
		System.exit(0);
	}





























}
