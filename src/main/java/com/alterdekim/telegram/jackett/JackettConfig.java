package com.alterdekim.telegram.jackett;

import com.alterdekim.telegram.xml.Comment;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name="jackett_config")
public class JackettConfig {

    @Element(name="jackett_server_ip")
    private String jackett_ip = "127.0.0.1";

    @Element(name="jackett_server_port")
    private Integer jackett_port = 9117;

    @Element(name="jackett_api_key")
    private String api_key = "xxx";

    @Comment(value = "store here jackett rss links like that: http://{REPLACE_IP}:9117/.../api?apikey=xxx&amp;t=search&amp;cat=3000&amp;q=")
    @ElementList(name="jackett_rss_trackers")
    private ArrayList<JackettTracker> trackers = JackettTracker.fromString("thepiratebay", "3000,3010", true);

    public String getJackett_ip() {
        return jackett_ip;
    }

    public ArrayList<JackettTracker> getTrackers() {
        return trackers;
    }

    public Integer getJackett_port() {
        return jackett_port;
    }

    public String getApi_key() {
        return api_key;
    }
}
