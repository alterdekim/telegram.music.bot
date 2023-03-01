package com.alterdekim.telegram;

import com.alterdekim.telegram.bittorrent.BitTorrentConfig;
import com.alterdekim.telegram.jackett.JackettConfig;
import com.alterdekim.telegram.xml.CommentVisitor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;

import java.io.File;

@Root(name="config")
public class Config {

    @Element(name="telegram_bot_token")
    private String telegram_token = "TOKEN";

    @Element(name="telegram_admin_login")
    private String telegram_master_login = "";

    @Element(name="jackett_config")
    private JackettConfig jackettConfig = new JackettConfig();

    @Element(name="bittorrent_config")
    private BitTorrentConfig bitTorrentConfig = new BitTorrentConfig();

    public String getTelegram_token() {
        return telegram_token;
    }

    public String getTelegram_master_login() {
        return telegram_master_login;
    }

    public JackettConfig getJackettConfig() {
        return jackettConfig;
    }

    public BitTorrentConfig getBitTorrentConfig() {
        return bitTorrentConfig;
    }

    public void toXML(String filename ) throws Exception {
        Visitor visitor = new CommentVisitor();
        Strategy strategy = new VisitorStrategy(visitor);
        Persister persister = new Persister(strategy);
        File result = new File(filename);
        persister.write(this, result);
    }
}
