package com.evolveum.midpoint.model.impl.bulkmodify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class UserFileReader {
	private Path path;
	private Charset charset = Charset.forName("UTF-8");
	private String delimiters = ";-\n?!'^+%&()=?_,:";
	private List<String> statuses;
	
	
	public ArrayList<ModifyUserBean> readFileIntoUsers(){
		ArrayList<ModifyUserBean> resultList = new ArrayList<ModifyUserBean>(100);
		
		try {
		      List<String> lines = Files.readAllLines(path, charset);
		      for (int i=0; i<lines.size(); i++) {
		    	  String line = lines.get(i);
		    	  StringTokenizer token = new StringTokenizer(line, delimiters);
		    	while (token.hasMoreTokens()) {
		    		String userName = token.nextToken().trim(); 
		    		String status = token.nextToken().trim();
		    		boolean statusCheck = false;
		    		for(int j=0; j<statuses.size(); j++){
		    			if(statuses.get(j).compareToIgnoreCase(status) == 0){
		    				statusCheck = true;
		    				break;
		    			}
		    		}
		           if(statusCheck == true)
		        	   resultList.add(new ModifyUserBean(userName, status, null));
		        }//while
		        
		      }//for
		    } catch (IOException e) {
		      
		}
		
		return resultList;
	}
	
	public synchronized ArrayList<ModifyUserBean> readFileIntoUsersForRole(){
		ArrayList<ModifyUserBean> resultList = new ArrayList<ModifyUserBean>(100);
		
		try {
		      List<String> lines = Files.readAllLines(path, charset);
		      for (int i=0; i<lines.size(); i++) {
		    	  String line = lines.get(i);
		    	  StringTokenizer token = new StringTokenizer(line, delimiters);
		    	while (token.hasMoreTokens()) {
		    		String userName = token.nextToken().trim(); 
		    		String role = token.nextToken().trim();
		    		/*boolean statusCheck = false;
		    		for(int j=0; j<statuses.size(); j++){
		    			if(statuses.get(j).compareToIgnoreCase(status) == 0){
		    				statusCheck = true;
		    				break;
		    			}
		    		}
		           if(statusCheck == true)*/
		        	   resultList.add(new ModifyUserBean(userName, null, role));
		        }//while
		        
		      }//for
		    } catch (IOException e) {
		      
		}
		
		return resultList;
	}
	

	public UserFileReader(String path){
		this.path = Paths.get(path);
		statuses = new ArrayList<String>(2);
		statuses.add("enabled");
		statuses.add("disabled");
	}


	public Path getPath() {
		return path;
	}


	public void setPath(Path path) {
		this.path = path;
	}
	
	
}
