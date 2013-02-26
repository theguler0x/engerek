package com.evolveum.midpoint.web.page.admin.configuration.dto;

import com.evolveum.midpoint.web.component.util.Selectable;

/**
 * @author lazyman
 */
public class DebugObjectItem extends Selectable {

    public static final String F_OID = "oid";
    public static final String F_NAME = "name";
    public static final String F_RESOURCE_NAME = "resourceName";
    public static final String F_RESOURCE_TYPE = "resourceType";

    private String oid;
    private String name;

    private String resourceName;
    private String resourceType;

    public DebugObjectItem(String oid, String name) {
        this.name = name;
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public String getOid() {
        return oid;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
