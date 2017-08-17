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

package com.evolveum.midpoint.notifications.impl.notifiers;

import com.evolveum.midpoint.model.api.context.ModelContext;
import com.evolveum.midpoint.model.api.context.ModelElementContext;
import com.evolveum.midpoint.notifications.api.events.Event;
import com.evolveum.midpoint.notifications.api.events.ModelEvent;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * @author mederly
 */
@Component
public class SimpleFocalObjectNotifier extends GeneralNotifier {

    private static final Trace LOGGER = TraceManager.getTrace(SimpleFocalObjectNotifier.class);

    @PostConstruct
    public void init() {
        register(SimpleFocalObjectNotifierType.class);
    }

    @Override
    protected boolean quickCheckApplicability(Event event, GeneralNotifierType generalNotifierType, OperationResult result) {
        if (!(event instanceof ModelEvent)) {
            LOGGER.trace("{} is not applicable for this kind of event, continuing in the handler chain; event class = {}", getClass().getSimpleName(), event.getClass());
            return false;
        }
        ModelEvent modelEvent = (ModelEvent) event;
        if (modelEvent.getFocusContext() == null || !FocusType.class.isAssignableFrom(modelEvent.getFocusContext().getObjectTypeClass())) {
            LOGGER.trace("{} is not applicable to non-focus related model operations, continuing in the handler chain", getClass().getSimpleName());
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkApplicability(Event event, GeneralNotifierType generalNotifierType, OperationResult result) {
        List<ObjectDelta<FocusType>> deltas = ((ModelEvent) event).getFocusDeltas();
        if (deltas.isEmpty()) {
            return false;
        }

        if (isWatchAuxiliaryAttributes(generalNotifierType)) {
            return true;
        }

        for (ObjectDelta<FocusType> delta : deltas) {
            if (!delta.isModify() || deltaContainsOtherPathsThan(delta, functions.getAuxiliaryPaths())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected String getSubject(Event event, GeneralNotifierType generalNotifierType, String transport, Task task, OperationResult result) {

		final ModelEvent modelEvent = (ModelEvent) event;
        String typeName = modelEvent.getFocusTypeName();

        if (event.isAdd()) {
            return typeName + " oluşturma bildirimi";
        } else if (event.isModify()) {
            return typeName + " değişiklik bildirimi";
        } else if (event.isDelete()) {
            return typeName + " silme bildirimi";
        } else {
            return "(bilinmeyen " + typeName.toLowerCase() + " işlemi)";
        }
    }

    @Override
    protected String getBody(Event event, GeneralNotifierType generalNotifierType, String transport, Task task, OperationResult result) throws SchemaException {

		final ModelEvent modelEvent = (ModelEvent) event;

		String typeName = modelEvent.getFocusTypeName();
        String typeNameLower = typeName.toLowerCase();

        boolean techInfo = Boolean.TRUE.equals(generalNotifierType.isShowTechnicalInformation());

		ModelContext<FocusType> modelContext = (ModelContext) modelEvent.getModelContext();
        ModelElementContext<FocusType> focusContext = modelContext.getFocusContext();
        PrismObject<FocusType> focus = focusContext.getObjectNew() != null ? focusContext.getObjectNew() : focusContext.getObjectOld();
        FocusType userType = focus.asObjectable();
        String oid = focusContext.getOid();

        String fullName;
        if (userType instanceof UserType) {
            fullName = PolyString.getOrig(((UserType) userType).getFullName());
        } else if (userType instanceof AbstractRoleType) {
            fullName = PolyString.getOrig(((AbstractRoleType) userType).getDisplayName());
        } else {
            fullName = "";          // TODO (currently it's not possible to get here)
        }

        if (fullName == null) {
            fullName = "";          // "null" is not nice in notifications
        }

        ObjectDelta<FocusType> delta = ObjectDelta.summarize(modelEvent.getFocusDeltas());

        StringBuilder body = new StringBuilder();

        String status;
        if (event.isSuccess()) {
            status = "BAŞARILI";
        } else if (event.isOnlyFailure()) {
            status = "BAŞARISIZ";
        } else if (event.isFailure()) {
            status = "KISMEN BAŞARILI";
        } else if (event.isInProgress()) {
            status = "İLERLEME HALİNDE";
        } else {
            status = "BİLİNMİYOR";
        }

        String attemptedTo = event.isSuccess() ? "" : "(attempted to be) ";
        body.append(typeNameLower).append("-ilgili işlemin bildirimi (durum: " + status + ")\n\n") ;
        body.append(typeName).append(": " + fullName + " (" + userType.getName() + ", oid " + oid + ")\n");
        body.append("Bildirim oluşturulma tarihi: " + new Date() + "\n\n");

        List<ItemPath> hiddenPaths = isWatchAuxiliaryAttributes(generalNotifierType) ? new ArrayList<ItemPath>() : auxiliaryPaths;
        if (delta.isAdd()) {
		    body.append(typeNameLower).append(" kaydı " + attemptedTo + "aşağıdaki verilerle oluşturuldu:\n");
            body.append(textFormatter.formatObject(delta.getObjectToAdd(), hiddenPaths, isWatchAuxiliaryAttributes(generalNotifierType)));
            body.append("\n");
        } else if (delta.isModify()) {
		    body.append(typeNameLower).append(" kaydı" + attemptedTo + "değiştirildi. Değiştirilen öznitelikler:\n");
            body.append(textFormatter.formatObjectModificationDelta(delta, hiddenPaths, isWatchAuxiliaryAttributes(generalNotifierType), focusContext.getObjectOld(), focusContext.getObjectNew()));
            body.append("\n");
        } else if (delta.isDelete()) {
		    body.append(typeNameLower).append(" kaydı " + attemptedTo + "silindi.\n\n");
        }
		body.append("\n");

        if (!event.isSuccess()) {
		    body.append("Yapılan istemin durumu hakkında daha fazla bilgisi gösterilmiştir ve/veya log dosyalarında mevcuttur.\n\n");
        }

        if (event.getRequester() != null) {
        	body.append("Requester: ");
        	try {
        		ObjectType requester = event.getRequester().resolveObjectType(result);
        		if (requester instanceof UserType) {
        			UserType requesterUser = (UserType) requester;
        			body.append(requesterUser.getFullName()).append(" (").append(requester.getName()).append(")");
        		} else {
        			body.append(ObjectTypeUtil.toShortString(requester));
        		}
        	} catch (RuntimeException e) {
        		body.append("couldn't be determined: ").append(e.getMessage());
        		LoggingUtils.logUnexpectedException(LOGGER, "Couldn't determine requester for a notification", e);
        	}
        	body.append("\n");
        }
        body.append("Channel: ").append(modelContext.getChannel()).append("\n\n");
        


        functions.addRequesterAndChannelInformation(body, event, result);


        if (techInfo) {
            body.append("----------------------------------------\n");
            body.append("Teknik bilgi:\n\n");
            body.append(modelContext.debugDump(2));
        }

        return body.toString();
    }

	@Override
    protected Trace getLogger() {
        return LOGGER;
    }

}
