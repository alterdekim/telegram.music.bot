package com.alterdekim.telegram.bittorrent;

public interface TorrentHandler {
    void handleDownloaded( String name );
    void handleDownloadedPercent( double percent, int peers );
}
