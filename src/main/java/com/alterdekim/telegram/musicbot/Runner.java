package com.alterdekim.telegram.musicbot;

import com.alterdekim.telegram.Config;
import com.alterdekim.telegram.ConfigImporter;

import java.io.File;

public class Runner {

    private static final String config_filename = "config.xml";
    public static Config config;

    public static void main(String[] args) throws Exception {
        if( loadConfig() ) { return; }
        new MusicBot().start();
    }

    private static boolean loadConfig() throws Exception {
        if( new File(config_filename).exists() ) {
            Runner.config = ConfigImporter.load(new File(config_filename));
        } else {
            Runner.config = new Config();
            Runner.config.toXML(config_filename);
            return true;
        }
        return false;
    }
}