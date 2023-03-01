package com.alterdekim.telegram.jackett;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name="channel", strict = false)
public class TrackerChannel {

    @Element(name="title")
    private String title;

    @ElementList( entry="item", inline = true )
    private ArrayList<TrackerItem> items;

    public String getTitle() {
        return this.title;
    }

    public ArrayList<TrackerItem> getItems() {
        return items;
    }
}
