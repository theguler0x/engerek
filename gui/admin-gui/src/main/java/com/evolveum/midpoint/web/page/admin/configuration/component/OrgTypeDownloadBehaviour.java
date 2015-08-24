package com.evolveum.midpoint.web.page.admin.configuration.component;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.file.Files;

import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.ResultHandler;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.AjaxDownloadBehaviorFromFile;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.web.page.error.PageError;
import com.evolveum.midpoint.web.security.MidPointApplication;
import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class OrgTypeDownloadBehaviour extends PageBase implements Serializable{
	
	 private static final Trace LOGGER = TraceManager.getTrace(OrgTypeDownloadBehaviour.class);

	    private static final String DOT_CLASS = OrgTypeDownloadBehaviour.class.getName() + ".";
	    private static final String OPERATION_SEARCH_OBJECT = DOT_CLASS + "loadObjects";
	    private static final String OPERATION_CREATE_DOWNLOAD_FILE = DOT_CLASS + "createDownloadFile";
	    private static String MIDPOINT_HOME = "midpoint.home";
	    
	    private boolean exportAll;
	    private Class<? extends ObjectType> type;
	    private boolean useZip;
	    private ObjectQuery query;
	    private String fileName;
	    private String exportFolder;
	    
	
	    public File initFile() {
		 String midpointHome = System.getProperty(MIDPOINT_HOME);
	 
	        PageBase page = (PageBase) getPage();

	        OperationResult result = new OperationResult(OPERATION_CREATE_DOWNLOAD_FILE);
	        if(this.exportFolder ==null){
	        	 LOGGER.info("Export folder is null.");
	        	if (StringUtils.isNotEmpty(midpointHome)) {
	        		this.setExportFolder(midpointHome+"/detsis");
	        	}
	        }
	        else{
        		LOGGER.info("got folder name {} .", getExportFolder());
        	}
	       
	        File folder = new File(getExportFolder());
	        
	        LOGGER.info("folder name {} .", folder.getAbsolutePath());
	        
	        if (!folder.exists() || !folder.isDirectory()) {
	            folder.mkdir();
	        }

	        String suffix = isUseZip() ? "zip" : "xml";
	        if(this.fileName==null){
	        	LOGGER.info("file name set {} .", fileName);
	        	fileName = "detsisXmlfile_out." + suffix;
	        	LOGGER.info("file name set {} .", fileName);
	        	
	        }
	        else{
	        	fileName=getFileName();
	        }
	        File file = new File(folder, fileName);
	    	LOGGER.info("file name {} .", file.getName());

	        Writer writer = null;
	        try {
	            LOGGER.info("Creating file '{}'.", new Object[]{file.getAbsolutePath()});
	            writer = createWriter(file);
	            LOGGER.info("Exporting objects.");
	            dumpHeader(writer);
	            dumpObjectsToStream(writer, result);
	            dumpFooter(writer);
	            LOGGER.info("Export finished.");

	            result.recomputeStatus();
	        } catch (Exception ex) {
	            LoggingUtils.logException(LOGGER, "Couldn't init download link", ex);
	            result.recordFatalError("Couldn't init download link", ex);
	        } finally {
	            if (writer != null) {
	                IOUtils.closeQuietly(writer);
	            }
	        }

	        if (!WebMiscUtil.isSuccessOrHandledError(result)) {
	            page.showResultInSession(result);
	            page.getSession().error(page.getString("PageOrgDiff.message.createFileException"));
	            LOGGER.debug("Removing file '{}'.", new Object[]{file.getAbsolutePath()});
	            Files.remove(file);

	            throw new RestartResponseException(PageError.class);
	        }

	        return file;
	    }
	 
	
	 
	  private <T extends ObjectType> void dumpObjectsToStream(final Writer writer, OperationResult result) throws Exception {
	        final PageBase page = (PageBase) getPage();

	        ResultHandler handler = new ResultHandler() {

	            @Override
	            public boolean handle(PrismObject object, OperationResult parentResult) {
	                try {
	                	LOGGER.info("writer object {}", object.getClass().getCanonicalName());
	                    String xml = page.getPrismContext().serializeObjectToString(object, PrismContext.LANG_XML);
	                    //LOGGER.info("xml object {}", xml);
	                    writer.write('\t');
	                    writer.write(xml);
	                    writer.write('\n');
	                } catch (IOException ex) {
	                    throw new SystemException(ex.getMessage(), ex);
	                } catch (SchemaException ex) {
	                    throw new SystemException(ex.getMessage(), ex);
	                }

	                return true;
	            }
	        };

	        ModelService service = page.getModelService();
	        GetOperationOptions options = GetOperationOptions.createRaw();
	        options.setResolveNames(true);
	        service.searchObjectsIterative(type, query, handler, SelectorOptions.createCollection(options),
	                page.createSimpleTask(OPERATION_SEARCH_OBJECT), result);
	    }
	 
	 private Writer createWriter(File file) throws IOException {
		 
		 OutputStream fs = new FileOutputStream(file);
		 Writer stream = new OutputStreamWriter(fs);
		 return new BufferedWriter(stream,1024);
	
	    }
	 
	    private void dumpFooter(Writer writer) throws IOException {
	        writer.write("</objects>");
	    }

	    private void dumpHeader(Writer writer) throws IOException {
	        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	        writer.write("<objects xmlns=\"");
	        writer.write(SchemaConstantsGenerated.NS_COMMON);
	        writer.write("\"\n");
	        writer.write("\txmlns:c=\"");
	        writer.write(SchemaConstantsGenerated.NS_COMMON);
	        writer.write("\"\n");
	        writer.write("\txmlns:org=\"");
	        writer.write(SchemaConstants.NS_ORG);
	        writer.write("\">\n");
	    }
	 

	 
	 public String getExportFolder() {
			return exportFolder;
		}

		public void setExportFolder(String exportFolder) {
			this.exportFolder = exportFolder;
		}

		public void setFileName(String fileName){
	    	this.fileName = fileName;
	    }
	    
	    public String getFileName(){
	    	return this.fileName;
	    }

	    public boolean isExportAll() {
	        return exportAll;
	    }

	    public void setExportAll(boolean exportAll) {
	        this.exportAll = exportAll;
	    }

	    public Class<? extends ObjectType> getType() {
	        if (type == null) {
	            return ObjectType.class;
	        }
	        return type;
	    }

	    public void setType(Class<? extends ObjectType> type) {
	        this.type = type;
	    }

	    public ObjectQuery getQuery() {
	        return query;
	    }

	    public void setQuery(ObjectQuery query) {
	        this.query = query;
	    }

	    public boolean isUseZip() {
	        return useZip;
	    }

	    public void setUseZip(boolean useZip) {
	        this.useZip = useZip;
	    }
	    

}
