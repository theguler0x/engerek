package com.evolveum.midpoint.model.impl.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.evolveum.midpoint.model.api.ModelExecuteOptions;
import com.evolveum.midpoint.model.api.ModelService;
//import com.evolveum.midpoint.model.api.PolicyViolationException;
import com.evolveum.midpoint.model.api.ProgressListener;
import com.evolveum.midpoint.model.impl.ModelConstants;
//import com.evolveum.midpoint.model.impl.ModelConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.provisioning.api.ChangeNotificationDispatcher;
import com.evolveum.midpoint.repo.cache.RepositoryCache;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.ObjectDeltaOperation;
import com.evolveum.midpoint.schema.ResultHandler;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.SearchResultMetadata;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.MiscSchemaUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskHandler;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.task.api.TaskRunResult;
import com.evolveum.midpoint.task.api.TaskRunResult.TaskRunResultStatus;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ImportOptionsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorHostType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

@Component
public class ImportDetsisTaskHandler  implements TaskHandler{
	
	private static final Trace LOGGER = TraceManager.getTrace(ImportDetsisTaskHandler.class);
	public static final String HANDLER_URI = "http://midpoint.evolveum.com/xml/ns/public/model/detsis/handler-3";
	private static final String DOT_CLASS = ImportDetsisTaskHandler.class.getName() + ".";
	private static final String IMPORT_OBJECTS_FROM_STREAM =DOT_CLASS+ "importObject";
	private static String MIDPOINT_HOME = System.getProperty("midpoint.home"); 
    
	@Autowired(required=true)
    private TaskManager taskManager;
	
	@Autowired(required=true)
	private ChangeNotificationDispatcher changeNotificationDispatcher;
	
	@Autowired(required = true)
    private ModelService modelService;
	
	@Autowired(required=true)
	private PrismContext prismContext;	
	
	@Autowired(required = true)
	private transient ObjectImporter objectImporter;

	
	private PrismPropertyDefinition<Object> filenamePropertyDefinition;
	
	
//	private static final Trace LOGGER = TraceManager.getTrace(ImportObjectsFromFileTaskHandler.class);
 
    @PostConstruct
    private void initialize() {
    	LOGGER.info(": INITIALIZE IN DETSIS POST...");
    	//filenamePropertyDefinition = new PrismPropertyDefinition<>(ModelConstants.FILENAME_PROPERTY_NAME,
        //        DOMUtil.XSD_STRING, prismContext);
        taskManager.registerHandler(HANDLER_URI, this);
        
      
    }
    
    @Override
    public TaskRunResult run(Task task)  {
    	LOGGER.info(": START TASK RUN RESULT...");
        long progress = task.getProgress();
        
        
        OperationResult opResult = new OperationResult(OperationConstants.IMPORT_DETSIS);
        TaskRunResult runResult = new TaskRunResult();
        runResult.setOperationResult(opResult);
 
      
        InputStream stream = null;
        File file = new File(MIDPOINT_HOME +"/detsis/detsisXmlfile.xml");
        LOGGER.info("{} : FILE SETTING...", file.getName());
        ImportOptionsType impOps = new ImportOptionsType();
        impOps = MiscSchemaUtil.getDefaultImportOptions();
        //impOps.setKeepOid(false);
        impOps.setOverwrite(true);
		try  {
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            stream = new ReaderInputStream(reader, reader.getEncoding());
            importObjectsFromStream(stream, impOps, task, opResult);
            
            		
			stream.close();
		} catch (IOException e) {
		
			e.printStackTrace();
			
		}   finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
		}
				
        progress++;
        
        opResult.computeStatus();
       // This "run" is finished.
        runResult.setRunResultStatus(TaskRunResultStatus.FINISHED);
        runResult.setProgress(progress);
        return runResult;
    }
    
    
    public void importObjectsFromStream(InputStream input, ImportOptionsType options, Task task,
    		   OperationResult parentResult) {
    		  RepositoryCache.enter();
    		  OperationResult result = parentResult.createSubresult(IMPORT_OBJECTS_FROM_STREAM);
    	//	  result.addParam("options", options);
    	//	 objectImporter.importObjectsNotRaw(input, options, task, result);
    		  if (LOGGER.isTraceEnabled()) {
    		   LOGGER.trace("Import result:\n{}", result.debugDump());
    		  }
    		  // No need to compute status. The validator inside will do it.
    		  // result.computeStatus("Couldn't import object from input stream.");
    		  RepositoryCache.exit();
    		  result.cleanupResult();
    		}
       


	@Override
	public Long heartbeat(Task task) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshStatus(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCategoryName(Task task) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCategoryNames() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
