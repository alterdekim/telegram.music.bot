package com.alterdekim.telegram.bittorrent;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="bittorrent_config")
public class BitTorrentConfig {
    @Element(name="download_path")
    private String download_path = "data/torrent";

    public String getDownload_path() {
        return download_path;
    }
}