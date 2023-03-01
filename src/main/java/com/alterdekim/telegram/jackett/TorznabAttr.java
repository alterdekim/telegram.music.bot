package com.alterdekim.telegram.jackett;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

@Element(name="torznab:attr")
public class TorznabAttr {

    @Attribute(name="name")
    private String name;

    @Attribute(name="value")
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
