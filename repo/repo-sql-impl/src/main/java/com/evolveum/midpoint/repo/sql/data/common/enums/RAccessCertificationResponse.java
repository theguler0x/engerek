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

package com.evolveum.midpoint.repo.sql.data.common.enums;

import com.evolveum.midpoint.repo.sql.query.definition.JaxbType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationStatusType;

/**
 * @author mederly
 */
@JaxbType(type = AccessCertificationResponseType.class)
public enum RAccessCertificationResponse implements SchemaEnum<AccessCertificationResponseType> {

    ACCEPT(AccessCertificationResponseType.ACCEPT),

    REVOKE(AccessCertificationResponseType.REVOKE);

    /*REDUCE(AccessCertificationResponseType.REDUCE),

    NOT_DECIDED(AccessCertificationResponseType.NOT_DECIDED),

    DELEGATE(AccessCertificationResponseType.DELEGATE),

    NO_RESPONSE(AccessCertificationResponseType.NO_RESPONSE); */

    private AccessCertificationResponseType status;

    private RAccessCertificationResponse(AccessCertificationResponseType status) {
        this.status = status;
    }

    @Override
    public AccessCertificationResponseType getSchemaValue() {
        return status;
    }
}
