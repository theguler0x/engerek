package com.evolveum.midpoint.web.page.admin.users.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class OrgsDto implements Serializable {

    public static enum SearchType {

        NAME("SearchType.NAME"),
        DISPLAY_NAME("SearchType.DISPLAY_NAME");

        private String key;

        private SearchType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static final String F_TEXT = "text";
    public static final String F_TYPE = "type";

    private String text;
    private Collection<SearchType> type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Collection<SearchType> getType() {
        if (type == null) {
            type = new ArrayList<SearchType>();
            type.add(SearchType.NAME);
        }
        return type;
    }

    public void setType(Collection type) {
        this.type = type;
    }

    public boolean hasType(SearchType type) {
        if (getType().contains(type)) {
            return true;
        }
        return false;
    }
}