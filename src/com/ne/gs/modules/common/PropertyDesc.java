package com.ne.gs.modules.common;

import com.ne.commons.utils.TypeUtil;
import com.ne.gs.modules.common.CustomLocTemplate;

/**
 * @author hex1r0
 */
public class PropertyDesc<T> {
    private final String _name;
    private final Class<T> _type;

    public PropertyDesc(String name, Class<T> type) {
        _name = name;
        _type = type;
    }

    public String getName() {
        return _name;
    }

    public Class<T> getType() {
        return _type;
    }

    public static <T> T of(CustomLocTemplate template, PropertyDesc<T> propertyDesc) {
        return TypeUtil.valueOf(
            template.getPropertyList().getValue(propertyDesc._name),
            propertyDesc._type);
    }

    public static <T> T of(CustomLocTemplate template, PropertyDesc<T> propertyDesc, String default0) {
        return TypeUtil.valueOf(
            template.getPropertyList().getValue(propertyDesc._name),
            propertyDesc._type, default0);
    }
}
