package com.alterdekim.telegram.bittorrent;

import bt.runtime.BtClient;

public class BtClientAndUUID {
    private BtClient btClient;
    private String uuid;

    public BtClient getBtClient() {
        return btClient;
    }

    public void setBtClient(BtClient btClient) {
        this.btClient = btClient;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BtClientAndUUID(BtClient btClient, String uuid) {
        this.btClient = btClient;
        this.uuid = uuid;
    }

    public BtClientAndUUID() {}
}
