package com.netazoic.netamtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/*
 * Sync files between local workstation and MTP device (i.e., an Android phone)
 * Optionally encode audio files on the way to MTP device
 */
public class MTPSync {

	protected static String locPath;
	protected static String mtpPath;
	protected static String encPath;
	public static String pName;  //project name
	public static String propFileName = "conf/mtpsync.properties";  //This is a default, override if desired
	//public static String mtpDir; //e.g., "storage/sdcard1/Music/JTM/Meet The Moores";
	public static Properties props;
	public static Boolean flgEncode = null;
	public static MTPSync_Action mtpAction;
	private static HashMap settings;

	public enum MTPSync_Param{
		dir_SYNC_LOCAL,
		dir_SYNC_MTP,
		dir_ENCODED_FILES, mtpAction, flgEncode, pName
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
		//TODO
		//MTPUtils.clearMTPDir(mtpPath);
	}

	public static MTPSync_Action getAction(){
		//mtpAction may have been specified as a param
		//if so, just use that. Otherwise, prompt

		if(mtpAction!=null) return mtpAction;
		else{
			String[] options = {"push","pull","clearMTP","dirMTP"};
			String actionString = SyncUtils.getInput("Sync Action?", options, MTPSync_Action.push.name());
			mtpAction = MTPSync_Action.valueOf(actionString);
		}

		return mtpAction;
	}

	public static boolean getEncoding(){
		flgEncode = SyncUtils.getYesNo("Encode files before pushing?", flgEncode);
		return flgEncode;
	}



	public static  Properties getProperties()
			throws IOException {
		Properties props = SyncUtils.getProperties(propFileName);
		if(props == null) props = SyncUtils.createPropertiesFile(propFileName);
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
			File locDir = SyncUtils.chooseDir(locPath,"Select local directory");
			locPath = locDir.getAbsolutePath();
		}
		if(flgEncode){
			encPath = props.getProperty(MTPSync_Param.dir_ENCODED_FILES.name());
			if(encPath ==null) encPath = locPath;
			File encDir = SyncUtils.chooseDir(encPath,"Select encoding directory");
			encPath = encDir.getAbsolutePath();
		}
		if(setMTP){
			File f = new File(System.getProperty("user.home"), "Desktop");
			f.listFiles();
			String msg = "Specify MTP directory: e.g., '/sdcard/Music':";
			mtpPath = SyncUtils.getInput(msg, mtpPath);
			//MTPUtils.verifyMTPDir(mtpPath);
		}
		writeProperties();
	}

	public static void pushMTPFiles() throws Throwable{
		if(flgEncode)MTPUtils.pushSyncFilesADB(encPath, mtpPath);
		else MTPUtils.pushSyncFilesADB(locPath, mtpPath);
	}

	public static void pullMTPFiles() throws Throwable{
		MTPUtils.pullSyncFilesADB(locPath,mtpPath);
	}

	public static void setParams(String[] args){
		int i=0;
		if(args == null ) return;
		settings = new HashMap<String,String>();
		for(String k : args){
			if(k.equals("-mtpPath")){
				mtpPath = k;
			}
			if(k.equals("-pName")){
				pName = args[i+1];
				propFileName = "conf/" + pName + ".properties";
			}
			if(k.contains(".properties")){
				propFileName = k;
			}
			if(k.equals("push")){
				mtpAction=MTPSync_Action.push;
			}
			if(k.equals("pull")){
				mtpAction=MTPSync_Action.pull;
			}
			i++;

		}


	}

	public static void writeProperties()
			throws FileNotFoundException, IOException {
		props.put(MTPSync_Param.dir_SYNC_LOCAL.name(), locPath);
		if(encPath!=null)props.put(MTPSync_Param.dir_ENCODED_FILES.name(), encPath);
		props.put(MTPSync_Param.dir_SYNC_MTP.name(), mtpPath);
		//props.put(MTPSync_Param.mtpAction.name(), mtpAction.name());
		if(flgEncode==null) flgEncode = false;
		props.put(MTPSync_Param.flgEncode.name(), flgEncode.toString());
		SyncUtils.writeProperties(propFileName, props);
	}

	public static void main(String[]args) throws Throwable{
		setParams(args);
		props = getProperties();
		mtpAction = getAction();
		boolean flgLocal, flgMTP  = false;
		//If action==copyFromPhone
		try{
			switch(mtpAction){
			case pull:
				flgLocal=true;
				flgMTP=true;
				flgEncode = false;
				setProjectPaths(flgLocal,flgMTP,flgEncode);
				pullMTPFiles();
				break;
			case push:
				flgLocal=true;flgMTP=true;
				if(flgEncode == null) getEncoding();
				setProjectPaths(flgLocal,flgMTP,flgEncode);
				if(flgEncode) encodeFiles(locPath,encPath);
				pushMTPFiles();
				break;
			case clearMTP:
				flgLocal=false;flgMTP=true;flgEncode=false;
				setProjectPaths(flgLocal,flgMTP,flgEncode);
				clearMTPDir();
				break;
			case dirMTP:
				flgLocal=false;flgMTP=true;flgEncode=false;
				setProjectPaths(flgLocal,flgMTP,flgEncode);
				MTPUtils.verifyMTPDir(mtpPath);
				MTPUtils.lsDir(mtpPath);
				break;

			}
		}catch(Exception ex){
			System.out.print(ex.getMessage());
			ex.printStackTrace();
		}finally{
			System.exit(0);
		}

	}
}
