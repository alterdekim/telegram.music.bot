package com.alterdekim.telegram.jackett;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class RSSReaderUtil {
    public static TrackerRSS parse( String response ) throws Exception {
        Serializer serializer = new Persister();
        return serializer.read(TrackerRSS.class, response);
    }
}