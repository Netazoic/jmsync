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
	
	protected static String sourcePath;
	protected static String destPath;
	protected static String encPath;
	public static String propFileName = "conf/mtpsync.properties";  //This is a default, override if desired
	public static String phoneDir; //e.g., "storage/sdcard1/Music/JTM/Meet The Moores";
	public static Properties props;


	public enum MTPSync_Param{
		dir_SYNC_SOURCE,
		dir_SYNC_TARGET,
		dir_ENCODED_FILES
	}
	protected static void encodeFiles(){
		File  dir = new File(sourcePath);
		File[] files = dir.listFiles();
		File destDir = new File(destPath);
		Date dTgt, dSrc;
		if(!destDir.exists())  new File(destPath).mkdirs();
		File tgt;
		FLAC_Encoder enc = new FLAC_Encoder();
		String tgtPath;
		for(File f : files){
			tgtPath = destPath + File.separator + f.getName().replace(".wav", ".flac");
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
	
	public static void setProjectPaths() {
		sourcePath = props.getProperty(MTPSync_Param.dir_SYNC_SOURCE.name());
		destPath = props.getProperty(MTPSync_Param.dir_SYNC_TARGET.name());
		encPath = props.getProperty(MTPSync_Param.dir_ENCODED_FILES.name());
		//destPath = "E:\\Audio Projects\\Songs 4 S\\mixdown\\flac";
		//sourcePath ="E:\\Audio Projects\\Songs 4 S\\mixdown";
		File sourceDir = MTPUtils.chooseDir(sourcePath,"Select source directory");
		sourcePath = sourceDir.getAbsolutePath();

		File encDir = MTPUtils.chooseDir(encPath,"Select encoding directory");
		encPath = encDir.getAbsolutePath();
		
		//File destDir = Utils.chooseDir(destPath,"Select destination directory");
		//destPath = destDir.getAbsolutePath();
		destPath = phoneDir;
	}
	
	public static void setProperties()
			throws FileNotFoundException, IOException {
		props.put(MTPSync_Param.dir_SYNC_SOURCE.name(), sourcePath);
		props.put(MTPSync_Param.dir_ENCODED_FILES.name(), encPath);
		props.put(MTPSync_Param.dir_SYNC_TARGET.name(), destPath);
		MTPUtils.writeProperties(propFileName, props);
	}
	
	public static void updateMTPFiles() throws Throwable{
		MTPUtils.updateFilesADB(encPath,destPath);
	}
	
	public static void setParams(){
		//Overwrite in project encoder instantiation
		//e.g.,
			//phoneDir = MY_phoneDir;
			//propFileName = MY_propFileName;
	}
	
	public static void main(String[]args) throws Throwable{
		setParams();
		props = getProperties();
		setProjectPaths();
		encodeFiles();
		updateMTPFiles();	
		setProperties();
		System.exit(0);
	}
}
