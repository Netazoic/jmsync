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

public class SyncUtils {
    private static final String[] WIN_RUNTIME = { "cmd.exe", "/C" };
    private static final String[] OS_LINUX_RUNTIME = { "/bin/bash", "-l", "-c" };

    private SyncUtils() {
    }

    
	public static void cd(String path){
		String[] cmd = new String[1];
		cmd[0] = "cd '" + path  + "'";

		if(isWin()){
			path = path.replaceAll("\\\\/", "\\");
			cmd[0] = "cd /d \"" + path + "\"";
		}
		List<String> rtn =SyncUtils.runProcess(cmd);
		checkErrors(rtn);
		
	}
	
    public static void checkErrors(List<String> rtn){
    	//Check the return value for erros
    	if(rtn==null) return;
    	try{rtn.get(0);}catch(Exception ex){System.out.println(ex.getMessage());return;}
    	String msg1 = rtn.get(0);
    	if(msg1.matches("(?i)^(error|fatal).*$")){
    		System.out.println(rtn.get(0));
    		System.exit(1);
    	}
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
	
	public static void pushFiles(String locPath,String syncPath) throws Throwable{
		//push any updated files to syncPath
		File syncDir = new File(syncPath);
		int ctPush=0;
		File srcDir = new File(locPath);
		File[] synclocFiles = syncDir.listFiles();
		if(synclocFiles == null){
			System.out.println("Problem loading the Sync directory.");
			return;
		}
		File[] locFiles = srcDir.listFiles();
		File syncFile = null;
		String cmd;
		Date locLastMod,mtpLastMod;
		boolean flgPush = false;
		for (File locFile : locFiles){
			flgPush = false;
			String n = locFile.getName();
			syncFile = new File(syncPath + "/" + n);
			locLastMod = new Date(locFile.lastModified());
			if(syncFile == null) flgPush = true;
			if(syncFile != null){
				mtpLastMod = new Date(syncFile.lastModified());
				if (mtpLastMod.before(locLastMod)) flgPush = true;
				//optional, check size. If file size is the same, probably the same file even
				//if modification dates are different
				if(syncFile.length() == locFile.length()) flgPush = false;
			}
			if(flgPush){
				ctPush++;
				System.out.println("Pushing " + locFile.getName() + " to " + syncPath);
				copyFile(locFile,syncFile);
			}
		}
		System.out.println("Pushed " + ctPush + " files to Sync location.");
	
	}

    public static Properties createPropertiesFile(String propFileName) throws IOException{
    	Properties prop = new Properties();
    	File f = new File(propFileName);
    	FileOutputStream fout = new FileOutputStream(f);
    	prop.store(fout, propFileName);
    	fout.close();
    	return prop;
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
		SyncUtils utils = new SyncUtils();
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
    
    public void GetShortcutPath(String[] args)
      {
        if(args.length > 0)
        {
          javax.swing.JOptionPane.showMessageDialog(null,"Full Path: "+args[0]);
        }
        else
        {
          try{Runtime.getRuntime().exec("wscript.exe GetShortcutPath.vbs");}
          catch(Exception e){e.printStackTrace();}
     
        }
        //System.exit(0);
      }
    
	public static boolean getYesNo(String settingQ, Boolean flg){
		String[] options = {"Yes","No"};
		String defaultAnswer = flg !=null && flg==true?"Yes":"No";
		String input = SyncUtils.getInput(settingQ, options, defaultAnswer);
		return (input.equals("Yes")?true:false);

	}

	
	
	public static boolean isWin() {
		boolean isWin = false;
		String osName = System.getProperty("os.name");
		if(osName.contains("Windows")) isWin = true;
		return isWin;
	}


	public static void pullFiles(String locPath,String syncPath) throws Throwable{
		//Pull any updated files from the phone
		File syncDir = new File(syncPath);
		int ctPull=0;
		File locDir = new File(locPath);
		File[] syncFiles = syncDir.listFiles();
		if(syncFiles == null){
			System.out.println("Problem loading the Sync directory.");
			return;
		}
		File[] locFiles = locDir.listFiles();
		Map<String,File> mapLoc = new HashMap<String,File>();
		for (File f : locFiles){
			mapLoc.put(f.getName(), f);
		}
		String cmd;
		File locFile = null;
		Date locLastMod = null, syncLastMod = null;
		boolean flgPull = false;
		for (File syncFile : syncFiles){
			flgPull = false;
			locFile = new File(locPath + File.separator + syncFile.getName());
			if(locFile == null || !locFile.exists()) flgPull = true;
			if(locFile.exists()){
				locLastMod = new Date(locFile.lastModified());
				syncLastMod = new Date(syncFile.lastModified());
				if(syncLastMod.after(locLastMod)) flgPull = true;
				
				//optional, check size. If file size is the same, probably the same file even
				//if modification dates are different
				if(syncFile.length() == locFile.length()){
					System.out.println("Skipping " + syncFile.getName() + ": local file appears to be same size.");
					flgPull = false;
				}
			}
			if(flgPull){
				ctPull++;
				System.out.println("Pulling " + syncFile.getName() + " to " +  locDir.getName());
				copyFile(syncFile,locFile);
			}
		}
		System.out.println("Pulled " + ctPull + " files from Sync source.");
	
	}

	public static void pwd(){
		String cmd = "pwd ";
		if(isWin()) cmd = "echo %cd%";
		List<String> rtn =SyncUtils.runProcess(cmd);
		checkErrors(rtn);
		System.out.println(rtn.get(0));
	}


	public static List<String> runProcess(String... command){
		return runProcess(false,command);
	}
	
	public static List<String> runProcess(boolean flgDebug, String...command ){
		File f= null;
		return runProcess(f,flgDebug,command);
	}
	public static List<String> runProcess(String workingPath, boolean flgDebug, String...command ){
		File f = new File(workingPath);
		return runProcess(f,flgDebug,command);
	}
	
	public static List<String> runProcess(File workingDir, boolean flgDebug,String... command) {
		boolean isWin = isWin();
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
	        if(workingDir != null) pb.directory(workingDir);
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
    
    
	public static void rmDir(String dirPath) {
		File mtpDir = new File(dirPath);
		for(File file: mtpDir.listFiles()){
			System.out.println("Deleting " + file.getName());
			file.delete();	
		}
	}
}