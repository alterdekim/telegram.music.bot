package com.alterdekim.telegram.jackett;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name="item", strict = false)
public class TrackerItem {

    @Element(name="title")
    private String title;

    @Element(name="size")
    private String size;

    @Element(name="link")
    private String link;

    @ElementList(entry = "attr", inline = true )
    private ArrayList<TorznabAttr> torznabAttrArrayList = new ArrayList<TorznabAttr>();

    public String getTitle() {
        return title;
    }

    public String getSize() {
        return size;
    }

    public String getLink() {
        return link;
    }

    public ArrayList<TorznabAttr> getTorznabAttrArrayList() {
        return torznabAttrArrayList;
    }
}
