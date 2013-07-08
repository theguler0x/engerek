/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.wf;

import com.evolveum.midpoint.model.AbstractInternalModelIntegrationTest;
import com.evolveum.midpoint.prism.PrismReference;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.RoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.SystemObjectsType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author mederly
 */
@ContextConfiguration(locations = {"classpath:ctx-workflow-test-main.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestInfrastructure extends AbstractInternalModelIntegrationTest {          // todo use weaker class (faster initialization)

    protected static final Trace LOGGER = TraceManager.getTrace(TestInfrastructure.class);

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private WfTaskUtil wfTaskUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void initSystem(Task initTask, OperationResult initResult)
            throws Exception {
        super.initSystem(initTask, initResult);
        repoAddObjectsFromFile(TestConstants.USERS_AND_ROLES_FILENAME, RoleType.class, initResult);
    }

    @Test(enabled = true)
    public void test010SetGetWfApprovedBy() throws Exception {

        Task task = taskManager.createTaskInstance();
        OperationResult result = new OperationResult("test010SetGetWfApprovedBy");

        task.setOwner(repositoryService.getObject(UserType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), result));
        taskManager.switchToBackground(task, result);

        wfTaskUtil.addApprovedBy(task, SystemObjectsType.USER_ADMINISTRATOR.value());
        wfTaskUtil.addApprovedBy(task, SystemObjectsType.USER_ADMINISTRATOR.value());
        wfTaskUtil.addApprovedBy(task, TestConstants.R1BOSS_OID);
        wfTaskUtil.addApprovedBy(task, TestConstants.R2BOSS_OID);
        task.savePendingModifications(result);

        Task task2 = taskManager.getTask(task.getOid(), result);
        PrismReference approvers = wfTaskUtil.getApprovedBy(task2);

        assertEquals("Incorrect number of approvers", 3, approvers.getValues().size());
        assertEquals("Incorrect approvers",
                new HashSet(Arrays.asList(SystemObjectsType.USER_ADMINISTRATOR.value(), TestConstants.R1BOSS_OID, TestConstants.R2BOSS_OID)),
                new HashSet(Arrays.asList(approvers.getValue(0).getOid(), approvers.getValue(1).getOid(), approvers.getValue(2).getOid())));
    }
}