package com.kraftek.stac.core.model.extensions;

import com.kraftek.stac.core.model.Extensible;
import com.kraftek.stac.core.model.extensions.eo.EOExtension;
import com.kraftek.stac.core.model.extensions.projection.ProjExtension;
import com.kraftek.stac.core.model.extensions.view.ViewExtension;

/**
 * Factory class for creating extensions of a given type.
 *
 * @author Cosmin Cara
 */
public class ExtensionFactory {

    /**
     * Creates an extension for the given parent object
     * @param parent    The parent object
     * @param type      The type of extension
     * @param <E>       The Java type of the parent
     */
    public static <E extends Extensible> Extension<E> create(E parent, ExtensionType type) {
        Extension<E> extension = null;
        switch (type) {
            case EO:
                extension = new EOExtension<>(parent);
                break;
            case PROJ:
                extension = new ProjExtension<>(parent);
                break;
            case VIEW:
                extension = new ViewExtension<>(parent);
                break;
        }
        return extension;
    }

}
