package com.alterdekim.telegram.bittorrent;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.runtime.Config;
import bt.torrent.TorrentSessionState;
import com.alterdekim.telegram.musicbot.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Consumer;

public class BitTorrentClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitTorrentClient.class);

    public static BtClientAndUUID download( final String name, String magnet, String dl, boolean isMagnet, TorrentHandler handler ) throws Exception {

        LOGGER.info("Requested download: " + name + "\r\nWith link: " + (isMagnet ? magnet : dl));

        Config config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors() * 2;
            }
        };

        DHTModule dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });

        String path_uuid = UUID.randomUUID().toString();

        Path targetDirectory = Paths.get(Runner.config.getBitTorrentConfig().getDownload_path() + "/" + path_uuid);

        if( !targetDirectory.toFile().mkdirs() ) {
            LOGGER.error("Directories had not been created.");
            return null;
        }

        Storage storage = new FileSystemStorage(targetDirectory);
        BtClient client;
        if( !isMagnet ) {
            client = Bt.client()
                    .config(config)
                    .storage(storage)
                    .torrent(new URL(dl))
                    .autoLoadModules()
                    .module(dhtModule)
                    .stopWhenDownloaded()
                    .build();
        } else {
            client = Bt.client()
                    .config(config)
                    .storage(storage)
                    .magnet(magnet)
                    .autoLoadModules()
                    .module(dhtModule)
                    .stopWhenDownloaded()
                    .build();
        }

        client.startAsync(new Consumer<TorrentSessionState>() {
            @Override
            public void accept(TorrentSessionState torrentSessionState) {
                if(torrentSessionState.getPiecesRemaining() == 0) {
                    handler.handleDownloaded(name);
                    client.stop();
                }
                double percent = (((double) torrentSessionState.getPiecesComplete()) / ((double) torrentSessionState.getPiecesTotal())) * 100d;
                handler.handleDownloadedPercent(percent, torrentSessionState.getConnectedPeers().size());
            }
        }, 10000);

        return new BtClientAndUUID(client, path_uuid);
    }
}
