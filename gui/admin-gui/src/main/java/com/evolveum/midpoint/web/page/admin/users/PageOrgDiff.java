package com.evolveum.midpoint.web.page.admin.users;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.AuthorizationAction;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.data.TablePanel;
import com.evolveum.midpoint.web.component.util.ListDataProvider;
import com.evolveum.midpoint.web.component.util.LoadableModel;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.web.page.PageTemplate;
import com.evolveum.midpoint.web.page.admin.configuration.component.OrgTypeDownloadBehaviour;
import com.evolveum.midpoint.web.page.admin.server.PageTasks;
import com.evolveum.midpoint.web.page.admin.server.dto.TaskDto;
import com.evolveum.midpoint.web.page.admin.server.dto.TaskDtoProviderOptions;
import com.evolveum.midpoint.web.page.admin.users.diff.DTVTOrg;
import com.evolveum.midpoint.web.page.admin.users.diff.XMLParseHelper;
import com.evolveum.midpoint.web.page.admin.users.dto.OrgDto;
import com.evolveum.midpoint.web.page.admin.users.dto.UsersDto;
import com.evolveum.midpoint.web.util.OnePageParameterEncoder;
import com.evolveum.midpoint.web.util.WebModelUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;



/*TODO:
 * SAYFANIN CALISMA SENARYOSU SUSEKILDEDIR:
 * 
 * SISTEM ILK DEFA KOSTURULDUGUNDA WEBSERVISINDEN XML'E DOKUMANTE EDILMIS YAPI IMPORT BUTONU ILE ENGEREK SISTEMINE IMPORT EDILIR.
 * BUNDAN SONRA EXPORT BUTONU DA SISTEMDEKI MEVCUT YAPININ DISARI AKTARILMASINI SAGLAR
 * DAHA SONRA HERHANGI BIR ANDA SISTEMDE BULUNAN DETSISWS DEN GELEN ORGANIZASYON XML'I ILE SISTEMDEKI DETSIS YAPILARI KARSILASTIRILMAK ISTENDIGINDE
 * COMPARE BUTONU KULLANILIR.COMPARE BUTONUNA BASILIGINDA SISTEMDE BULUNAN DOSYALAR ILE DETSIS XML DE BULUNAN DOSYALAR ARASINDAKI FARKLAR SAYFADA 
 * GOSTERILIR. FARKLILIKLARI ENGEREK SISTEMINE DAHIL ETMEK ICIN TEKRAR IMPORT BUTONU KULLANILIR.
 * 
 */
@PageDescriptor(url = "/admin/org/diff", encoder = OnePageParameterEncoder.class, action = {
		@AuthorizationAction(actionUri = PageAdminUsers.AUTH_ORG_ALL, label = PageAdminUsers.AUTH_ORG_ALL_LABEL, description = PageAdminUsers.AUTH_ORG_ALL_DESCRIPTION),
		@AuthorizationAction(actionUri = AuthorizationConstants.AUTZ_UI_ORG_UNIT_URL, label = "PageOrgDiff.auth.orgDiff.label", description = "PageOrgDiff.auth.orgDiff.description") })
public class PageOrgDiff extends PageAdminUsers {
	
	private static final String DOT_CLASS = PageOrgDiff.class.getName() + ".";
	private static final String ID_START_WEB_SERVICE_BUTTON ="startWebServiceButton";
	private static final String ID_IMPORT_BUTTON = "startImportButton";
	private static final String ID_COMPARE_BUTTON ="startCompareButton";
	private static final String ID_EXPORT_BUTTON = "startExportButton";
	private static final String ID_MAIN_FORM = "mainForm";
	private static final String ID_DESCRIPTION = "description";
	private static final String ID_START_LABEL = "startButtonLabel";
	private static final String ID_DELETED_LABEL = "labelDeleted";
	private static final String ID_ADDED_LABEL = "labelAdded";
	private static final String ID_ORGSAYI_LABEL = "labelOrgSayi";
	private static final String ID_COUNT = "labelCount";
	private static final String ID_ENGEREK_ORG = "labelEngerekOrg";
	private static final String ID_DETSIS_ORG = "labelDetsisOrg";
	private static final String ID_DETSIS_STATUS = "detsisStatus";
	private static final String OPERATION_LOAD_TASK = DOT_CLASS + "loadTask";
	private static final Trace LOGGER = TraceManager
			.getTrace(PageOrgDiff.class);	
	private static final String OPERATION_LOAD_ORGS = DOT_CLASS + "loadOrg";
	private static final String OPERATION_COMPARE_ORGS = DOT_CLASS + "compareOrg";
	private static final String OPERATION_EXPORT_ORGS = DOT_CLASS + "exportOrg";
	
	private static final String OPERATION_CREATE_DOWNLOAD_FILE =DOT_CLASS + "createDownloadFile";
	private static final String DELETE_UNIT = DOT_CLASS+"deleteOrganization";
	private static String MIDPOINT_HOME = System.getProperty("midpoint.home");
	private static String MAIN_FOLDER=MIDPOINT_HOME+"/detsis/";
	
	private IModel<TaskDto> model;
	private PageParameters parameters;
	private static boolean edit = false;
	
	/*TODO: :
	 * IMPORT ISLEMINI UYGULAYAN TASK'IN OID NUMARASINA IHTIYACIMIZ VAR. SU ANDA BUNU STATIC OLARAK YAPIYORUZ.
	 */
	private String taskOid ="3db60ed4-c198-45a1-83bc-75972ab98ba7";
	
	/*TODO: 
	 * BURADA ISLEMLERIMIZ ICIN KULLANDIGM YAPILARI TUTTUGUM UTILS CLASSININ INSTANCE'INI SINGLETON OLARAK CAGIRIYORUZ
	 * SINGLETON OLMASI ONEMLI AKSI TAKDIRDE BIR DATABASE KULLANMADIGIMIZ ICIN VERILER HER YENILEME DE YENI INSTANCE ICERISINDEN CAGIRILIR VE
	 * DEFAULT DEGERLER DONER
	 */
	PageOrgDiffUtils utils = PageOrgDiffUtils.getInstance();
	
	private XMLParseHelper xp = new XMLParseHelper();

	
	public PageOrgDiff(PageParameters parameters) {
		this(parameters, null);
	}

	
    public PageOrgDiff(PageParameters parameters, PageTemplate previousPage) {
    	
        this.parameters = parameters;
        setPreviousPage(previousPage);

		this.model = new LoadableModel<TaskDto>(false) {

			@Override
			protected TaskDto load() {
				return loadTask();
			}
		};

        edit = false;
        initLayout();
	}
    
    /* TODO:
     * SAYFANIN PARTLARININ INITIALIZE EDILDIGI METHOD
     */
	private void initLayout() {
		
		Form mainForm = new Form(ID_MAIN_FORM);
		add(mainForm);
		mainForm.add(new FeedbackPanel("feedback"));
		//compareXMLPerformed(tr);
		initBasicInfoLayout(mainForm);
		initButtons(mainForm);
		initResult(mainForm);
		initFark(mainForm);
		
	}
	
	
	/*TODO: BURASI TEST AMACLIDIR SILINMESINDE BIR SAKINCA YOK FAKAT SILINECEKSE HTML SAYFASINDAN DA BU COMPONENTLER KALDIRILMALI
	 * 
	 */
	private void initBasicInfoLayout(Form mainForm) {

		mainForm.add(new Label(ID_DESCRIPTION));
		mainForm.add(new Label(ID_START_LABEL));
	}
	
	
	/*TODO:
	 * BURASI SAYFADA IMPORT EDILECEK DOSYA SAYISI, ESLESEN DOSYA SAYISI, SILINECEK DOSYA SAYISI VE EKLENECEK DOSYA SAYISI
	 * BILGILERININ GOSTERILDIGI KISIM.
	 */
	private void initResult(Form mainForm){
		
		SortableDataProvider<OperationResult, String> provider = new ListDataProvider<>(this,
				new PropertyModel<List<OperationResult>>(model, "opResult"));
		TablePanel result = new TablePanel<>("operationResult", provider, initResultColumns());
		result.setStyle("padding-top: 0px;");
		result.setShowPaging(false);
		result.setOutputMarkupId(true);
		mainForm.add(result);
	}

	/*BUTONLARIN INTIALIZE EDILDIGI METOD. */
	private void initButtons(final Form mainForm) {

		
		AjaxButton runWebServiceNow = new AjaxButton(ID_START_WEB_SERVICE_BUTTON,createStringResource("PageOrgDiff.button.runWS")){
			
			@Override
            public void onClick(AjaxRequestTarget target) {
				Process proc;
				try {
					System.out.println("before detsis jar");
					proc = Runtime.getRuntime().exec("java -jar D:/DetsisXml.jar");
					System.out.println("Calling detsis jar");
					InputStream in = proc.getInputStream();
					InputStream err = proc.getErrorStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// process output
				
				
				//	runWebServicePerformed(target);
			
            }
			
		};
		mainForm.add(runWebServiceNow);
			
		AjaxButton compareNow = new AjaxButton(ID_COMPARE_BUTTON, createStringResource("PageOrgDiff.button.compare")) {

            @Override
            public void onClick(AjaxRequestTarget target) {
            	
					compareXMLPerformed(target);
			
            }
        };
        mainForm.add(compareNow);
		
	     AjaxButton importNow = new AjaxButton(ID_IMPORT_BUTTON, createStringResource("PageOrgDiff.button.start")) {

	            @Override
	            public void onClick(AjaxRequestTarget target) {
	                importNowPerformed(target);
	            }
	        };
	     
	        mainForm.add(importNow);
	        
	        AjaxButton export = new AjaxButton(ID_EXPORT_BUTTON, createStringResource("PageOrgDiff.button.export")) {

	            @Override
	            public void onClick(AjaxRequestTarget target) {
	                exportAllType(target);
	            }
	        };
	     
	        mainForm.add(export);
	        
	        OrgTypeDownloadBehaviour ajaxDownloadBehaviour = new OrgTypeDownloadBehaviour();
	        mainForm.add(ajaxDownloadBehaviour);

	}
	
	/*IMPORT METODU TASK YARDIMI ILE GERCEKLESTIRILIYOR*/
    private void importNowPerformed(AjaxRequestTarget target) {
    	//PageBase page = getPageBase();
        String oid = this.model.getObject().getOid();
        OperationResult result = new OperationResult(OPERATION_LOAD_ORGS);
        try {
            try {
				getTaskService().scheduleTasksNow(Arrays.asList(oid), result);
			} catch (SecurityViolationException | ObjectNotFoundException
					| SchemaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            result.computeStatus();
            
            //TODO:SILINMIS ORGANIZASYON VARSA ONLARI DISABLE EDIYOR
            if (result.isSuccess()) {
                result.recordStatus(OperationResultStatus.SUCCESS, "The task has been successfully scheduled to run.");
                if(!isMapEmpty(utils.getDeletedMap())){
                	for(String dId : utils.getDeletedMap().keySet()){
                		disableOrgPerformed(target, dId);
                		LOGGER.info("DISABLED ORG IS: " + dId);
                	}
                	utils.getDeletedMap().clear();
                }
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't schedule the task due to an unexpected exception", e);
        }
        
        //page.showResult(result);
		//target.add(getFeedbackPanel());
        showResultInSession(result);
		setResponsePage(PageOrgDiff.class);
    }
    
    /*STATIC OLARAK OID'SINI BELIRLEDIGIMIZ TASKI SAYFAYA LOAD EDIYOR*/
    private TaskDto loadTask() {
		OperationResult result = new OperationResult(OPERATION_LOAD_TASK);
        Task operationTask = getTaskManager().createTaskInstance(OPERATION_LOAD_TASK);

        //StringValue taskOid = parameters.get(OnePageParameterEncoder.PARAMETER);
       //TODO CALLS STATIC OID MAKE IT PARAMETRIC !
        String taskOid = this.taskOid;

        TaskDto taskDto = null;
		try {
            Collection<SelectorOptions<GetOperationOptions>> options = GetOperationOptions.createRetrieveAttributesOptions(TaskType.F_SUBTASK, TaskType.F_NODE_AS_OBSERVED, TaskType.F_NEXT_RUN_START_TIMESTAMP);
            TaskType loadedTask = getModelService().getObject(TaskType.class, taskOid, options, operationTask, result).asObjectable();
            taskDto = prepareTaskDto(loadedTask, result);
			result.computeStatus();
		} catch (Exception ex) {
			result.recordFatalError("Couldn't get task.", ex);
		}

		if (!result.isSuccess()) {
			showResult(result);
		}

		if (taskDto == null) {
			getSession().error(getString("PageOrgDiff.message.cantTaskDetails"));
			if (!result.isSuccess()) {
				showResultInSession(result);
			}
            throw getRestartResponseException(PageTasks.class);
		}
		return taskDto;
	}
    
    private TaskDto prepareTaskDto(TaskType task, OperationResult result) throws SchemaException, ObjectNotFoundException {
        TaskDto taskDto = new TaskDto(task, getModelService(), getTaskService(), getModelInteractionService(),
                getTaskManager(), TaskDtoProviderOptions.fullOptions(), result, this);
        for (TaskType child : task.getSubtask()) {
            taskDto.addChildTaskDto(prepareTaskDto(child, result));
        }
        return taskDto;
    }
    
    //IMPORT TASK OPERATIN RESULT
	private List<IColumn<OperationResult, String>> initResultColumns() {
		List<IColumn<OperationResult, String>> columns = new ArrayList<IColumn<OperationResult, String>>();

		columns.add(new PropertyColumn(createStringResource("PageOrgDiff.opResult.token"), "token"));
		columns.add(new PropertyColumn(createStringResource("PageOrgDiff.opResult.operation"), "operation"));
		columns.add(new PropertyColumn(createStringResource("PageOrgDiff.opResult.status"), "status"));
		columns.add(new PropertyColumn(createStringResource("PageOrgDiff.opResult.message"), "message"));
		
		return columns;
	}
	
	/*COMPARE ISLEMI SONUCU FARKLILIKLARI SAYFADA GOSTEREN LABELLAR BUTUNU*/
	private void initFark(final Form mainForm){
		mainForm.add(new Label(ID_DELETED_LABEL, Integer.toString(utils.getTargetDeleted())));
		mainForm.add(new Label(ID_ADDED_LABEL, Integer.toString(utils.getTargetAdded())));
		mainForm.add(new Label(ID_COUNT, Integer.toString(utils.getCount())));
		mainForm.add(new Label(ID_ORGSAYI_LABEL,utils.getOrgSayi()));
		mainForm.add(new Label(ID_ENGEREK_ORG, Integer.toString(utils.getEngerekOrg())));
		mainForm.add(new Label(ID_DETSIS_ORG, Integer.toString(utils.getDetsisOrg())));
	}
	
	
	/*TODO: 
	 * DETSISWS SISTEMINDEN SILINEN ORGANIZASYON YAPILARININ ENGEREKTE MEVCUTTA BULUNAN DURUMLARININ DISABLE OLARAK SET EDILMESI ISLEMI*/
	private void disableOrgPerformed(AjaxRequestTarget target, String detsisNo){
		OperationResult result = new OperationResult(DELETE_UNIT);
		PageBase page = getPageBase();
		String oid = getOID(detsisNo);
		ObjectDelta delta =WebModelUtils.createActivationAdminStatusDelta(OrgType.class, oid, false, page.getPrismContext());
		WebModelUtils.save(delta, result, page);
		result.computeStatus();
		
	}
	
	private PageBase getPageBase() {
        return (PageBase) getPage();
    }
	
		
	/*TODO DISABLE EDILECEK ORGANIZASYONUN DETSIS NAME'I KULLANILARAK BU METHOD UZERINDEN OID TESPITI EDILIYOR*/
	  private String getOID(String ds){
        String s1,s2;
        s1=ds.substring(0, 4); //ilk 4
        s2=ds.substring(4,8);  //son 4
        ds= ds + "-DETS-" + s1+ "-" + s2 +"-" + s1+s2+"000D";
        return ds;
  }
	
	
	  /*TODO: COMPARE METHODU*/
	private void compareXMLPerformed(AjaxRequestTarget trg){

        try {
        	OperationResult result = new OperationResult(OPERATION_COMPARE_ORGS);
        	
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            
            /*TODO: 
             * BU KISIM SOURCE FOLDERIN PATH + NAME OZELLIKLERINI SET EDIYOR. 
             * SISTEMIN HALIHAZIRDA VAR OLAN YAPISININ EXPORT EDILMIS HALI SOURCE DOSYAMIZI OLUSTURUYOR(DETSISXMLFILE_OUT)
             * PAGEORGDIFF.PROPERTIES DOSYASINDAN ADI DEGISTIRILEBILIR. PATH KISMI SU AN KOD ICERISINDEKI "MAIN_FOLDER" DEGISKENINE BAGLIDIR.*/
            StringBuilder source = new StringBuilder();
            source.append(MAIN_FOLDER);
            source.append(createStringResource("PageOrgDiff.filePath.source").getString());
            LOGGER.debug("SOURCE PATH INFO: "+source.toString());
            
            /*TODO: 
             * BU KISIM TARGET FOLDERIN PATH + NAME OZELLIKLERINI SET EDIYOR. 
             * DETSISWS UZERINDEN CEKILEREK OLUSTURULMUS DETSISXMLFILE.XML FILE TARGET DOSYAMIZDIR.
             * PAGEORGDIFF.PROPERTIES DOSYASINDAN ADI DEGISTIRILEBILIR. PATH KISMI SU AN KOD ICERISINDEKI "MAIN_FOLDER" DEGISKENINE BAGLIDIR.*/
            StringBuilder target = new StringBuilder();
            target.append(MAIN_FOLDER);
            target.append(createStringResource("PageOrgDiff.filePath.target").getString());
            LOGGER.debug("SOURCE PATH INFO: "+target.toString());
            
            /*YUKARIDAKI ACIKLAMALARA BAGLI OLARAK SOURCEDOC = EXPORT DOC, TARGETDOC=DETSISWS DOC*/
            Document sourceDoc = documentBuilder.parse(new File(source.toString()));
            Document targetDoc = documentBuilder.parse(new File(target.toString()));

            //SOURCE -ENGEREK REPO
            sourceDoc.getDocumentElement().normalize();
            NodeList orgListSource = xp.getNodeList(sourceDoc, "org");
            
            /*SOURCE DOSYASINA BAGLI HASHMAP TANIMI*/
            final Map<String, String> testSRCMap = new HashMap<String, String>();

            /* TODO: SOURCE DOC DA BULUNAN XML YAPISI NODELIST YARDIMI ILE HASHMAP'E AKTARILIYOR. 
             * KEY OLARAK GETNAME(DETSISID) VALUE OLARAK ADMINISTRATIVE STATUS KULLANILDI.
             * BU SAYEDE DISABLE VE ENABLE ORGANIZASYON FARKI ANLASILABILIYOR
             */
            for(DTVTOrg item: xp.getOrgValues(xp.getNodes(orgListSource))){
            	testSRCMap.put(item.getName(), item.getAdministrativeStatus());
            }
            
            //TARGET - WEB SERVICE
            targetDoc.getDocumentElement().normalize();
            
            /*TODO: DETSISWS(TARGET) DOSYASINA BAGLI HASHMAP TANIMI*/
            final Map<String, String> testTRGMap = new HashMap<String, String>();
            NodeList orgListTarget = xp.getNodeList(targetDoc, "org");
            
          //TODO: TARGET(DETSISWS) DOSYASINDA BULUNAN XML YAPISI NODELIST YARDIMI ILE HASHMAP'E AKTARILIYOR
            for(DTVTOrg item: xp.getOrgValues(xp.getNodes(orgListTarget))){
            	testTRGMap.put(item.getName(), item.getAdministrativeStatus());
            }
            
            /*TODO: TARGET VE EXPORT SISTEMINDEKI ORGANIZASYON SAYILARI AYNI ISE SETORGSAYI DEGISKENI 
             * PROPERTIES SAYFASINDAN BELIRTILDIGI SEKILDE SET EDILIYOR
             * FARKLI ISE YINE AYNI SEKILDE PROPERTIES'DEN SET EDILIYOR*/
            if (testSRCMap.size()==testTRGMap.size()){
              
                utils.setOrgSayi(createStringResource("PageOrgDiff.orgSayiAyni").getString());
                utils.setEngerekOrg(testSRCMap.size());
                utils.setDetsisOrg(testTRGMap.size());


            }else{
                utils.setOrgSayi(createStringResource("PageOrgDiff.orgSayiFarkli").getString());
                utils.setEngerekOrg(testSRCMap.size());
                utils.setDetsisOrg(testTRGMap.size());
                
            }
            
            //KARSILASTIRMA ICIN KULLANILAN COUNT
            utils.setCount(0);
            utils.getDeletedMap().clear();
            utils.getAddedMap().clear();
            
            
            for (final String key : testSRCMap.keySet()) {
            	String val = testSRCMap.get(key);
            	//System.out.println("key: "+key + ", value: " + val);
            	//System.out.println("val:" + val);
            	if(val.equalsIgnoreCase("enabled")){
	                if (testTRGMap.containsKey(key)) {
	                    utils.setCount(utils.getCount()+1);
	                }else{
	                	utils.getDeletedMap().put(key, testSRCMap.get(key));
	                }
	            }
            }
            for (final String key : testTRGMap.keySet()) {
                if (!testSRCMap.containsKey(key)) {
                	utils.getAddedMap().put(key, testTRGMap.get(key));
                }
            }
            
            //SILINEN LISTE DEBUG
            if(!utils.getDeletedMap().isEmpty()){
	            for(String org : utils.getDeletedMap().keySet()){
	            	LOGGER.info("Delete org: " + org);
	            	
	            }
            }
            
            //YENI EKLENECEK LISTE DEBUG
            if(!utils.getAddedMap().isEmpty()){
	            for(String org : utils.getAddedMap().keySet()){
	            	LOGGER.info("New org: " + org);
	            }
            }
            
            LOGGER.info("COUNTER: "+utils.getCount());
            
            /*SISTEM VE DETSISWS ARASINDAKI FARKLILIKLARI YANSITAN METODLAR*/
        //    if(testSRCMap.size()>utils.getCount()){
            	utils.setTargetDeleted(utils.getDeletedMap().size());
            	LOGGER.info("Import edilecek dosyada "+ utils.getCount() + " adet import dosyasındakilerle eşleşiyor.");
            	LOGGER.info("Import edilecek dosyada " + utils.getTargetDeleted()+ " adet silimiş oranizasyon var." );
       //     } else if(testTRGMap.size()>utils.getCount()) {
                utils.setTargetAdded(utils.getAddedMap().size());
                LOGGER.info("Import edilecek dosyada "+ utils.getCount() + " adet import dosyasındakilerle eşleşiyor.");
                LOGGER.info("Import edilecek dosyada " + utils.getTargetAdded()+ " adet eklenmiş oranizasyon var." );
                

       //     }
            showResultInSession(result);
            setResponsePage(PageOrgDiff.class);
            //trg.add(getFeedbackPanel());
            

        } catch (SAXParseException e) {
            System.out.println("** Parsing error, line " +
                    e.getLineNumber() + ",uri " + e.getSystemId());
            System.out.println(" " + e.getMessage());
        } catch (SAXException err) {
            Exception x = err.getException();
            ((x == null) ? err : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }

	}
	
	  public boolean isMapEmpty(Map<String,String> mp){
      	if(mp.isEmpty())
      		return true;
      	return false;
      }
	  
	  /*TODO: EXPORT ISLEMINI BASLATAN METOD*/
	 private void exportAllType(AjaxRequestTarget target) {
		 LOGGER.info("***EXPORT IS STARTING***");
	        initDownload(target, OrgType.class, null);
	    }
	 
	 /*EXPORT ISLEMI ICIN OZELLESTIRILMIS DOWNLOAD BEHAVIOUR YAPISINI CAGIRAN METOD*/
	 private void initDownload(AjaxRequestTarget target, Class<? extends ObjectType> type, ObjectQuery query) {
		 OperationResult result = new OperationResult(OPERATION_EXPORT_ORGS);
	        OrgTypeDownloadBehaviour downloadBehaviour = new OrgTypeDownloadBehaviour();

	        downloadBehaviour.setType(type);
	        downloadBehaviour.setQuery(query);
	        downloadBehaviour.setUseZip(false);
	        downloadBehaviour.initFile();
	        
	        showResultInSession(result);
	        setResponsePage(PageOrgDiff.class);

	    }
	 
}
