package com.netazoic.netamtp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

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
    
    public static File chooseDir(String startPath,String dlgTitle){
    	boolean flgDebug = false;
		JFileChooser chooser = new JFileChooser(startPath);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File f = null;
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Audio Files", "wav", "mp3", "flac");
		//chooser.setFileFilter(filter);
		int returnVal = chooser.showDialog(new JFrame(""),dlgTitle);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			if(flgDebug) System.out.println("You chose to open this file: " +
					f.getName());
		}
		return f;
	}
	public static File chooseFile(){
		JFileChooser chooser = new JFileChooser();
		File f = null;
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Audio Files", "wav", "mp3", "flac");
		//chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(new JFrame(""));
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			System.out.println("You chose to open this file: " +
					f.getName());
		}
		return f;
	}
	private static <T> T[] concat(T[] first, T[] second) {
	    T[] result = Arrays.copyOf(first, first.length + second.length);
	    System.arraycopy(second, 0, result, first.length, second.length);
	    return result;
	}
	private static void copyFile(String srcPath,String destPath) throws Throwable{
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		if(!destFile.exists()) new File(destPath).mkdirs();
		destFile = new File(destPath);
		Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	
	}
	private static void copyFile(File srcFile,File destFile) throws Throwable{
		if(!destFile.exists()){
			new File(destFile.getAbsolutePath()).mkdirs();
			destFile = new File(destFile.getAbsolutePath());
		}
		Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	private static void copyFiles(String srcPath,String destPath) throws Throwable{
		File srcDir = new File(srcPath);
		File destDir = new File(destPath);
		if(!destDir.exists()) new File(destPath).mkdirs();
		destDir = new File(destPath);
		Files.copy(srcDir.toPath(), destDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
	
	}
	private static void copyFilesADB(String srcPath,String destPath) throws Throwable{
		String cmd = "adb push \"" +  srcPath + "\" \"" + destPath + "\"";
		List<String> ret = MTPUtils.runProcess(true, cmd);

		//seeFiles(null);

	}
    public static Properties createPropertiesFile(String propFileName) throws IOException{
    	Properties prop = new Properties();
    	File f = new File(propFileName);
    	FileOutputStream fout = new FileOutputStream(f);
    	prop.store(fout, propFileName);
    	fout.close();
    	return prop;
    }
    
    private static List<String> getADBDir(String dir) {
		String cmd = "adb shell ls -ls \"" + dir + "\"";
		List<String> destDirList = MTPUtils.runProcess(true, cmd);
		//Check for connection errors
		String string1 = destDirList.get(0);
		if(!string1.startsWith("total")){
			//ADB not initialized, try again
			destDirList = MTPUtils.runProcess(true, cmd);
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
	public static String getInput(String msg, String defaultInput){
		// msg = something like: "Please enter the MTP directory, e.g., '/sdcard/Music'"
	
		String input = (String) JOptionPane.showInputDialog(msg, defaultInput);
		
		return input;
	
	}
    
    public static String getInput(String msg, String[] options, String defaultInput){
		// msg = something like: "Please enter the MTP directory, e.g., '/sdcard/Music'"
	
		String input = (String)JOptionPane.showInputDialog(
                null,
                msg,
                "Please select an option",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                defaultInput);		
		return input;
	
	}
	public static Properties getProperties(String propFileName) throws IOException {   	 
		Properties prop = new Properties();
		//String propFileName = "config.properties";
		MTPUtils utils = new MTPUtils();
		InputStream inputStream = utils.getClass().getClassLoader().getResourceAsStream(propFileName);
		if(inputStream == null){
			try{
				File f = new File(propFileName);
				f.createNewFile();
				if(!f.exists()) throw new IOException("File could not be created.");
				inputStream = new FileInputStream(propFileName);
			}catch(IOException ex){
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath and could not be created.");
			}
		}
		if (inputStream != null) {
			prop.load(inputStream);
		} 
		inputStream.close();
		return prop;
	}

    public String getPropValue(String propName, String propFileName) throws IOException {
		 
		String propVal = "";
		Properties prop = getProperties(propFileName);
	
		propVal = prop.getProperty(propName);
		return propVal;
	}
    
	public static boolean getYesNo(String settingQ, Boolean flg){
		String[] options = {"Yes","No"};
		String defaultAnswer = flg !=null && flg==true?"Yes":"No";
		String input = MTPUtils.getInput(settingQ, options, defaultAnswer);
		return (input.equals("Yes")?true:false);

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
		File dir = new File(dirPath);
		System.out.println("Directory listing for " + dirPath);
		for(File f : dir.listFiles()){
			System.out.println(f.getName());
		}
		System.out.println(dir.listFiles().length + " files");
	}
	
	public static void lsDir_ADB(String dirPath){
		List<String> ls = MTPUtils.getADBDir(dirPath);
		for (String l : ls){
			System.out.println(l);
		}
	}
	
	
	public static void pullFiles(String locPath,String mtpPath) throws Throwable{
		//Pull any updated files from the phone
		File mtpDir = new File(mtpPath);
		int ctPull=0;
		File locDir = new File(locPath);
		File[] mtpFiles = mtpDir.listFiles();
		if(mtpFiles == null){
			System.out.println("Problem loading the MTP directory.");
			return;
		}
		File[] locFiles = locDir.listFiles();
		Map<String,File> mapLoc = new HashMap<String,File>();
		for (File f : locFiles){
			mapLoc.put(f.getName(), f);
		}
		String cmd;
		File locFile = null;
		Date locLastMod = null, mtpLastMod = null;
		boolean flgPull = false;
		for (File mtpFile : mtpFiles){
			flgPull = false;
			locFile = new File(locPath + File.separator + mtpFile.getName());
			if(locFile == null || !locFile.exists()) flgPull = true;
			if(locFile.exists()){
				locLastMod = new Date(locFile.lastModified());
				mtpLastMod = new Date(mtpFile.lastModified());
				if(mtpLastMod.after(locLastMod)) flgPull = true;
				
				//optional, check size. If file size is the same, probably the same file even
				//if modification dates are different
				if(mtpFile.length() == locFile.length()){
					System.out.println("Skipping " + mtpFile.getName() + ": local file appears to be same size.");
					flgPull = false;
				}
			}
			if(flgPull){
				ctPull++;
				System.out.println("Pulling " + mtpFile.getName() + " to " +  locDir.getName());
				copyFile(mtpFile,locFile);
			}
		}
		System.out.println("Pulled " + ctPull + " files from MTP source.");
	
	}

	public static void pullFilesADB(String locPath,String mtpPath){
		//String cmd = "cd " + locPath;
		//MTPUtils.runProcess(true, cmd);
		String cmd = "adb pull \"" + mtpPath + "\" \"" + locPath + "\"";
	
		MTPUtils.runProcess(true, cmd);
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

				List<String> ret2 = MTPUtils.runProcess(true, cmd);
			}
		}
		System.out.println("Pulled " + ctPull + " files from MTP source.");
	
	}
	
	public static void pushFiles(String locPath,String mtpPath) throws Throwable{
		//push any updated files to phone
		File mtpDir = new File(mtpPath);
		int ctPush=0;
		File srcDir = new File(locPath);
		File[] mtpFiles = mtpDir.listFiles();
		if(mtpFiles == null){
			System.out.println("Problem loading the MTP directory.");
			return;
		}
		File[] locFiles = srcDir.listFiles();
		File mtpFile = null;
		String cmd;
		Date locLastMod,mtpLastMod;
		boolean flgPush = false;
		for (File locFile : locFiles){
			flgPush = false;
			String n = locFile.getName();
			mtpFile = new File(mtpPath + "/" + n);
			locLastMod = new Date(locFile.lastModified());
			if(mtpFile == null) flgPush = true;
			if(mtpFile != null){
				mtpLastMod = new Date(mtpFile.lastModified());
				if (mtpLastMod.before(locLastMod)) flgPush = true;
				//optional, check size. If file size is the same, probably the same file even
				//if modification dates are different
				if(mtpFile.length() == locFile.length()) flgPush = false;
			}
			if(flgPush){
				ctPush++;
				System.out.println("Pushing " + locFile.getName() + " to " + mtpPath);
				copyFile(locFile,mtpFile);
			}
		}
		System.out.println("Pushed " + ctPush + " files to MTP location.");
	
	}

	
	
	public static void pushFilesADB(String locPath,String mtpPath) throws Throwable{
		//push any updated files to phone
		File mtpDir = new File(mtpPath);
		int ctPush=0;
		File srcDir = new File(locPath);
		Map<String,FileMTP> mapMTP  = loadMTPDirMap(mtpPath);
		if(mapMTP == null){
			System.out.println("Problem loading the MTP directory.");
			return;
		}
		FileMTP mtpFile;
		File[] fileList = srcDir.listFiles();
		String cmd;
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
			if(mtpFile.fSize == locFile.length()) flgPush = false;
			if(flgPush){
				ctPush++;
				System.out.println("Pushing " + locFile.getName() + " to " + mtpPath);
				cmd = "adb push \"" +  locPath + File.separator + locFile.getName() + "\" \"" + mtpPath + "\"";
				List<String> ret2 = MTPUtils.runProcess(true, cmd);
			}
		}
		System.out.println("Pushed " + ctPush + " files to MTP location.");
	
	}
	public static List<String> runProcess(boolean isWin, String... command){
		return runProcess(isWin,true,command);
	}
	public static List<String> runProcess(boolean isWin, boolean flgDebug,String... command) {
	    if(flgDebug){
	    	System.out.print("command to run: ");
		    for (String s : command) {
		        System.out.print(s);
		    }
		    System.out.print("\n");
	    }
	    String[] allCommand = null;
	    try {
	        if (isWin) {
	            allCommand = concat(WIN_RUNTIME, command);
	        } else {
	            allCommand = concat(OS_LINUX_RUNTIME, command);
	        }
	        ProcessBuilder pb = new ProcessBuilder(allCommand);
	        pb.redirectErrorStream(true);
	        Process p = pb.start();
	        p.waitFor();
	        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String _temp = null;
	        List<String> line = new ArrayList<String>();
	        while ((_temp = in.readLine()) != null) {
	        	if(flgDebug) System.out.println("temp line: " + _temp);
	            line.add(_temp);
	        }
	        if(flgDebug) System.out.println("result after command: " + line);
	        return line;
	
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	public static void seeFiles(String[] args) {
		File f1= new File("\\?\\");
		System.out.println("File system roots returned by   FileSystemView.getFileSystemView():");
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File[] roots = fsv.getRoots();
		for (int i = 0; i < roots.length; i++)
		{
			System.out.println("Root: " + roots[i]);
		}
	
		System.out.println("Home directory: " + fsv.getHomeDirectory());
	
		System.out.println("File system roots returned by File.listRoots():");
	
		File[] f = File.listRoots();
		for (int i = 0; i < f.length; i++)
		{
			System.out.println("Drive: " + f[i]);
			System.out.println("Display name: " + fsv.getSystemDisplayName(f[i]));
			System.out.println("Is drive: " + fsv.isDrive(f[i]));
			System.out.println("Is floppy: " + fsv.isFloppyDrive(f[i]));
			System.out.println("Readable: " + f[i].canRead());
			System.out.println("Writable: " + f[i].canWrite());
		}
	
	}
	public static void setPropValue(String propName, String propVal, String propFileName) throws IOException {  	 
		Properties prop = getProperties(propFileName);
		prop.setProperty(propName, propVal);
	}

    public static void writeProperties(String propFileName, Properties props)
			throws FileNotFoundException, IOException {
		File f= new File(propFileName);
		OutputStream out = new FileOutputStream(f);
		props.store(out, "MTPSync Props");
	}
    
    public static Boolean verifyMTPDir(String dir) throws IOException {
		File mtpDir = new File(dir);
		try{
			mtpDir.exists();
			if(!mtpDir.exists()) throw new IOException("Directory '" +dir + "' does not seem to exist.");
			if(!mtpDir.canRead()) throw new IOException("Directory '" +dir + "' can't be read.");
		}catch(IOException ex){
			throw ex;
		}
		return mtpDir.exists();
	}
	public static void clearMTPDir(String mtpPath) {
		File mtpDir = new File(mtpPath);
		for(File file: mtpDir.listFiles()){
			System.out.println("Deleting " + file.getName());
			file.delete();	
		}
	}
}