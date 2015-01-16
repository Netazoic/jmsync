package com.netazoic.netampg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/*
 * Encode project audio files from wav to flac
 */
public class ProjectEncoder {
	
	protected static String locPath;
	protected static String mtpPath;
	protected static String encPath;
	public static String propFileName = "conf/mtpsync.properties";  //This is a default, override if desired
	public static String mtpDir; //e.g., "storage/sdcard1/Music/JTM/Meet The Moores";
	public static Properties props;
	public static boolean flgEncode = false;
	public static MTPSync_Action mtpAction;


	public enum MTPSync_Param{
		dir_SYNC_LOCAL,
		dir_SYNC_MTP,
		dir_ENCODED_FILES
	}
	
	public enum MTPSync_Action{
		PUSH_Files,PULL_Files
	}
	protected static void copyPhoneFiles() throws Throwable{
		pullMTPFiles();
	}
	
	protected static void encodeFiles(){
		File  dir = new File(locPath);
		File[] files = dir.listFiles();
		File destDir = new File(mtpPath);
		Date dTgt, dSrc;
		if(!destDir.exists())  new File(mtpPath).mkdirs();
		File tgt;
		FLAC_Encoder enc = new FLAC_Encoder();
		String tgtPath;
		for(File f : files){
			tgtPath = mtpPath + File.separator + f.getName().replace(".wav", ".flac");
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
	
	public static  Properties getProperties()
			throws IOException {
		Properties props = MTPUtils.getProperties(propFileName);
		if(props == null) props = MTPUtils.createPropertiesFile(propFileName);
		return props;
	}
	
	public static void setProjectPaths(boolean flgEncode) {
		locPath = props.getProperty(MTPSync_Param.dir_SYNC_LOCAL.name());
		mtpPath = props.getProperty(MTPSync_Param.dir_SYNC_MTP.name());
		//destPath = "E:\\Audio Projects\\Songs 4 S\\mixdown\\flac";
		//sourcePath ="E:\\Audio Projects\\Songs 4 S\\mixdown";
		File sourceDir = MTPUtils.chooseDir(locPath,"Select local directory");
		locPath = sourceDir.getAbsolutePath();
		if(flgEncode){
			encPath = props.getProperty(MTPSync_Param.dir_ENCODED_FILES.name());
			File encDir = MTPUtils.chooseDir(encPath,"Select encoding directory");
			encPath = encDir.getAbsolutePath();
		}
		//File destDir = Utils.chooseDir(destPath,"Select destination directory");
		//destPath = destDir.getAbsolutePath();
		mtpPath = mtpDir;
	}
	
	public static void setProperties()
			throws FileNotFoundException, IOException {
		props.put(MTPSync_Param.dir_SYNC_LOCAL.name(), locPath);
		if(encPath!=null)props.put(MTPSync_Param.dir_ENCODED_FILES.name(), encPath);
		props.put(MTPSync_Param.dir_SYNC_MTP.name(), mtpPath);
		MTPUtils.writeProperties(propFileName, props);
	}
	
	public static void pushMTPFiles() throws Throwable{
		MTPUtils.pushFilesADB(encPath,mtpPath);
	}
	
	public static void pullMTPFiles() throws Throwable{
		MTPUtils.pullFilesADB(locPath,mtpPath);
	}
	
	public static void setParams(){
		//Overwrite in project encoder instantiation
		//e.g.,
			//mtpDir = MY_phoneDir;
			//propFileName = MY_propFileName;
		mtpDir="storage/sdcard1/Music/JTM/Meet The Moores";
		propFileName = "mtm.props";
		mtpAction = MTPSync_Action.PULL_Files;
		flgEncode=false;
		
	}
	
	public static void main(String[]args) throws Throwable{
		setParams();
		props = getProperties();

		//If action==copyFromPhone
		if(mtpAction.equals(MTPSync_Action.PULL_Files)){
			setProjectPaths(flgEncode);
			setProperties();
			pullMTPFiles();
		}
		//If action==pushToPhone
		else if(mtpAction.equals(MTPSync_Action.PUSH_Files)){

			setProjectPaths(flgEncode);
			setProperties();
			encodeFiles();
			pushMTPFiles();	

		}
		setProperties();
		System.exit(0);
	}
}
