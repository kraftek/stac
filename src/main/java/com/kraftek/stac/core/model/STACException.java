package com.kraftek.stac.core.model;

public class STACException extends Exception {
    protected final String code;
    protected final String description;

    public STACException(String code, String description) {
        super(description);
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
