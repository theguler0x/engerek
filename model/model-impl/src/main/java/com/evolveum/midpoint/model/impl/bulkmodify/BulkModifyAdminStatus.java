package com.evolveum.midpoint.model.impl.bulkmodify;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskCategory;
import com.evolveum.midpoint.task.api.TaskHandler;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.task.api.TaskRunResult;
import com.evolveum.midpoint.task.api.TaskRunResult.TaskRunResultStatus;
import com.evolveum.midpoint.model.impl.bulkmodify.*;

@Component
public class BulkModifyAdminStatus implements TaskHandler {
 
    public static final String HANDLER_URI = "http://midpoint.evolveum.com/xml/ns/public/model/bulkmodify/adminstatus/handler-3";
    
    private static String credentialUsername = "administrator";
	private static String credentialsPassword = "5ecr3t";
	private static String csvFilePath = "C:\\midpoint\\resources\\modifyusersalldisabled.csv"; // "C:/Users/gurer.onder/workspacekisisel/utilities/resources/modifyusersalldisabled.csv"
 
    @Autowired(required=true)
    private TaskManager taskManager;
 
    @PostConstruct
    private void initialize() {
        taskManager.registerHandler(HANDLER_URI, this);
    }
 
    public TaskRunResult run(Task task) {
                 
        long progress = task.getProgress();
        OperationResult opResult = new OperationResult("com.example.mytask.handler");
        TaskRunResult runResult = new TaskRunResult();
        runResult.setOperationResult(opResult);
 
        // TODO: custom code comes here   
        ArrayList<ModifyUserBean> users;
		UserFileReader ufr = new UserFileReader(csvFilePath);
		
		
	    users = ufr.readFileIntoUsers();
	    
	    
	    System.out.println(users.toString());
	    
	    for(int i=0; i<users.size(); i++){
	    	PostXML runObj = new PostXML(credentialUsername, credentialsPassword);
	    	try {
				String oid = runObj.searchByNamePostMethod(users.get(i).getUserName());
				int result = runObj.modifyByOidPostMethod(oid, users.get(i).getStatus());
				
				//if http result is 204, operation is successful
				if(result==204){
					System.out.println("User:'"+users.get(i).getUserName()+"' administrativeStatus is updated to:'"+users.get(i).getStatus()+"'");
				}
				else{
					System.out.println("ERROR! User:'"+users.get(i).getUserName()+"' administrativeStatus COULDN'T updated to:'"+users.get(i).getStatus()+"'");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
 
        progress++;
 
        opResult.computeStatus();
        // This "run" is finished.
        runResult.setRunResultStatus(TaskRunResultStatus.FINISHED);
        runResult.setProgress(progress);
        return runResult;
    }
 
    @Override
    public Long heartbeat(Task task) {
        // Heartbeat is not supported for this task
        return null;
    }
 
    @Override
    public void refreshStatus(Task task) {
        // Do nothing, no need to explicitly refresh task status
    }
 
    @Override
    public String getCategoryName(Task task) {
         return TaskCategory.CUSTOM;
    }
 
    @Override
    public List<String> getCategoryNames() {
        // just a single category, specified above
        return null;
    }
}