package com.netazoic.netamtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/*
 * Sync files between local workstation and MTP device (i.e., an Android phone)
 * Optionally encode audio files on the way to MTP device
 */
public class MTPSync {

	protected static String locPath;
	protected static String mtpPath;
	protected static String encPath;
	public static String propFileName = "conf/mtpsync.properties";  //This is a default, override if desired
	//public static String mtpDir; //e.g., "storage/sdcard1/Music/JTM/Meet The Moores";
	public static Properties props;
	public static Boolean flgEncode = null;
	public static MTPSync_Action mtpAction;


	public enum MTPSync_Param{
		dir_SYNC_LOCAL,
		dir_SYNC_MTP,
		dir_ENCODED_FILES, mtpAction, flgEncode
	}

	public enum MTPSync_Action{
		push,pull,clearMTP,dirMTP
	}
	protected static void copyPhoneFiles() throws Throwable{
		pullMTPFiles();
	}

	protected static void encodeFiles(String locPath, String encPath){
		File  dir = new File(locPath);
		File[] files = dir.listFiles();
		File destDir = new File(encPath);
		Date dTgt, dSrc;
		if(!destDir.exists())  new File(encPath).mkdirs();
		File tgt;
		FLAC_Encoder enc = new FLAC_Encoder();
		String tgtPath;
		for(File f : files){
			tgtPath = encPath + File.separator + f.getName().replace(".wav", ".flac");
			tgt = new File(tgtPath);
			//Check to make sure the tgt needs updating
			if(tgt.exists()){
				dTgt = new Date(tgt.lastModified());
				dSrc = new Date(f.lastModified());
				if(dTgt.after(dSrc) || dTgt.equals(dSrc)) continue;
			}
			System.out.println("Encoding " +f.getName());
			enc.encodeFile(f, tgt);
		}
	}

	public static void clearMTPDir(){
		MTPUtils.clearMTPDir(mtpPath);
	}

	public static MTPSync_Action getAction(){
		//mtpAction may have been specified as a param
		//if so, just use that. Otherwise, prompt

		if(mtpAction!=null) return mtpAction;
		else{
			String[] options = {"push","pull","clearMTP","dirMTP"};
			String actionString = MTPUtils.getInput("Sync Action?", options, MTPSync_Action.push.name());
			mtpAction = MTPSync_Action.valueOf(actionString);
		}

		return mtpAction;
	}

	public static boolean getEncoding(){
		flgEncode = MTPUtils.getYesNo("Encode files before pushing?", flgEncode);
		return flgEncode;
	}



	public static  Properties getProperties()
			throws IOException {
		Properties props = MTPUtils.getProperties(propFileName);
		if(props == null) props = MTPUtils.createPropertiesFile(propFileName);
		locPath = props.getProperty(MTPSync_Param.dir_SYNC_LOCAL.name());
		mtpPath = props.getProperty(MTPSync_Param.dir_SYNC_MTP.name());
		String temp = props.getProperty(MTPSync_Param.mtpAction.name());
		if(temp!=null) mtpAction = MTPSync_Action.valueOf(temp);
		temp = props.getProperty(MTPSync_Param.flgEncode.name());
		if(temp!=null) flgEncode = Boolean.parseBoolean(temp);
		return props;
	}



	public static void setProjectPaths(Boolean setLocal, Boolean setMTP, Boolean flgEncode) throws IOException {
		if(setLocal){
			File locDir = MTPUtils.chooseDir(locPath,"Select local directory");
			locPath = locDir.getAbsolutePath();
		}
		if(flgEncode){
			encPath = props.getProperty(MTPSync_Param.dir_ENCODED_FILES.name());
			if(encPath ==null) encPath = locPath;
			File encDir = MTPUtils.chooseDir(encPath,"Select encoding directory");
			encPath = encDir.getAbsolutePath();
		}
		if(setMTP){
			String msg = "Specify MTP directory: e.g., '/sdcard/Music':";
			mtpPath = MTPUtils.getInput(msg, mtpPath);
			MTPUtils.verifyMTPDir(mtpPath);
		}
	}

	public static void pushMTPFiles() throws Throwable{
		//A/B comparison

		boolean flgADB = false;

		if(!flgADB){
			if(flgEncode)MTPUtils.pushFiles(encPath,mtpPath);
			else MTPUtils.pushFiles(locPath, mtpPath);
		}
		else if(flgADB){
			if(flgEncode)MTPUtils.pushFilesADB(encPath, mtpPath);
			else MTPUtils.pushFilesADB(locPath, mtpPath);
		}
	}

	public static void pullMTPFiles() throws Throwable{
		boolean flgADB = false;
		if(flgADB)	MTPUtils.pullSyncFilesADB(locPath,mtpPath);
		else MTPUtils.pullFiles(locPath, mtpPath);
	}

	public static void setParams(){
		//Overwrite in project encoder instantiation
		//e.g.,
		//mtpDir = MY_phoneDir;
		//propFileName = MY_propFileName;
		mtpPath="storage/sdcard1/Music/JTM/Meet The Moores";
		propFileName = "conf/mtm.props";
		//mtpAction = MTPSync_Action.dirMTP;
		//flgEncode=false;

	}

	public static void writeProperties()
			throws FileNotFoundException, IOException {
		props.put(MTPSync_Param.dir_SYNC_LOCAL.name(), locPath);
		if(encPath!=null)props.put(MTPSync_Param.dir_ENCODED_FILES.name(), encPath);
		props.put(MTPSync_Param.dir_SYNC_MTP.name(), mtpPath);
		//props.put(MTPSync_Param.mtpAction.name(), mtpAction.name());
		if(flgEncode==null) flgEncode = false;
		props.put(MTPSync_Param.flgEncode.name(), flgEncode.toString());
		MTPUtils.writeProperties(propFileName, props);
	}

	public static void main(String[]args) throws Throwable{
		setParams();
		props = getProperties();
		mtpAction = getAction();
		//If action==copyFromPhone
		switch(mtpAction){
		case pull:
			setProjectPaths(true,true,false);
			pullMTPFiles();
			break;
		case push:
			if(flgEncode == null) getEncoding();
			setProjectPaths(true,true,flgEncode);
			if(flgEncode) encodeFiles(locPath,encPath);
			pushMTPFiles();
			break;
		case clearMTP:
			setProjectPaths(false,true,false);
			clearMTPDir();
			break;
		case dirMTP:
			setProjectPaths(false,true,false);
			MTPUtils.verifyMTPDir(mtpPath);
			MTPUtils.lsDir(mtpPath);
			//MTPUtils.lsDir_ADB(mtpPath);
			break;

		}
		writeProperties();
		System.exit(0);
	}
}
