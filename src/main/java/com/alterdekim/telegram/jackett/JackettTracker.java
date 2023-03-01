package com.alterdekim.telegram.jackett;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name="jackett_tracker")
public class JackettTracker {
    @Element(name="tracker_name")
    private String tracker_name = "";

    @Element(name="cats")
    private String cats = "3000";

    @Element(name="is_magnet")
    private Boolean isMagnet = false;

    public String getTrackerName() {
        return tracker_name;
    }

    public Boolean getMagnet() {
        return isMagnet;
    }

    JackettTracker() {

    }

    public String getUrl( String cat ) {
        return "http://{REPLACE_IP}:{REPLACE_PORT}/api/v2.0/indexers/"+tracker_name+"/results/torznab/api?apikey={REPLACE_APIKEY}&t=search&cat="+cat+"&q=";
    }

    public String[] getCats() {
        return cats.split(",");
    }

    private JackettTracker( String tracker_name, String cats, Boolean isMagnet ) {
        this.tracker_name = tracker_name;
        this.cats = cats;
        this.isMagnet = isMagnet;
    }

    public static ArrayList<JackettTracker> fromString(String tracker_name, String cats, Boolean isMagnet ) {
        ArrayList<JackettTracker> arr = new ArrayList<>();
        arr.add(new JackettTracker(tracker_name, cats, isMagnet));
        return arr;
    }
}
