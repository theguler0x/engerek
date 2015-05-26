/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.certification.impl;

import com.evolveum.midpoint.certification.api.CertificationManager;
import com.evolveum.midpoint.certification.impl.handlers.CertificationHandler;
import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.model.api.PolicyViolationException;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.parser.XNodeSerializer;
import com.evolveum.midpoint.prism.path.IdItemPathSegment;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.NameItemPathSegment;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.EqualFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrderDirection;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.ObjectDeltaOperation;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.CertCampaignTypeUtil;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.security.api.SecurityEnforcer;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCaseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCasesStatisticsEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCasesStatisticsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationDecisionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationStageType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import net.sf.jasperreports.engine.util.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType.CLOSED;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType.CREATED;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType.IN_REMEDIATION;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType.IN_REVIEW_STAGE;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignStateType.REVIEW_STAGE_DONE;

/**
 * @author mederly
 */
@Service(value = "certificationManager")
public class CertificationManagerImpl implements CertificationManager {

    private static final transient Trace LOGGER = TraceManager.getTrace(CertificationManager.class);

    public static final String INTERFACE_DOT = CertificationManager.class.getName() + ".";
    public static final String OPERATION_CREATE_CAMPAIGN = INTERFACE_DOT + "createCampaign";
    public static final String OPERATION_OPEN_NEXT_STAGE = INTERFACE_DOT + "openNextStage";
    public static final String OPERATION_CLOSE_CURRENT_STAGE = INTERFACE_DOT + "closeCurrentStage";
    public static final String OPERATION_RECORD_DECISION = INTERFACE_DOT + "recordDecision";
    public static final String OPERATION_SEARCH_CASES = INTERFACE_DOT + "searchCases";
    public static final String OPERATION_SEARCH_DECISIONS = INTERFACE_DOT + "searchDecisions";
    public static final String OPERATION_CLOSE_CAMPAIGN = INTERFACE_DOT + "closeCampaign";
    public static final String OPERATION_GET_CAMPAIGN_STATISTICS = INTERFACE_DOT + "getCampaignStatistics";

    @Autowired
    private PrismContext prismContext;

    @Autowired
    private ModelService modelService;

    @Autowired
    protected SecurityEnforcer securityEnforcer;

    @Autowired
    private MatchingRuleRegistry matchingRuleRegistry;

    @Autowired
    protected AccCertGeneralHelper helper;

    @Autowired
    private AccessCertificationRemediationTaskHandler remediationTaskHandler;

    // TODO temporary hack because of some problems in model service...
    @Autowired
    @Qualifier("cacheRepositoryService")
    protected RepositoryService repositoryService;

    private PrismObjectDefinition<AccessCertificationCampaignType> campaignObjectDefinition = null;     // lazily evaluated

    private Map<String,CertificationHandler> registeredHandlers = new HashMap<>();

    public void registerHandler(String handlerUri, CertificationHandler handler) {
        if (registeredHandlers.containsKey(handlerUri)) {
            throw new IllegalStateException("There is already a handler with URI " + handlerUri);
        }
        registeredHandlers.put(handlerUri, handler);
    }

    public CertificationHandler findCertificationHandler(AccessCertificationDefinitionType accessCertificationDefinitionType) {
        if (StringUtils.isBlank(accessCertificationDefinitionType.getHandlerUri())) {
            throw new IllegalArgumentException("No handler URI for access certification definition " + ObjectTypeUtil.toShortString(accessCertificationDefinitionType));
        }
        CertificationHandler handler = registeredHandlers.get(accessCertificationDefinitionType.getHandlerUri());
        if (handler == null) {
            throw new IllegalStateException("No handler for URI " + accessCertificationDefinitionType.getHandlerUri());
        }
        return handler;
    }

    @Override
    public AccessCertificationCampaignType createCampaign(AccessCertificationDefinitionType certDefinition, AccessCertificationCampaignType campaign,
                                                          Task task, OperationResult parentResult) throws SchemaException, SecurityViolationException, ConfigurationException, ObjectNotFoundException, CommunicationException, ExpressionEvaluationException, ObjectAlreadyExistsException, PolicyViolationException {
        Validate.notNull(certDefinition, "certificationDefinition");
        Validate.notNull(certDefinition.getOid(), "certificationDefinition.oid");
        if (campaign != null) {
            Validate.isTrue(campaign.getOid() == null, "Certification campaign with non-null OID is not permitted.");
        }
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CREATE_CAMPAIGN);
        try {
            AccessCertificationCampaignType newCampaign = createCampaignObject(certDefinition, campaign, task, result);
            addObject(newCampaign, task, result);
            return newCampaign;
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't create certification campaign: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    private AccessCertificationCampaignType createCampaignObject(AccessCertificationDefinitionType definition, AccessCertificationCampaignType campaign,
                                                                 Task task, OperationResult result) throws SecurityViolationException {
        AccessCertificationCampaignType newCampaign = new AccessCertificationCampaignType(prismContext);
        Date now = new Date();

        if (campaign != null && campaign.getName() != null) {
            campaign.setName(campaign.getName());
        } else {
            newCampaign.setName(new PolyStringType("Campaign for " + definition.getName().getOrig() + " started " + now));
        }

        if (campaign != null && campaign.getDescription() != null) {
            newCampaign.setDescription(newCampaign.getDescription());
        } else {
            newCampaign.setDescription(definition.getDescription());
        }

        if (campaign != null && campaign.getOwnerRef() != null) {
            newCampaign.setOwnerRef(campaign.getOwnerRef());
        } else if (definition.getOwnerRef() != null) {
            newCampaign.setOwnerRef(definition.getOwnerRef());
        } else {
            newCampaign.setOwnerRef(securityEnforcer.getPrincipal().toObjectReference());
        }

        if (campaign != null && campaign.getTenantRef() != null) {
            newCampaign.setTenantRef(campaign.getTenantRef());
        } else {
            newCampaign.setTenantRef(definition.getTenantRef());
        }

        newCampaign.setCurrentStageNumber(0);

        ObjectReferenceType typeRef = new ObjectReferenceType();
        typeRef.setType(AccessCertificationDefinitionType.COMPLEX_TYPE);
        typeRef.setOid(definition.getOid());
        newCampaign.setDefinitionRef(typeRef);
        newCampaign.setState(CREATED);

        return newCampaign;
    }

    @Override
    public void openNextStage(String campaignOid, int requestedStageNumber, Task task, OperationResult parentResult) throws SchemaException, SecurityViolationException, ConfigurationException, ObjectNotFoundException, CommunicationException, ExpressionEvaluationException, ObjectAlreadyExistsException, PolicyViolationException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_OPEN_NEXT_STAGE);
        result.addParam("campaignOid", campaignOid);
        result.addParam("requestedStageNumber", requestedStageNumber);

        try {
            AccessCertificationCampaignType campaign = helper.getCampaignWithDefinition(campaignOid, task, result);
            result.addParam("campaign", ObjectTypeUtil.toShortString(campaign));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("openNextStage starting for {}", ObjectTypeUtil.toShortString(campaign));
            }

            AccessCertificationDefinitionType certDefinition = CertCampaignTypeUtil.getDefinition(campaign);

            final int currentStageNumber = CertCampaignTypeUtil.getCurrentStageNumber(campaign);
            final int stages = certDefinition.getStage().size();
            final AccessCertificationCampaignStateType state = campaign.getState();
            LOGGER.trace("openNextStage: currentStageNumber={}, stages={}, requestedStageNumber={}, state={}", currentStageNumber, stages, requestedStageNumber, state);
            if (IN_REVIEW_STAGE.equals(state)) {
                result.recordFatalError("Couldn't advance to review stage " + requestedStageNumber + " as the stage " + currentStageNumber + " is currently open.");
            } else if (IN_REMEDIATION.equals(state)) {
                result.recordFatalError("Couldn't advance to review stage " + requestedStageNumber + " as the campaign is currently in the remediation phase.");
            } else if (CLOSED.equals(state)) {
                result.recordFatalError("Couldn't advance to review stage " + requestedStageNumber + " as the campaign is already closed.");
            } else if (!REVIEW_STAGE_DONE.equals(state) && !CREATED.equals(state)) {
                throw new IllegalStateException("Unexpected campaign state: " + state);
            } else if (REVIEW_STAGE_DONE.equals(state) && requestedStageNumber != currentStageNumber+1) {
                result.recordFatalError("Couldn't advance to review stage " + requestedStageNumber + " as the campaign is currently in stage " + currentStageNumber);
            } else if (CREATED.equals(state) && requestedStageNumber != 1) {
                result.recordFatalError("Couldn't advance to review stage " + requestedStageNumber + " as the campaign was just created");
            } else if (requestedStageNumber > stages) {
                result.recordFatalError("Couldn't advance to review stage " + requestedStageNumber + " as the campaign has only " + stages + " stages");
            } else {
                CertificationHandler handler = findCertificationHandler(certDefinition);
                handler.moveToNextStage(certDefinition, campaign, task, result);

                // some bureaucracy... stage#, state, start time
                List<ItemDelta> itemDeltaList = new ArrayList<>();
                PropertyDelta<Integer> stageNumberDelta = createStageNumberDelta(requestedStageNumber);
                itemDeltaList.add(stageNumberDelta);
                PropertyDelta<AccessCertificationCampaignStateType> stateDelta = createStateDelta(IN_REVIEW_STAGE);
                itemDeltaList.add(stateDelta);
                if (requestedStageNumber == 1) {
                    PropertyDelta<XMLGregorianCalendar> startDelta = createStartTimeDelta(XmlTypeConverter.createXMLGregorianCalendar(new Date()));
                    itemDeltaList.add(startDelta);
                }
                repositoryService.modifyObject(AccessCertificationCampaignType.class, campaign.getOid(), itemDeltaList, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't move to certification campaign stage " + requestedStageNumber + ": unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public void closeCurrentStage(String campaignOid, int stageNumberToClose, Task task, OperationResult parentResult) throws SchemaException, SecurityViolationException, ConfigurationException, ObjectNotFoundException, CommunicationException, ExpressionEvaluationException, ObjectAlreadyExistsException, PolicyViolationException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CLOSE_CURRENT_STAGE);
        result.addParam("campaignOid", campaignOid);
        result.addParam("stageNumber", stageNumberToClose);

        try {
            AccessCertificationCampaignType campaign = helper.getCampaignWithDefinition(campaignOid, task, result);
            result.addParam("campaign", ObjectTypeUtil.toShortString(campaign));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("closeCurrentStage starting for {}", ObjectTypeUtil.toShortString(campaign));
            }

            AccessCertificationDefinitionType certDefinition = CertCampaignTypeUtil.getDefinition(campaign);

            final int currentStageNumber = CertCampaignTypeUtil.getCurrentStageNumber(campaign);
            final int stages = certDefinition.getStage().size();
            final AccessCertificationCampaignStateType state = campaign.getState();
            LOGGER.trace("closeCurrentStage: currentStageNumber={}, stages={}, stageNumberToClose={}, state={}", currentStageNumber, stages, stageNumberToClose, state);

            if (stageNumberToClose != currentStageNumber) {
                result.recordFatalError("Couldn't close review stage " + stageNumberToClose + " as the campaign is not in that stage");
            } else if (!IN_REVIEW_STAGE.equals(state)) {
                result.recordFatalError("Couldn't close review stage " + stageNumberToClose + " as it is currently not open");
            } else {
                List<ItemDelta> campaignDeltaList = createCurrentResponsesDeltas(campaign);
                PropertyDelta<AccessCertificationCampaignStateType> stateDelta = createStateDelta(REVIEW_STAGE_DONE);
                campaignDeltaList.add(stateDelta);
                repositoryService.modifyObject(AccessCertificationCampaignType.class, campaign.getOid(), campaignDeltaList, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't close certification campaign stage " + stageNumberToClose+ ": unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    private List<ItemDelta> createCurrentResponsesDeltas(AccessCertificationCampaignType campaign) {
        List<ItemDelta> campaignDeltaList = new ArrayList<>();

        LOGGER.trace("Updating current response for cases in {}", ObjectTypeUtil.toShortString(campaign));
        AccessCertificationDefinitionType definition = CertCampaignTypeUtil.getDefinition(campaign);
        List<AccessCertificationCaseType> caseList = campaign.getCase();

        for (int i = 0; i < caseList.size(); i++) {
            AccessCertificationCaseType _case = caseList.get(i);
            AccessCertificationResponseType newResponse = helper.computeResponseForStage(_case, campaign, definition);
            if (newResponse != _case.getCurrentResponse()) {
                PropertyDelta currentResponseDelta = PropertyDelta.createModificationReplaceProperty(
                        new ItemPath(
                                new NameItemPathSegment(AccessCertificationCampaignType.F_CASE),
                                new IdItemPathSegment(_case.asPrismContainerValue().getId()),
                                new NameItemPathSegment(AccessCertificationCaseType.F_CURRENT_RESPONSE)),
                        getCampaignDefinition(), newResponse);
                campaignDeltaList.add(currentResponseDelta);
            }
        }
        return campaignDeltaList;
    }

    @Override
    public void startRemediation(String campaignOid, Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException, SecurityViolationException, ObjectAlreadyExistsException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CLOSE_CURRENT_STAGE);
        result.addParam("campaignOid", campaignOid);

        try {
            AccessCertificationCampaignType campaign = helper.getCampaignWithDefinition(campaignOid, task, result);
            result.addParam("campaign", ObjectTypeUtil.toShortString(campaign));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("startRemediation starting for {}", ObjectTypeUtil.toShortString(campaign));
            }

            AccessCertificationDefinitionType certDefinition = CertCampaignTypeUtil.getDefinition(campaign);

            final int currentStageNumber = CertCampaignTypeUtil.getCurrentStageNumber(campaign);
            final int lastStageNumber = certDefinition.getStage().size();
            final AccessCertificationCampaignStateType state = campaign.getState();
            LOGGER.trace("startRemediation: currentStageNumber={}, stages={}, state={}", currentStageNumber, lastStageNumber, state);

            if (currentStageNumber != lastStageNumber) {
                result.recordFatalError("Couldn't start the remediation as the campaign is not in its last stage ("+lastStageNumber+"); current stage: "+currentStageNumber);
            } else if (!REVIEW_STAGE_DONE.equals(state)) {
                result.recordFatalError("Couldn't start the remediation as the last stage was not properly closed.");
            } else {
                if (CertCampaignTypeUtil.isRemediationAutomatic(certDefinition)) {
                    remediationTaskHandler.launch(campaign, task, result);
                } else {
                    result.recordWarning("The automated remediation is not configured. The campaign state was set to IN REMEDIATION, but all remediation actions have to be done by hand.");
                }
                setStageNumberAndState(campaign, lastStageNumber + 1, IN_REMEDIATION, task, result);
            }
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't start the remediation: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    private void addObject(ObjectType objectType, Task task, OperationResult result) throws CommunicationException, ObjectAlreadyExistsException, ExpressionEvaluationException, PolicyViolationException, SchemaException, SecurityViolationException, ConfigurationException, ObjectNotFoundException {
        ObjectDelta objectDelta = ObjectDelta.createAddDelta(objectType.asPrismObject());
        Collection<ObjectDeltaOperation<?>> ops = modelService.executeChanges((Collection) Arrays.asList(objectDelta), null, task, result);
        ObjectDeltaOperation odo = ops.iterator().next();
        objectType.setOid(odo.getObjectDelta().getOid());
    }

    @Override
    public List<AccessCertificationCaseType> searchCases(String campaignOid, ObjectQuery query,
                                                         Collection<SelectorOptions<GetOperationOptions>> options,
                                                         Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, SecurityViolationException, ConfigurationException, CommunicationException {

        Validate.notEmpty(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_SEARCH_CASES);

        // temporary implementation: simply fetches the whole campaign and selects requested items by itself
        try {
            ObjectFilter filter = query != null ? query.getFilter() : null;
            ObjectPaging paging = query != null ? query.getPaging() : null;
            AccessCertificationCampaignType campaign = helper.getCampaign(campaignOid, options, task, result);
            List<AccessCertificationCaseType> caseList = getCases(campaign, filter, task, result);
            caseList = doSortingAndPaging(caseList, paging);
            return caseList;
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't search for certification cases: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public List<AccessCertificationCaseType> searchDecisions(ObjectQuery campaignQuery, ObjectQuery caseQuery,
                                                             String reviewerOid,
                                                             Collection<SelectorOptions<GetOperationOptions>> options,
                                                             Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, SecurityViolationException, ConfigurationException, CommunicationException {

        Validate.notNull(reviewerOid, "reviewerOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_SEARCH_DECISIONS);

        try {
            // enhance filter with reviewerRef
            ObjectFilter enhancedFilter;
            ObjectReferenceType reviewerRef = ObjectTypeUtil.createObjectRef(reviewerOid, ObjectTypes.USER);
            ObjectFilter reviewerFilter = RefFilter.createReferenceEqual(
                    new ItemPath(AccessCertificationCaseType.F_REVIEWER_REF),
                    AccessCertificationCaseType.class, prismContext, reviewerRef.asReferenceValue());
            ObjectFilter enabledFilter = EqualFilter.createEqual(
                    AccessCertificationCaseType.F_ENABLED, AccessCertificationCaseType.class, prismContext, Boolean.TRUE);
            ObjectFilter andFilter = AndFilter.createAnd(reviewerFilter, enabledFilter);

            if (caseQuery == null || caseQuery.getFilter() == null) {
                enhancedFilter = andFilter;
            } else {
                enhancedFilter = AndFilter.createAnd(caseQuery.getFilter(), andFilter);
            }

            // retrieve cases, filtered
            List<PrismObject<AccessCertificationCampaignType>> campaignObjects = modelService.searchObjects(AccessCertificationCampaignType.class, campaignQuery, options, task, result);
            List<AccessCertificationCaseType> caseList = new ArrayList<>();
            for (PrismObject<AccessCertificationCampaignType> campaignObject : campaignObjects) {
                AccessCertificationCampaignType campaign = campaignObject.asObjectable();
                AccessCertificationDefinitionType definition = helper.resolveCertificationDef(campaign, task, result);
                if (!AccessCertificationCampaignStateType.IN_REVIEW_STAGE.equals(campaign.getState())) {
                    continue;
                }
                List<AccessCertificationCaseType> campaignCases = getCases(campaign, enhancedFilter, task, result);

                // remove irrelevant decisions from each case
                // and add campaignRef
                int stage = CertCampaignTypeUtil.getCurrentStageNumber(campaign);
                for (AccessCertificationCaseType _case : campaignCases) {
                    Iterator<AccessCertificationDecisionType> decisionIterator = _case.getDecision().iterator();
                    while (decisionIterator.hasNext()) {
                        AccessCertificationDecisionType decision = decisionIterator.next();
                        if (decision.getStageNumber() != stage || !decision.getReviewerRef().getOid().equals(reviewerOid)) {
                            decisionIterator.remove();
                        }
                    }
                    _case.setCampaignRef(ObjectTypeUtil.createObjectRef(campaignObject));
                    caseList.add(_case);
                }
            }

            // sort and page cases
            ObjectPaging paging = caseQuery != null ? caseQuery.getPaging() : null;
            caseList = doSortingAndPaging(caseList, paging);

            return caseList;
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't search for certification decisions: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    protected List<AccessCertificationCaseType> doSortingAndPaging(List<AccessCertificationCaseType> caseList, ObjectPaging paging) {
        if (paging == null) {
            return caseList;
        }

        // sorting
        if (paging.getOrderBy() != null) {
            Comparator<AccessCertificationCaseType> comparator = createComparator(paging.getOrderBy(), paging.getDirection());
            if (comparator != null) {
                caseList = new ArrayList<>(caseList);
                Collections.sort(caseList, comparator);
            }
        }
        // paging
        if (paging.getOffset() != null || paging.getMaxSize() != null) {
            int offset = paging.getOffset() != null ? paging.getOffset() : 0;
            int maxSize = paging.getMaxSize() != null ? paging.getMaxSize() : Integer.MAX_VALUE;
            if (offset >= caseList.size()) {
                caseList = new ArrayList<>();
            } else {
                if (maxSize > caseList.size() - offset) {
                    maxSize = caseList.size() - offset;
                }
                caseList = caseList.subList(offset, offset+maxSize);
            }
        }
        return caseList;
    }

    protected List<AccessCertificationCaseType> getCases(AccessCertificationCampaignType campaign, ObjectFilter filter, Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, SecurityViolationException, CommunicationException, ConfigurationException {
        // temporary implementation: simply fetches the whole campaign and selects requested items by itself
        List<AccessCertificationCaseType> caseList = campaign.getCase();

        // filter items
        if (filter != null) {
            Iterator<AccessCertificationCaseType> caseIterator = caseList.iterator();
            while (caseIterator.hasNext()) {
                AccessCertificationCaseType _case = caseIterator.next();
                if (!ObjectQuery.match(_case, filter, matchingRuleRegistry)) {
                    caseIterator.remove();
                }
            }
        }

        return caseList;
    }

    /**
     * Experimental implementation: we support ordering by subject object name, target object name.
     * However, there are no QNames that exactly match these options. So we map the following QNames to ordering attributes:
     *
     * F_SUBJECT_REF -> ordering by subject name
     * F_TARGET_REF -> ordering by target name
     *
     * The requirement is that object names were fetched as well (resolveNames option)
     *
     */
    private Comparator<AccessCertificationCaseType> createComparator(QName orderBy, OrderDirection direction) {
        if (QNameUtil.match(orderBy, AccessCertificationCaseType.F_SUBJECT_REF)) {
            return createSubjectNameComparator(direction);
        } else if (QNameUtil.match(orderBy, AccessCertificationCaseType.F_TARGET_REF)) {
            return createTargetNameComparator(direction);
        } else {
            LOGGER.warn("Unsupported sorting attribute {}. Results will not be sorted.", orderBy);
            return null;
        }
    }

    private Comparator<AccessCertificationCaseType> createSubjectNameComparator(final OrderDirection direction) {
        return new Comparator<AccessCertificationCaseType>() {
            @Override
            public int compare(AccessCertificationCaseType o1, AccessCertificationCaseType o2) {
                return compareRefNames(o1.getSubjectRef(), o2.getSubjectRef(), direction);
            }
        };
    }

    private Comparator<AccessCertificationCaseType> createTargetNameComparator(final OrderDirection direction) {
        return new Comparator<AccessCertificationCaseType>() {
            @Override
            public int compare(AccessCertificationCaseType o1, AccessCertificationCaseType o2) {
                return compareRefNames(o1.getTargetRef(), o2.getTargetRef(), direction);
            }
        };
    }

    private int compareRefNames(ObjectReferenceType leftRef, ObjectReferenceType rightRef, OrderDirection direction) {
        if (leftRef == null) {
            return respectDirection(1, direction);      // null > anything
        } else if (rightRef == null) {
            return respectDirection(-1, direction);     // anything < null
        }

        // brutal hack - we (mis)use the fact that names are serialized as XNode comments
        String leftName = (String) leftRef.asReferenceValue().getUserData(XNodeSerializer.USER_DATA_KEY_COMMENT);
        String rightName = (String) rightRef.asReferenceValue().getUserData(XNodeSerializer.USER_DATA_KEY_COMMENT);
        if (leftName == null) {
            return respectDirection(1, direction);      // null > anything
        } else if (rightName == null) {
            return respectDirection(-1, direction);     // anything < null
        }

        int ordering = leftName.compareTo(rightName);
        return respectDirection(ordering, direction);
    }

    private int respectDirection(int ordering, OrderDirection direction) {
        if (direction == OrderDirection.ASCENDING) {
            return ordering;
        } else {
            return -ordering;
        }
    }

    @Override
    public void recordDecision(String campaignOid, long caseId, AccessCertificationDecisionType decision,
                               Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, SecurityViolationException, ConfigurationException, CommunicationException, ObjectAlreadyExistsException {

        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(decision, "decision");

        OperationResult result = parentResult.createSubresult(OPERATION_RECORD_DECISION);
        try {
            AccessCertificationCampaignType campaign = helper.getCampaignWithDefinition(campaignOid, task, result);
            AccessCertificationDefinitionType definition = CertCampaignTypeUtil.getDefinition(campaign);

            if (!AccessCertificationCampaignStateType.IN_REVIEW_STAGE.equals(campaign.getState())) {
                throw new IllegalStateException("Campaign is not in review stage; its state is " + campaign.getState());
            }

            int currentStage = CertCampaignTypeUtil.getCurrentStageNumber(campaign);
            if (decision.getStageNumber() != 0 && decision.getStageNumber() != currentStage) {
                throw new IllegalStateException("Cannot add decision with stage number (" + decision.getStageNumber() + ") other than current (" + currentStage + ")");
            }
            if (decision.getTimestamp() == null) {
                decision.setTimestamp(XmlTypeConverter.createXMLGregorianCalendar(System.currentTimeMillis()));
            }
            AccessCertificationCaseType _case = findCaseById(campaign, caseId);
            if (_case == null) {
                throw new ObjectNotFoundException("Case " + caseId + " was not found in campaign " + ObjectTypeUtil.toShortString(campaign));
            }
            if (!_case.isEnabled()) {
                throw new IllegalStateException("Cannot update case because it is not update-enabled in this stage");
            }
            ObjectReferenceType currentReviewerRef = decision.getReviewerRef();
            if (currentReviewerRef == null) {
                UserType currentUser = securityEnforcer.getPrincipal().getUser();
                currentReviewerRef = ObjectTypeUtil.createObjectRef(currentUser);
            }

            Long existingDecisionId = null;
            for (AccessCertificationDecisionType d : _case.getDecision()) {
                if (d.getStageNumber() == currentStage &&
                        d.getReviewerRef().getOid().equals(currentReviewerRef.getOid())) {
                    existingDecisionId = d.asPrismContainerValue().getId();
                    break;
                }
            }
            AccessCertificationDecisionType decisionWithCorrectId = decision.clone();

            if (decisionWithCorrectId.getTimestamp() == null) {
                decisionWithCorrectId.setTimestamp(XmlTypeConverter.createXMLGregorianCalendar(new Date()));
            }
            if (decisionWithCorrectId.getReviewerRef() == null) {
                decisionWithCorrectId.setReviewerRef(currentReviewerRef);
            }
            if (decisionWithCorrectId.getStageNumber() == 0) {
                decisionWithCorrectId.setStageNumber(currentStage);
            }
            decisionWithCorrectId.asPrismContainerValue().setId(existingDecisionId);        // may be null if this is a new decision
            ItemPath decisionPath = new ItemPath(
                    new NameItemPathSegment(AccessCertificationCampaignType.F_CASE),
                    new IdItemPathSegment(caseId),
                    new NameItemPathSegment(AccessCertificationCaseType.F_DECISION));
            ContainerDelta<AccessCertificationDecisionType> decisionDelta =
                    ContainerDelta.createModificationReplace(decisionPath, AccessCertificationCampaignType.class, prismContext, decisionWithCorrectId);
            Collection<ItemDelta> deltaList = new ArrayList<>();
            deltaList.add(decisionDelta);

            AccessCertificationResponseType newResponse = helper.computeResponseForStage(_case, decisionWithCorrectId, campaign, definition);
            if (!ObjectUtils.equals(newResponse, _case.getCurrentResponse())) {
                PropertyDelta currentResponseDelta = PropertyDelta.createModificationReplaceProperty(
                        new ItemPath(
                                new NameItemPathSegment(AccessCertificationCampaignType.F_CASE),
                                new IdItemPathSegment(_case.asPrismContainerValue().getId()),
                                new NameItemPathSegment(AccessCertificationCaseType.F_CURRENT_RESPONSE)),
                        getCampaignDefinition(), newResponse);
                deltaList.add(currentResponseDelta);
            }

            // TODO: call model service instead of repository
            repositoryService.modifyObject(AccessCertificationCampaignType.class, campaignOid, deltaList, result);

        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't record reviewer decision: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    public PrismObjectDefinition<?> getCampaignDefinition() {
        if (campaignObjectDefinition != null) {
            return campaignObjectDefinition;
        }
        campaignObjectDefinition = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(AccessCertificationCampaignType.class);
        if (campaignObjectDefinition == null) {
            throw new IllegalStateException("Couldn't find definition for AccessCertificationCampaignType prism object");
        }
        return campaignObjectDefinition;
    }

    private AccessCertificationCaseType findCaseById(AccessCertificationCampaignType campaign, long caseId) {
        for (AccessCertificationCaseType _case : campaign.getCase()) {
            if (_case.asPrismContainerValue().getId() != null && _case.asPrismContainerValue().getId() == caseId) {
                return _case;
            }
        }
        return null;
    }

    private void setStageNumberAndState(AccessCertificationCampaignType campaign, int number, AccessCertificationCampaignStateType state,
                                        Task task, OperationResult result) throws ObjectAlreadyExistsException, ObjectNotFoundException, SchemaException {
        PropertyDelta<Integer> stageNumberDelta = createStageNumberDelta(number);
        PropertyDelta<AccessCertificationCampaignStateType> stateDelta = createStateDelta(state);
        // todo switch to model service
        repositoryService.modifyObject(AccessCertificationCampaignType.class, campaign.getOid(), Arrays.asList(stateDelta, stageNumberDelta), result);
    }

    private void setState(AccessCertificationCampaignType campaign, AccessCertificationCampaignStateType state, Task task, OperationResult result) throws ObjectAlreadyExistsException, ObjectNotFoundException, SchemaException {
        PropertyDelta<AccessCertificationCampaignStateType> stateDelta = createStateDelta(state);
        // todo switch to model service
        repositoryService.modifyObject(AccessCertificationCampaignType.class, campaign.getOid(), Arrays.asList(stateDelta), result);
    }

    private PropertyDelta<Integer> createStageNumberDelta(int number) {
        return PropertyDelta.createReplaceDelta(getCampaignDefinition(), AccessCertificationCampaignType.F_CURRENT_STAGE_NUMBER, number);
    }

    private PropertyDelta<AccessCertificationCampaignStateType> createStateDelta(AccessCertificationCampaignStateType state) {
        return PropertyDelta.createReplaceDelta(getCampaignDefinition(), AccessCertificationCampaignType.F_STATE, state);
    }

    private PropertyDelta<XMLGregorianCalendar> createStartTimeDelta(XMLGregorianCalendar date) {
        return PropertyDelta.createReplaceDelta(getCampaignDefinition(), AccessCertificationCampaignType.F_START, date);
    }

    @Override
    public void closeCampaign(String campaignOid, Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException, SecurityViolationException, ObjectAlreadyExistsException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_CLOSE_CAMPAIGN);

        try {
            AccessCertificationCampaignType campaign = helper.getCampaignWithDefinition(campaignOid, task, result);
            int currentStageNumber = campaign.getCurrentStageNumber();
            int lastStageNumber = CertCampaignTypeUtil.getNumberOfStages(campaign);
            AccessCertificationCampaignStateType state = campaign.getState();
            // TODO issue a warning if we are not in a correct state
            setStageNumberAndState(campaign, lastStageNumber + 1, CLOSED, task, result);
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't close certification campaign: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }

    @Override
    public AccessCertificationCasesStatisticsType getCampaignStatistics(String campaignOid, boolean currentStageOnly, Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException, SecurityViolationException, ConfigurationException, CommunicationException, ObjectAlreadyExistsException {
        Validate.notNull(campaignOid, "campaignOid");
        Validate.notNull(task, "task");
        Validate.notNull(parentResult, "parentResult");

        OperationResult result = parentResult.createSubresult(OPERATION_GET_CAMPAIGN_STATISTICS);
        try {
            AccessCertificationCampaignType campaign = helper.getCampaignWithDefinition(campaignOid, task, result);
            int currentStageNumber = campaign.getCurrentStageNumber();
            AccessCertificationCampaignStateType state = campaign.getState();

            Map<AccessCertificationResponseType,MutablePair<Integer,Integer>> statMap = new HashMap<>();
            for (AccessCertificationCaseType _case : campaign.getCase()) {
                if (currentStageOnly && !_case.isEnabled()) {
                    continue;
                }
                AccessCertificationResponseType response = _case.getCurrentResponse();
                if (response == null) {
                    response = AccessCertificationResponseType.NO_RESPONSE;
                }
                MutablePair<Integer,Integer> pair = statMap.get(response);
                if (pair == null) {
                    pair = new MutablePair<>(0, 0);
                    statMap.put(response, pair);
                }
                pair.setLeft(pair.getLeft()+1);
                if (_case.getRemediedTimestamp() != null) {
                    pair.setRight(pair.getRight() + 1);
                }
            }
            AccessCertificationCasesStatisticsType retval = new AccessCertificationCasesStatisticsType(prismContext);
            for (Map.Entry<AccessCertificationResponseType,MutablePair<Integer,Integer>> statMapEntry : statMap.entrySet()) {
                AccessCertificationCasesStatisticsEntryType entry = new AccessCertificationCasesStatisticsEntryType(prismContext);
                entry.setResponse(statMapEntry.getKey());
                entry.setMarkedAsThis(statMapEntry.getValue().getLeft());
                entry.setMarkedAsThisAndResolved(statMapEntry.getValue().getRight());
                retval.getEntry().add(entry);
            }
            return retval;
        } catch (RuntimeException e) {
            result.recordFatalError("Couldn't get campaign statistics: unexpected exception: " + e.getMessage(), e);
            throw e;
        } finally {
            result.computeStatusIfUnknown();
        }
    }


}