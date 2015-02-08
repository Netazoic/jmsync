package com.netazoic.netamtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/*
 * Commit git files for PTF files in project
 */
public class GITSync {

	public static String repoPath;		//e.g.,, E:\Audio Files\Meet the Moores
	public static String pCode;				//e.g., mtm
	public static String gitBranch;
	public static String gitRemote;
	public static String propFileName = "conf/gitsync.properties";  //This is a default, override if desired
	private static Properties props;
	private static GITSync_Action gitAction;

	public enum GITSync_Param{
		dir_REPO,
		gitAction, gitBranch, gitRemote,pCode
	}

	public enum GITSync_Action{
		ci,push,add,pull,update,commit
	}


	public static void addFiles(String addPath){
		GITUtils.addFiles(repoPath,addPath);
	}



	public static void commitFiles(){
		String cmtMsg = getCommitMsg();
		GITUtils.commitFiles(repoPath,cmtMsg);
	}



	public static void commitFiles(String cmtMsg){
		GITUtils.commitFiles(repoPath,cmtMsg);
	}



	public static GITSync_Action getAction(){
		//gitAction may have been specified as a param
		//if so, just use that. Otherwise, prompt

		if(gitAction!=null) return gitAction;

		else{
			String[] options = {"update","push","pull","add","ci"};
			String actionString = SyncUtils.getInput("Sync Action?", options, GITSync_Action.update.name());
			if(actionString==null) System.exit(1);
			gitAction = GITSync_Action.valueOf(actionString);
		}

		return gitAction;
	}



	public static String getCommitMsg(){
		String defaultInput = "Auto sync commit";
		String msg = SyncUtils.getInput("Commit msg:", defaultInput);
		return msg;
	}




	public static  Properties getProperties()
			throws IOException {
		Properties props = SyncUtils.getProperties(propFileName);
		if(props == null) props = SyncUtils.createPropertiesFile(propFileName);

		return props;
	}



	public static void pullFiles(){
		GITUtils.pullFiles(repoPath,gitRemote,gitBranch);
	}

	public static void pushFiles() throws Throwable{
		GITUtils.pushFiles(repoPath,gitRemote,gitBranch);
	}

	public static void setCmdLineParams(String[] args){
		int i=0;
		if(args == null ) return;
		for(String k : args){
			if(k.equals("-action") || k.equals("-a")){
				gitAction=GITSync_Action.valueOf(args[i+1]);
			}
			if(k.equals("-loc") || k.equals("-projPath")){
				repoPath = args[i+1];
			}
			if(k.equals("-pCode")){
				pCode = args[i+1];
				propFileName = "conf/" + pCode + ".properties";
			}
			if(args.length==1){
				pCode = k;
			}
			if(k.contains(".properties")){
				propFileName = k;
			}
			if(k.equals("update")){
				gitAction=GITSync_Action.update;
			}
			if(k.equals("add")){
				gitAction=GITSync_Action.add;
			}
			if(k.equals("push")){
				gitAction=GITSync_Action.push;
			}
			if(k.equals("pull")){
				gitAction=GITSync_Action.pull;
			}
			i++;

		}


	}

	private static void setParams() throws IOException{
		if(pCode == null){
			pCode = SyncUtils.getInput("Short code for the project you are syncing:", "mtm");
			if(pCode == null) System.exit(1);
		}
		propFileName = "conf/" + pCode + ".properties";
		props = getProperties();
		setParams(props);
	}

	private static void setParams(Properties props) throws FileNotFoundException, IOException{
		if(repoPath==null) repoPath = props.getProperty(GITSync_Param.dir_REPO.name());
		gitBranch=props.getProperty(GITSync_Param.gitBranch.name());
		gitRemote=props.getProperty(GITSync_Param.gitRemote.name());
		if(gitAction==null){
			String temp = props.getProperty(GITSync_Param.gitAction.name());
			if(temp!=null) gitAction = GITSync_Action.valueOf(temp);
		}
		if(gitAction==null) gitAction = getAction();

		setProjectPaths(false);

		//Get the remote if not yet specified
		if(gitRemote==null)gitRemote = SyncUtils.getInput("What is the remote repository?", "origin");

		//Get the branch if not yet specified
		if(gitBranch==null) gitBranch = SyncUtils.getInput("What branch?", "master");

		writeProperties();
	}

	public static void setProjectPaths(Boolean flgOverride) throws IOException {
		if(repoPath !=null && !flgOverride) return;
		File repoDir = SyncUtils.chooseDir(repoPath,"Select project directory");
		if(repoDir==null) System.exit(1);
		repoPath = repoDir.getAbsolutePath();
	}



	public static void writeProperties()
			throws FileNotFoundException, IOException {
		props.put(GITSync_Param.dir_REPO.name(), repoPath);
		props.put(GITSync_Param.gitBranch.name(), gitBranch);
		props.put(GITSync_Param.gitRemote.name(), gitRemote);
		props.put(GITSync_Param.pCode.name(), pCode);
		SyncUtils.writeProperties(propFileName, props);
	}

	public static void main(String[]args) throws Throwable{
		//SyncUtils.getYesNo("Proceed with a git sync?", true);
		//Uncomment above to create a wait state to allow attaching a remote debugger
		setCmdLineParams(args);
		setParams();

		try{
			switch(gitAction){
			case pull:
				pullFiles();
				break;
			case push:
				pushFiles();
				break;
			case update:
				addFiles("*.ptf");
				commitFiles();
				break;
			case add:
				addFiles("*.ptf");
				break;
			case commit:
			case ci:
				commitFiles();
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
