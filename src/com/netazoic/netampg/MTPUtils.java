package com.netazoic.netampg;

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
    
    public String getPropValue(String propName, String propFileName) throws IOException {
    	 
		String propVal = "";
		Properties prop = getProperties(propFileName);

		propVal = prop.getProperty(propName);
		return propVal;
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

    public static void setPropValue(String propName, String propVal, String propFileName) throws IOException {  	 
		Properties prop = getProperties(propFileName);
		prop.setProperty(propName, propVal);
	}

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

	public static File chooseDir(String startPath,String dlgTitle){
		JFileChooser chooser = new JFileChooser(startPath);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File f = null;
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Audio Files", "wav", "mp3", "flac");
		//chooser.setFileFilter(filter);
		int returnVal = chooser.showDialog(new JFrame(""),dlgTitle);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			System.out.println("You chose to open this file: " +
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
	
	private static void copyFiles(String srcPath,String destPath) throws Throwable{
		File srcDir = new File(srcPath);
		File destDir = new File(destPath);
		if(!destDir.exists()) new File(destPath).mkdirs();
		destDir = new File(destPath);
		Files.copy(srcDir.toPath(), destDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
	
	}

	private static Map<String,FileMTP> loadMTPDirMap(String dir) throws ParseException{
		String cmd = "adb shell ls -ls \"" + dir + "\"";
		List<String> destDirList = MTPUtils.runProcess(true, cmd);
		//Check for connection errors
		String string1 = destDirList.get(0);
		if(!string1.startsWith("total")){
			//ADB not initialized, try again
			destDirList = MTPUtils.runProcess(true, cmd);
			string1 = destDirList.get(0);
			if(string1.matches("error: device unauthorized.*")){
				System.out.println(string1);
				return null;
			}
			if(!string1.startsWith("total")){
				System.out.println(string1);
				return null;
			}

		}
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


    public static List<String> runProcess(boolean isWin, String... command) {
        System.out.print("command to run: ");
        for (String s : command) {
            System.out.print(s);
        }
        System.out.print("\n");
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
                System.out.println("temp line: " + _temp);
                line.add(_temp);
            }
            System.out.println("result after command: " + line);
            return line;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
	public static void updateFilesADB(String srcPath,String destPath) throws Throwable{
		File srcDir = new File(srcPath);
		Map<String,FileMTP> map  = loadMTPDirMap(destPath);
		if(map == null){
			System.out.println("Problem loading the MTP directory.");
			return;
		}
		FileMTP currFile;
		File[] fileList = srcDir.listFiles();
		String cmd;
		boolean flgPush = false;
		for (File ff : fileList){
			flgPush = false;
			String n = ff.getName();
			currFile = map.get(ff.getName());
			Date d = new Date(ff.lastModified());
			if(currFile == null) flgPush = true;
			if(currFile != null && currFile.lastUpdate.before(d)) flgPush = true;

			if(flgPush){
				System.out.println("Copying " + ff.getName() + " to " + destPath);
				cmd = "adb push \"" +  srcPath + File.separator + ff.getName() + "\" \"" + destPath + "\"";
				List<String> ret2 = MTPUtils.runProcess(true, cmd);
			}
		}

	}
	
	public static void writeProperties(String propFileName, Properties props)
			throws FileNotFoundException, IOException {
		File f= new File(propFileName);
		OutputStream out = new FileOutputStream(f);
		props.store(out, "MTPSync Props");
	}
}