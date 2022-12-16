package com.kraftek.stac.core.model.extensions;

import com.kraftek.stac.core.model.extensions.eo.EOExtension;
import com.kraftek.stac.core.model.extensions.eo.EoFields;
import com.kraftek.stac.core.model.extensions.projection.ProjExtension;
import com.kraftek.stac.core.model.extensions.projection.ProjFields;
import com.kraftek.stac.core.model.extensions.view.ViewExtension;
import com.kraftek.stac.core.model.extensions.view.ViewFields;

/**
 * Enumeration of the supported extension types.
 *
 * @author Cosmin Cara
 */
public enum ExtensionType {
    EO(EOExtension.class, EoFields.PREFIX,
       "https://stac-extensions.github.io/eo/v1.0.0/schema.json"),
    PROJ(ProjExtension.class,
         ProjFields.PREFIX,
         "https://stac-extensions.github.io/projection/v1.0.0/schema.json"),
    VIEW(ViewExtension.class,
         ViewFields.PREFIX,
         "https://stac-extensions.github.io/view/v1.0.0/schema.json");

    private final String value;
    private final String description;
    private final Class<? extends Extension> extClass;

    ExtensionType(Class<? extends Extension> clazz, String value, String description) {
        this.value = value;
        this.description = description;
        this.extClass = clazz;
    }

    public String friendlyName() { return this.description; }

    public String value() { return this.value; }

    public static ExtensionType fromURI(String uri) {
        for (ExtensionType type : ExtensionType.values()) {
            if (type.description.equals(uri)) {
                return type;
            }
        }
        return null;
    }
}
