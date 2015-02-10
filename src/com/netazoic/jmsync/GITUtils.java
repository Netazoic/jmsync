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

public class GITUtils {
 
    private GITUtils() {
    }


	public static void addFiles(String repoPath,String path){
		//SyncUtils.pwd();
		String[] cmd = new String[1] ;
		File workingDir = gitDir(repoPath);
		cmd[0] = "git add -A \"" + path + "\"";
		List<String> rtn = SyncUtils.runProcess(workingDir,false,cmd);
		SyncUtils.checkErrors(rtn);
		status(repoPath);
	}





	public static void commitFiles(String repoPath,String msg){
		String cmd = "git commit -m \"" + msg + "\"" ;
		File workingDir = gitDir(repoPath);
		List<String> rtn =SyncUtils.runProcess(workingDir,true,cmd);
		SyncUtils.checkErrors(rtn);
		

	}

	private static File gitDir(String dirPath) {
		if(SyncUtils.isWin()) dirPath = dirPath.replaceAll("\\\\/", "\\");
		File workingDir= new File(dirPath);
		return workingDir;
	}





	public static void updateRepo(String repoPath,String addPath,String msg){
		addFiles(repoPath,addPath);
		commitFiles(repoPath,msg);
	}
	

	public static void status(String repoPath){
		String cmd = "git st ";
		File workingDir = gitDir(repoPath);
		List<String> rtn =SyncUtils.runProcess(workingDir,true,cmd);
		SyncUtils.checkErrors(rtn);
	}	
	
	public static void pullFiles(String repoPath, String gitRemote,String gitBranch){
		String cmd = "git pull \"" + gitRemote + "\" \"" + gitBranch + "\"";
		File workingDir = gitDir(repoPath);
		List<String> rtn =SyncUtils.runProcess(workingDir,true,cmd);
		SyncUtils.checkErrors(rtn);
	}	
	
	public static void pushFiles(String repoPath,String gitRemote,String gitBranch){
		String cmd = "git push \"" + gitRemote + "\" \"" + gitBranch + "\"";
		File workingDir = gitDir(repoPath);
		List<String> rtn =SyncUtils.runProcess(workingDir,true,cmd);
		SyncUtils.checkErrors(rtn);
	}
	
   
}