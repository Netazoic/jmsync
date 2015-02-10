package com.netazoic.jmsync;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MTPUtils {
    private static final String[] WIN_RUNTIME = { "cmd.exe", "/C" };
    private static final String[] OS_LINUX_RUNTIME = { "/bin/bash", "-l", "-c" };

    private MTPUtils() {
    }

	public static class FileMTP{
		String fName;
		Integer fSize;
		Date lastUpdate;
	}
    
	private static boolean checkPathExists(String mtpPath) {
		String cmd = "adb shell \"if [ -d  \'" + mtpPath + "\' ]; then echo \'1\'; else echo \'0\'; fi;\" ";
		List<String>rtn = SyncUtils.runProcess(true, cmd);
		if(rtn.get(0).equals("1")) return true;
		else return false;
	}

	private static void copyFilesADB(String srcPath,String destPath) throws Throwable{
		String cmd = "adb push \"" +  srcPath + "\" \"" + destPath + "\"";
		List<String> ret = SyncUtils.runProcess(true, cmd);

		//seeFiles(null);

	}
    
    private static List<String> getADBDir(String dir) {
		String cmd = "adb shell ls -ls \"" + dir + "\"";
		List<String> destDirList = SyncUtils.runProcess(true, cmd);
		//Check for connection errors
		String string1 = destDirList.get(0);
		if(!string1.startsWith("total")){
			//ADB not initialized, try again
			destDirList = SyncUtils.runProcess(true, cmd);
			string1 = destDirList.get(0);
			if(string1.matches("error: device.*")){
				System.out.println(string1);
				return null;
			}
			if(!string1.startsWith("total")){
				System.out.println(string1);
				return null;
			}
	
		}
		return destDirList;
	}

	private static Map<String,FileMTP> loadMTPDirMap(String dir) throws ParseException{
		List<String> destDirList = getADBDir(dir);
		Map map = new HashMap<String,FileMTP>();
		String temp,temp2,perms,group,owner,fName;
		int idx,fileSize;
		Date lastUpdate;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	
		for(String ls : destDirList){
			idx = 0;
			if(ls == null || ls.equals("")) continue;
			temp = ls.substring(0,ls.indexOf(" "));
			if(temp.equals("total")) continue;
			perms = temp;
			//Check to see if this is a directory
			if(perms.charAt(0) == 'd'){
				//directory
				continue;
			}
			idx  = idx + temp.length() + 1;
			temp = ls.substring(idx,ls.indexOf(" ", idx));
			owner = temp.trim();
			idx  = idx + temp.length() + 1;
			while(ls.charAt(idx) == ' ' ) idx ++;
			temp = ls.substring(idx,ls.indexOf(" ",idx));
			group = temp.trim();
			idx  = idx + temp.length() + 1;
			while(ls.charAt(idx) == ' ' ) idx ++;
			temp = ls.substring(idx,ls.indexOf(" ",idx));
			fileSize = Integer.parseInt(temp.trim());
			idx  = idx + temp.length() + 1;
			while(ls.charAt(idx) == ' ' ) idx ++;
			temp = ls.substring(idx,ls.indexOf(" ",idx));
			temp += " ";
			idx = idx + temp.length();
			temp2 = ls.substring(idx,ls.indexOf(" ",idx));	//Pickup  the time
			temp += temp2;
			lastUpdate = dateFormat.parse(temp);
			idx  = idx + temp2.length() + 1;
			while(ls.charAt(idx) == ' ' ) idx ++;
			fName = ls.substring(idx).trim();
	
			FileMTP fls = new FileMTP();
			fls.fName = fName;
			fls.fSize = fileSize;
			fls.lastUpdate = lastUpdate;
			map.put(fName, fls);
		}
		return map;
	}
	
	
	public static void lsDir(String dirPath){
		List<String> ls = MTPUtils.getADBDir(dirPath);
		for (String l : ls){
			System.out.println(l);
		}
	}
	
	


	private static void mkDir(String mtpPath) {
		String cmd = "adb shell \"mkdir \'" + mtpPath + "\'\"";
		List<String>rtn = SyncUtils.runProcess(true, cmd);
	}

	public static void pullFilesADB(String locPath,String mtpPath){
		//String cmd = "cd " + locPath;
		//SyncUtils.runProcess(true, cmd);
		String cmd = "adb pull \"" + mtpPath + "\" \"" + locPath + "\"";
	
		SyncUtils.runProcess(true, cmd);
	}
	
	
	public static void pullSyncFilesADB(String locPath,String mtpPath) throws Throwable{
		//Pull any updated files from the phone
		File mtpDir = new File(mtpPath);
		int ctPull=0;
		File locDir = new File(locPath);
		Map<String,FileMTP> mapMTP  = loadMTPDirMap(mtpPath);
		if(mapMTP == null){
			System.out.println("Problem loading the MTP directory.");
			return;
		}
		File[] fileList = locDir.listFiles();
		Map<String,File> mapLoc = new HashMap<String,File>();
		for (File f : fileList){
			mapLoc.put(f.getName(), f);
		}
		String cmd;
		FileMTP mtpFile = null;
		File locFile = null;
		Date locLastMod = null;
		boolean flgPull = false;
		for (String k : mapMTP.keySet()){
			mtpFile = mapMTP.get(k);
			flgPull = false;
			locFile = mapLoc.get(mtpFile.fName);
			if(locFile == null) flgPull = true;
			if(locFile != null){
				locLastMod = new Date(locFile.lastModified());
				if(mtpFile.lastUpdate.after(locLastMod)) flgPull = true;
				
				//optional, check size. If file size is the same, probably the same file even
				//if modification dates are different
				if(mtpFile.fSize == locFile.length()){
					System.out.println("Skipping " + mtpFile.fName + ": local file appears to be same size.");
					flgPull = false;
				}
			}
			if(flgPull){
				ctPull++;
				System.out.println("Pulling " + mtpFile.fName + " to " +  locDir.getName());
				cmd = "adb pull \"" + mtpPath + "/" + mtpFile.fName + "\" \"" + locPath + "\"";

				List<String> ret2 = SyncUtils.runProcess(true, cmd);
			}
		}
		System.out.println("Pulled " + ctPull + " files from MTP source.");
	
	}
	

	
	
	public static void pushSyncFilesADB(String locPath,String mtpPath) throws Throwable{
		//push any updated files to phone
		File mtpDir = new File(mtpPath);
		int ctPush=0;
		File srcDir = new File(locPath);
		String cmd;
		if (!checkPathExists(mtpPath)){
			mkDir(mtpPath);
			if(!checkPathExists(mtpPath)){
				System.out.println("Could not create directory " + mtpPath);
				return;
			}
		}
		//String DIR_RESULT=$(adb shell if [ -d  "/data/textdata" ]; then echo "exists"; else echo "not exists"; fi; );
		Map<String,FileMTP> mapMTP  = loadMTPDirMap(mtpPath);
		if(mapMTP == null){
			System.out.println("Problem loading the MTP directory.");
			return;
		}
		FileMTP mtpFile;
		File[] fileList = srcDir.listFiles();
		boolean flgPush = false;
		for (File locFile : fileList){
			flgPush = false;
			String n = locFile.getName();
			mtpFile = mapMTP.get(locFile.getName());
			Date locLastMod = new Date(locFile.lastModified());
			if(mtpFile == null) flgPush = true;
			if(mtpFile != null && mtpFile.lastUpdate.before(locLastMod)) flgPush = true;
			//optional, check size. If file size is the same, probably the same file even
			//if modification dates are different
			if(mtpFile != null && mtpFile.fSize == locFile.length()) flgPush = false;
			if(flgPush){
				ctPush++;
				System.out.println("Pushing " + locFile.getName() + " to " + mtpPath);
				cmd = "adb push \"" +  locPath + File.separator + locFile.getName() + "\" \"" + mtpPath + "\"";
				List<String> ret2 = SyncUtils.runProcess(true, cmd);
			}
		}
		System.out.println("Pushed " + ctPush + " files to MTP location.");
	
	}
	public static Boolean verifyMTPDir(String dir) throws IOException {
		boolean rtn = checkPathExists(dir);
		try{
			if(!rtn) throw new IOException("Directory '" +dir + "' does not seem to exist.");
		}catch(IOException ex){
			throw ex;
		}
		return rtn;
	}
    
}