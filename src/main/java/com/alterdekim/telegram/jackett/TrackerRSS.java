package com.alterdekim.telegram.jackett;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="rss", strict = false)
public class TrackerRSS {

    @Element(name="channel")
    private TrackerChannel channel;

    public TrackerChannel getChannel() {
        return channel;
    }
}
