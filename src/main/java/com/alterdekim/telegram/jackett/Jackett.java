package com.alterdekim.telegram.jackett;

import com.alterdekim.telegram.musicbot.Runner;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Jackett {
    public static ArrayList<TrackerItem> search(String query , int pos ) throws Exception {
        ArrayList<TrackerItem> items = new ArrayList<>();
        for( String cat : Runner.config.getJackettConfig().getTrackers().get(pos).getCats() ) {
            try {
                String url = Runner.config.getJackettConfig().getTrackers().get(pos).getUrl(cat)
                        .replace("{REPLACE_IP}", Runner.config.getJackettConfig().getJackett_ip())
                        .replace("{REPLACE_PORT}", Runner.config.getJackettConfig().getJackett_port() + "")
                        .replace("{REPLACE_APIKEY}", Runner.config.getJackettConfig().getApi_key());
                String response = HTTP.get(url + URLEncoder.encode(query, StandardCharsets.UTF_8));
                TrackerRSS rss = RSSReaderUtil.parse(response);
                items.addAll(rss.getChannel().getItems());
            } catch ( Exception e ) {
                // Nothing...
            }
        }
        return items;
    }
}
