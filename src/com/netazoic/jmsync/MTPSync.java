package com.netazoic.jmsync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.netazoic.jmsync.itfc.itfc_Encoder.ENC_Format;

/*
 * Sync files between local workstation and MTP device (i.e., an Android phone)
 * Optionally encode audio files on the way to MTP device
 */
public class MTPSync {

	protected static String locPath;
	protected static String mtpPath;
	protected static String encPath;
	public static String pCode;  //project name
	public static String propFileName = "conf/mtm.properties";  //This is a default, override if desired
	public static String mainPropFileName = "conf/mtpsync.properties";
	//public static String mtpDir; //e.g., "storage/sdcard1/Music/JTM/Meet The Moores";
	public static Properties props;
	public static Boolean flgEncode = null;
	public static ENC_Format encFormat;
	public static Boolean flgForce = false;
	public static MTPSync_Action mtpAction;
	public static MTPSync_Action mtpLastAction;
	private static HashMap settings;

	public enum MTPSync_Param{
		dir_SYNC_LOCAL,
		dir_SYNC_MTP,
		dir_ENCODED_FILES, mtpAction, flgEncode, pCode, 
		encFormat
	}

	public enum MTPSync_Action{
		push,pull,clearMTP,dirMTP
	}


	public static void clearMTPDir(){
		MTPUtils.rm(mtpPath);
	}

	protected static void encodeFiles(String locPath, String encPath, boolean flgForce) throws Exception {
		File  dir = new File(locPath);
		File[] files = dir.listFiles();
		File destDir = new File(encPath);
		Date dTgt, dSrc;
		if(!destDir.exists())  new File(encPath).mkdirs();
		File tgt;
		Encoder<?> enc = (Encoder)encFormat.encClass.newInstance();
		enc.encFormat = encFormat;
		String tgtPath;
		try{
			for(File f : files){
				if(f.isDirectory()) continue;
				tgtPath = encPath + File.separator + f.getName().replace(".wav", "." + enc.getExtension());
				tgt = new File(tgtPath);
				//Check to make sure the tgt needs updating
				//unless flgForce is in effect
				if(!flgForce && tgt.exists()){
					dTgt = new Date(tgt.lastModified());
					dSrc = new Date(f.lastModified());
					if(dTgt.after(dSrc) || dTgt.equals(dSrc)) continue;
				}
				System.out.println("Encoding " +f.getName());
				enc.encodeFile(f, tgt);
			}
		}catch(UnsupportedAudioFileException ex){
			throw new Exception(ex);
		}catch(IOException ex){
			throw new Exception(ex);
		}
	}

	public static MTPSync_Action getAction(){
		//mtpAction may have been specified as a param
		//if so, just use that. Otherwise, prompt

		if(mtpAction!=null) return mtpAction;
		else{
			MTPSync_Action dfltAction;
			if(mtpLastAction!=null) dfltAction=mtpLastAction;
			else dfltAction = MTPSync_Action.push;
			String[] options = {"push","pull","clearMTP","dirMTP"};
			String actionString = SyncUtils.getInput("Sync Action?", options, dfltAction.name());
			mtpAction = MTPSync_Action.valueOf(actionString);
		}

		return mtpAction;
	}

	public static boolean getEncoding(){
		if(mtpAction.equals(MTPSync_Action.push)) flgEncode = true;
		else flgEncode = false;
		//if(mtpAction.equals(MTPSync_Action.push)) flgEncode = SyncUtils.getYesNo("Encode files before pushing?", flgEncode);
		if(flgEncode){
			String[] options = {"none","mp3","flac"};
			String defaultOpt = encFormat!=null?encFormat.name():"flac";
			String temp = SyncUtils.getInput("Encoding format:", options,defaultOpt);
			if(temp == null) System.exit(1);
			ENC_Format newFormat = ENC_Format.valueOf(temp);
			encFormat = newFormat;
			if(encFormat.equals(ENC_Format.none)){
				flgEncode = false;
				encPath = null;
			}
			else encPath = locPath + File.separator + encFormat.name();
		}
		return flgEncode;
	}



	public static  Properties getProperties(String myPropFileName)
			throws IOException {
		Properties props = SyncUtils.getProperties(myPropFileName);
		if(props == null) props = SyncUtils.createPropertiesFile(myPropFileName);
		locPath = props.getProperty(MTPSync_Param.dir_SYNC_LOCAL.name());
		mtpPath = props.getProperty(MTPSync_Param.dir_SYNC_MTP.name());
		String temp = props.getProperty(MTPSync_Param.mtpAction.name());
		if(temp!=null) mtpLastAction = MTPSync_Action.valueOf(temp);
		temp = props.getProperty(MTPSync_Param.flgEncode.name());
		if(temp!=null) flgEncode = Boolean.parseBoolean(temp);
		temp = props.getProperty(MTPSync_Param.encFormat.name());
		if(temp!=null) encFormat = ENC_Format.valueOf(temp);
		return props;
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}

	public static void pushMTPFiles() throws Throwable{
		if(flgEncode)MTPUtils.pushSyncFilesADB(encPath, mtpPath);
		else MTPUtils.pushSyncFilesADB(locPath, mtpPath);
	}

	public static void pullMTPFiles() throws Throwable{
		MTPUtils.pullSyncFilesADB(locPath,mtpPath);
	}

	public static void setParams(String[] args) throws IOException{
		int i=0;
		if(args == null ) return;
		settings = new HashMap<String,String>();
		for(String k : args){
			if(k.equals("-mtpPath")){
				mtpPath = k;
			}
			if(k.equals("-pCode")){
				pCode = args[i+1];
				propFileName = "conf/" + pCode + ".properties";
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
			if(k.equals("-f")){
				flgForce=true;
			}
			if(k.equals("-u") || k.equals("--usage")){
				usage();
			}
			if(k.equals("-h") || k.equals("--help")){
				usage();
			}
			i++;

		}
		if(pCode == null){
			Properties mainProps = SyncUtils.getProperties(mainPropFileName);
			String lastPCode = mainProps.getProperty(MTPSync_Param.pCode.name());
			pCode = SyncUtils.getInput("Short code for the project you are syncing:", lastPCode);
			if(pCode == null) System.exit(1);
			propFileName = "conf/" + pCode + ".properties";
		}
		if(propFileName == null) propFileName = "conf/" + pCode + ".properties";
		props = getProperties(propFileName);
		mtpAction = getAction();
		getEncoding();
		setProjectPaths();
	}

	public static void setProjectPaths() throws IOException {

			if(locPath == null){
				File locDir = SyncUtils.chooseDir(locPath,"Select local directory");
				locPath = locDir.getAbsolutePath();
				if(locPath==null) System.exit(1);
			}

			if(mtpPath == null){
				File f = new File(System.getProperty("user.home"), "Desktop");
				f.listFiles();
				String msg = "Specify MTP directory: e.g., '/sdcard/Music':";
				mtpPath = SyncUtils.getInput(msg, mtpPath);
				if(mtpPath==null) System.exit(1);
				//MTPUtils.verifyMTPDir(mtpPath);
			}

	}

	public static void usage(){
		out("jmsync.MTPSync: usage:");
		out("\tjava MTPSync -h");
		out("\tjava MTPSync [<project-code>]  [<prop-file>] [-f] [<action:push|pull>]");
		out("");
		out("params:");
		out(" -f      :  force re-encoding even if source files not updated");
		System.exit(1);
	}

	public static void writeProperties()
			throws FileNotFoundException, IOException {
		props.put(MTPSync_Param.pCode.name(), pCode);
		props.put(MTPSync_Param.dir_SYNC_LOCAL.name(), locPath);
		if(encPath!=null)props.put(MTPSync_Param.dir_ENCODED_FILES.name(), encPath);
		props.put(MTPSync_Param.dir_SYNC_MTP.name(), mtpPath);
		//props.put(MTPSync_Param.mtpAction.name(), mtpAction.name());
		if(flgEncode==null) flgEncode = false;
		props.put(MTPSync_Param.flgEncode.name(), flgEncode.toString());
		if(encFormat!=null) props.put(MTPSync_Param.encFormat.name(), encFormat.name());
		SyncUtils.writeProperties(propFileName, props);

		//Update the main properties file with the code of most recent project
		Properties mainProps = SyncUtils.getProperties(propFileName);
		mainProps.put(MTPSync_Param.pCode.name(), pCode);
		SyncUtils.writeProperties(mainPropFileName, mainProps);

	}

	public static void main(String[]args) throws Throwable{
		setParams(args);
		writeProperties();
		GITSync git = new GITSync();

		boolean flgLocal, flgMTP  = false;
		//If action==copyFromPhone
		try{
			switch(mtpAction){
			case pull:
				pullMTPFiles();
				break;
			case push:
				if(flgEncode) encodeFiles(locPath,encPath,false);
				pushMTPFiles();
				break;
			case clearMTP:
				clearMTPDir();
				break;
			case dirMTP:
				MTPUtils.verifyMTPDir(mtpPath);
				MTPUtils.lsDir(mtpPath);
				break;

			}
		git.pCode = pCode;	
		git.run();
		}catch(Exception ex){
			System.out.print(ex.getMessage());
			ex.printStackTrace();
		}finally{
			System.exit(0);
		}
		

	}
}
