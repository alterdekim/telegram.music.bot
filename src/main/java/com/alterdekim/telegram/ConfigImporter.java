package com.alterdekim.telegram;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

public class ConfigImporter {
    public static Config load( File file ) throws Exception {
        Serializer serializer = new Persister();
        return serializer.read(Config.class, file);
    }
}