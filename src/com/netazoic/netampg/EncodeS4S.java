package com.netazoic.netampg;



public class EncodeS4S extends ProjectEncoder{
	public static String MY_phoneDir = "storage/sdcard1/Music/JTM/Meet The Moores";
	public static String MY_propFileName = "conf/s4s_sync.properties";



	public static void setParams(){
		mtpDir = MY_phoneDir;
		propFileName = MY_propFileName;
		flgEncode = true;
	}
	
	public static void main(String[]args) throws Throwable{
		setParams();
		props = getProperties();
		setProjectPaths(flgEncode);
		encodeFiles();
		pushMTPFiles();	
		setProperties();
		System.exit(0);
	}





























}
