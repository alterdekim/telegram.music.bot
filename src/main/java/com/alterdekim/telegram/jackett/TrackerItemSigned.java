package com.alterdekim.telegram.jackett;

import java.util.ArrayList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class TrackerItemSigned {

    private boolean i;

    private String title;

    private String size;

    private String link;

    private ArrayList<TorznabAttr> torznabAttrArrayList = new ArrayList<TorznabAttr>();

    public void setIsMagnet( boolean i ) {
        this.i = i;
    }

    public boolean getIsMagnet() {
        return this.i;
    }

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

    public void addSeeders( String s ) {
        this.title += " <b>Seeders: " + s+"</b>";
    }

    private TrackerItemSigned(boolean i, String title, String size, String link, ArrayList<TorznabAttr> attrs ) {
        this.i = i;
        this.title = escapeHtml4(title);
        this.size = size;
        this.link = link;
        this.torznabAttrArrayList = attrs;
    }

    public static TrackerItemSigned parse( TrackerItem item, boolean i ) {
        return new TrackerItemSigned(i, item.getTitle(), item.getSize(), item.getLink(), item.getTorznabAttrArrayList());
    }
}
