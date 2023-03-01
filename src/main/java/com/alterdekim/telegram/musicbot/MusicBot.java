package com.alterdekim.telegram.musicbot;

import com.alterdekim.telegram.bittorrent.BitTorrentClient;
import com.alterdekim.telegram.bittorrent.BtClientAndUUID;
import com.alterdekim.telegram.bittorrent.TorrentHandler;
import com.alterdekim.telegram.jackett.*;
import com.alterdekim.telegram.storage.FileAndTag;
import com.alterdekim.telegram.storage.FileSearcher;
import com.alterdekim.telegram.storage.TreeNode;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.alterdekim.telegram.musicbot.Commands.*;

public class MusicBot {

    private enum BotStatus {
        NONE,
        SEARCHTORRES,
        SEARCHRES,
        GOTLIST,
        GOTFOLDERS,
        GOTSONGS,
        GOTALBUMS,
        GOTARTISTS
    }

    private interface AvailableCommands {
        String[] array = new String[] {
                searchtor+"\r\n"+folders+"\r\n"+songs+"\r\n"+search+"\r\n"+searchyear+"\r\n"+searchartist+"\r\n"+searchalbum+"\r\n"+stoptor+"\r\n"+albums+"\r\n"+artists+"\r\n",
                download+"\r\n"+stoptor+"\r\n",
                play+"\r\n"+playall+"\r\n",
                play+"\r\n"+playall+"\r\n",
                list+"\r\n",
                play+"\r\n",
                list+"\r\n",
                list+"\r\n"
        };
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicBot.class);

    private TelegramBot bot;
    private String tg_name;

    private BotStatus status = BotStatus.NONE;

    private ArrayList<TrackerItemSigned> trackerItems = new ArrayList<TrackerItemSigned>();

    private int rnd;

    private ArrayList<BtClientAndUUID> btClients = new ArrayList<BtClientAndUUID>();

    private TreeNode<File> fileTree = null;
    private ArrayList<FileAndTag> tags = null;

    private ArrayList<File> resultSongs = new ArrayList<File>();

    private void indexTags() {
        ArrayList<File> files = fileTree.getEndPoints();
        tags = new ArrayList<FileAndTag>();
        for( File f : files ) {
            try {
                AudioFile af = AudioFileIO.read(f);
                Tag tag = af.getTag();
                tags.add(new FileAndTag(tag, f));
            } catch ( Exception e ) {
                LOGGER.error("Error while parsing metadata of " + f.getAbsolutePath());
            }
        }
    }

    public void start() throws Exception {
        new File(Runner.config.getBitTorrentConfig().getDownload_path()).mkdirs();
        bot = new TelegramBot(Runner.config.getTelegram_token());
        tg_name = Runner.config.getTelegram_master_login();
        fileTree = FileSearcher.getTree(new TreeNode<File>(new File(Runner.config.getBitTorrentConfig().getDownload_path())));
        indexTags();
        bot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> list) {
                for (Update u : list) {
                    try {
                        if (!u.message().from().username().equals(tg_name)) continue;
                        if( u.message().text().equals("esc") ) {
                            status = BotStatus.NONE;
                            continue;
                        }
                        if( u.message().text().equals(help) ) {
                            showAvailableCommands(u);
                            continue;
                        }
                        switch( status ) {
                            case NONE:
                                processNone( u, u.message().text() );
                                break;
                            case SEARCHTORRES:
                                processSearchTorResult( u, u.message().text() );
                                break;
                            case SEARCHRES:
                                processSearchResult( u, u.message().text(), true );
                                break;
                            case GOTLIST:
                                processList( u, u.message().text() );
                                break;
                            case GOTFOLDERS:
                                processGotFolders( u, u.message().text() );
                                break;
                            case GOTSONGS:
                                processGotSongs( u, u.message().text() );
                                break;
                            case GOTALBUMS:
                                processGotAlbums( u, u.message().text() );
                                break;
                            case GOTARTISTS:
                                processGotArtists( u, u.message().text() );
                                break;
                            default:
                                bot.execute(new SendMessage(u.message().chat().id(), "Try to send other command. (case=default)"));
                                break;
                        }
                    } catch ( Exception e ) {
                        // Nothing....
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }

    private void processGotSongs(Update u, String text) {
        processSearchResult(u, text, false);
    }

    private void processList(Update u, String text) {
        processSearchResult(u, text, true);
    }

    private void showAvailableCommands( Update u ) {
        bot.execute(new SendMessage(u.message().chat().id(), AvailableCommands.array[status.ordinal()]+"type <b>esc</b> to escape.").parseMode(ParseMode.HTML));
    }

    private void processGotArtists( Update u, String text ) {
        if( text.startsWith(list) ) {
            try {
                int index = Integer.parseInt(text.substring(list.length()));
                LinkedHashSet<String> artists = new LinkedHashSet<String>();
                for (FileAndTag tag : tags) {
                    artists.addAll(tag.getTag().getAll(FieldKey.ARTISTS));
                    artists.addAll(tag.getTag().getAll(FieldKey.ARTIST));
                }
                List<String> al_list = artists.stream().sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                }).collect(Collectors.toList());
                if( index < 0 || index >= al_list.size() ) { throw new Exception(); }
                String art = al_list.get(index);
                ArrayList<File> songs = new ArrayList<File>();
                for( FileAndTag t : tags ) {
                    if( t.getTag().getFields(FieldKey.ARTIST).stream().anyMatch(n -> n.toString().contains(art)) ||
                            t.getTag().getFields(FieldKey.ARTISTS).stream().anyMatch(n -> n.toString().contains(art)) ) {
                        songs.add(t.getFile());
                    }
                }
                resultSongs = songs;
                ArrayList<String> results = new ArrayList<String>();
                results.add("All songs of: "+art+" \r\n");
                String result = "";
                for( int i = 0; i < songs.size(); i++ ) {
                    result += "#" + i + " " + songs.get(i).getName() + "\r\n";
                    if( result.length() > 400 ) {
                        results.add(result);
                        result = "";
                    }
                }
                if( !result.equals("") ) {
                    results.add(result);
                }
                for( String i : results ) {
                    bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
                }
                status = BotStatus.GOTSONGS;
            } catch ( Exception e ) {
                bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
                return;
            }
        }
        showAvailableCommands(u);
    }

    private void processSearchTorResult(Update u, String text) {
        if( text.startsWith(Commands.download) ) {
            int index = -1;
            try {
                index = Integer.parseInt(text.substring(Commands.download.length()));
            } catch ( Exception e ) {
                bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
                return;
            }
            try {
                Integer msgId = bot.execute(new SendMessage(u.message().chat().id(), "Downloading has been started...")).message().messageId();
                int n = btClients.size();
                btClients.add(BitTorrentClient.download(trackerItems.get(index).getTitle(), trackerItems.get(index).getLink(), trackerItems.get(index).getLink(), trackerItems.get(index).getIsMagnet(), new TorrentHandler() {
                    @Override
                    public void handleDownloaded(String name) {
                        bot.execute(new EditMessageText(u.message().chat().id(), msgId, "Downloaded successfully: " + name));
                        try {
                            fileTree = FileSearcher.getTree(new TreeNode<File>(new File(Runner.config.getBitTorrentConfig().getDownload_path())));
                            indexTags();
                        } catch ( Exception e ) {
                            LOGGER.error("Failed to index your storage.");
                        }
                    }

                    @Override
                    public void handleDownloadedPercent(double percent, int peers) {
                        rnd = (int) (Math.random() * 10000);
                        bot.execute(new EditMessageText(u.message().chat().id(), msgId, "#"+n+" Downloaded " + String.format(Locale.ENGLISH, "%(.2f", percent) + "% / 100%, with " + peers + " peers (#"+rnd+")"));
                    }
                }));
            } catch ( Exception e ) {
                // Nothing...
            }
        } else if( text.startsWith(Commands.stoptor) ) {
            performStopTorrent(u, text);
        }
    }

    private void performStopTorrent( Update u, String text ) {
        int index = -1;
        try {
            index = Integer.parseInt(text.substring(Commands.stoptor.length()));
            if( btClients.size() > index && index > -1 ) {
                btClients.get(index).getBtClient().stop();
                try {
                    FileUtils.deleteDirectory(new File(Runner.config.getBitTorrentConfig().getDownload_path() + "/" + btClients.get(index).getUuid()));
                } catch ( Exception e ) {
                    // HAHAH
                }
                bot.execute(new SendMessage(u.message().chat().id(), "Torrent #"+index+" has been stopped."));
            } else {
                bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
            }
        } catch ( Exception e ) {
            bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
            return;
        }
    }

    private void processNone(Update u, String text) {
        if( text.startsWith(searchtor) ) {
            bot.execute(new SendMessage(u.message().chat().id(), "Searching..."));
            ArrayList<TrackerItemSigned> items = new ArrayList<TrackerItemSigned>();
            for( int i = 0; i < Runner.config.getJackettConfig().getTrackers().size(); i++ ) {
                try {
                    ArrayList<TrackerItem> it = Jackett.search(text.substring(searchtor.length()), i);
                    ArrayList<TrackerItemSigned> its = new ArrayList<TrackerItemSigned>();
                    try {
                        for (TrackerItem trackerItem : it) {
                            TrackerItemSigned tis = TrackerItemSigned.parse(trackerItem, Runner.config.getJackettConfig().getTrackers().get(i).getMagnet());
                            boolean ss = false;
                            String seeders_c = "0";
                            for(TorznabAttr attr : tis.getTorznabAttrArrayList() ) {
                                if( attr.getName().equals("seeders") && ( attr.getValue().equals("") || attr.getValue().equals("0") ) ) {
                                    ss = true;
                                }
                                if( attr.getName().equals("seeders") ) {
                                    seeders_c = attr.getValue();
                                }
                            }
                            tis.addSeeders(seeders_c);
                            if( !ss ) {
                                its.add(tis);
                            }
                        }
                        items.addAll(its);
                    } catch (Exception e) {
                       e.printStackTrace();
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
            trackerItems = items;
            ArrayList<String> results = new ArrayList<String>();
            results.add("Found items: \r\n");
            String result = "";
            for( int i = 0; i < trackerItems.size(); i++ ) {
                result += "#" + i + " " + trackerItems.get(i).getTitle() + " <b>Size(MB): " + ((Long.parseLong(trackerItems.get(i).getSize()) / 1024) / 1024) + "</b>\r\n";
                if( result.length() > 400 ) {
                    results.add(result);
                    result = "";
                }
            }
            if( !result.equals("") ) {
                results.add(result);
            }
            for( String i : results ) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.SEARCHTORRES;
        } else if( text.startsWith(Commands.search) ) {
            ArrayList<File> songs = fileTree.getEndPoints();
            resultSongs = songs;
            String str = "Search results: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            for( int i = 0; i < songs.size(); i++ ) {
                if(StringUtils.containsIgnoreCase(songs.get(i).getName(), text.substring(Commands.search.length()))) {
                    str += "#" + i + " " + songs.get(i).getName() + "\r\n";
                    if (str.length() > 400) {
                        results.add(str);
                        str = "";
                    }
                }
            }
            if( !str.equals("") ) {
                results.add(str);
            }
            for( String i : results ) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.SEARCHRES;
        } else if( text.equals(Commands.folders) ) {
            ArrayList<File> songs = fileTree.getNonEndPoints(true);
            String str = "All your folders: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            for( int i = 0; i < songs.size(); i++ ) {
                str += "#" + i + " " + songs.get(i).getName() + "\r\n";
                if( str.length() > 400 ) {
                    results.add(str);
                    str = "";
                }
            }
            if( !str.equals("") ) {
                results.add(str);
            }
            for( String i : results ) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.GOTFOLDERS;
        } else if( text.equals(Commands.songs) ) {
            ArrayList<File> songs = fileTree.getEndPoints();
            resultSongs = songs;
            String str = "All your songs: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            for( int i = 0; i < songs.size(); i++ ) {
                str += "#" + i + " " + songs.get(i).getName() + "\r\n";
                if( str.length() > 400 ) {
                    results.add(str);
                    str = "";
                }
            }
            if( !str.equals("") ) {
                results.add(str);
            }
            for( String i : results ) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.GOTSONGS;
        } else if( text.startsWith(Commands.searchalbum) ) {
            String query = text.substring(searchalbum.length());
            String str = "Results: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            ArrayList<File> sr = new ArrayList<File>();
            for( int i = 0; i < tags.size(); i++ ) {
                List<String> arr = tags.get(i).getTag().getAll(FieldKey.ALBUM);
                for( String s : arr ) {
                    if (StringUtils.containsIgnoreCase(s, query)) {
                        sr.add(tags.get(i).getFile());
                        break;
                    }
                }
            }
            resultSongs = sr;
            for( int i = 0; i < sr.size(); i++ ) {
                str += "#" + i + " " + sr.get(i).getName() + "\r\n";
                if( str.length() > 400 ) {
                    results.add(str);
                    str = "";
                }
            }
            if( !str.equals("") ) {
                results.add(str);
            }
            for( String i : results ) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.SEARCHRES;
        } else if( text.startsWith(Commands.searchartist) ) {
            String query = text.substring(searchartist.length());
            String str = "Results: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            ArrayList<File> sr = new ArrayList<File>();
            for( int i = 0; i < tags.size(); i++ ) {
                List<String> arr = tags.get(i).getTag().getAll(FieldKey.ARTIST);
                for( String s : arr ) {
                    if (StringUtils.containsIgnoreCase(s, query)) {
                        sr.add(tags.get(i).getFile());
                        break;
                    }
                }
            }
            resultSongs = sr;
            for( int i = 0; i < sr.size(); i++ ) {
                str += "#" + i + " " + sr.get(i).getName() + "\r\n";
                if( str.length() > 400 ) {
                    results.add(str);
                    str = "";
                }
            }
            if( !str.equals("") ) {
                results.add(str);
            }
            for( String i : results ) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.SEARCHRES;

        } else if( text.startsWith(Commands.searchyear) ) {
            String query = text.substring(searchyear.length());
            String str = "Results: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            ArrayList<File> sr = new ArrayList<File>();
            for (int i = 0; i < tags.size(); i++) {
                List<String> arr = tags.get(i).getTag().getAll(FieldKey.YEAR);
                for (String s : arr) {
                    if (StringUtils.containsIgnoreCase(s, query)) {
                        sr.add(tags.get(i).getFile());
                        break;
                    }
                }
            }
            resultSongs = sr;
            for (int i = 0; i < sr.size(); i++) {
                str += "#" + i + " " + sr.get(i).getName() + "\r\n";
                if (str.length() > 400) {
                    results.add(str);
                    str = "";
                }
            }
            if (!str.equals("")) {
                results.add(str);
            }
            for (String i : results) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.SEARCHRES;
        } else if( text.equals(albums) ) {
            List<String> al_list = performAlbums(u, text);
            String result = "Albums: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            for (int i = 0; i < al_list.size(); i++) {
                result += "#" + i + " " + al_list.get(i) + "\r\n";
                if (result.length() > 400) {
                    results.add(result);
                    result = "";
                }
            }
            if (!result.equals("")) {
                results.add(result);
            }
            for (String i : results) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.GOTALBUMS;
        } else if( text.equals(artists) ) {
            LinkedHashSet<String> artists = new LinkedHashSet<String>();
            for (FileAndTag tag : tags) {
                artists.addAll(tag.getTag().getAll(FieldKey.ARTISTS));
                artists.addAll(tag.getTag().getAll(FieldKey.ARTIST));
            }
            List<String> al_list = artists.stream().sorted(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            }).collect(Collectors.toList());
            String result = "Artists: \r\n";
            ArrayList<String> results = new ArrayList<String>();
            for( int i = 0; i < al_list.size(); i++ ) {
                result += "#" + i + " " + al_list.get(i) + "\r\n";
                if (result.length() > 400) {
                    results.add(result);
                    result = "";
                }
            }
            if (!result.equals("")) {
                results.add(result);
            }
            for (String i : results) {
                bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
            }
            status = BotStatus.GOTARTISTS;
        } else if( text.startsWith(Commands.stoptor) ) {
            performStopTorrent(u, text);
        }
        showAvailableCommands(u);
    }

    private void processSearchResult(Update u, String text, boolean apa) {
        if( text.startsWith(Commands.play) ) {
            try {
                File f = resultSongs.get(Integer.parseInt(text.substring(Commands.play.length())));
                bot.execute(new SendAudio(u.message().chat().id(), f));
            } catch ( Exception e ) {
                bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
                return;
            }
        } else if( text.equals(Commands.playall) && apa ) {
            try {
                for (File resultSong : resultSongs) {
                    bot.execute(new SendAudio(u.message().chat().id(), resultSong));
                    Thread.sleep(300);
                }
            } catch ( Exception e ) {
                bot.execute(new SendMessage(u.message().chat().id(), "Something went wrong."));
                return;
            }
        }
    }

    private List<String> performAlbums( Update u, String text ) {
        LinkedHashSet<String> albums = new LinkedHashSet<String>();
        for (FileAndTag tag : tags) {
            albums.addAll(tag.getTag().getAll(FieldKey.ALBUM));
        }
        return albums.stream().sorted(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        }).collect(Collectors.toList());
    }

    private void performList( Update u, String text, Boolean isImaginary ) {
        if( !isImaginary ) {
            ArrayList<File> songs = fileTree.getNonEndPoints(true);
            int index = 0;
            try {
                index = Integer.parseInt(text.substring(Commands.list.length()));
                if( index < 0 || index >= songs.size() ) {
                    throw new Exception();
                }
                ArrayList<File> files = FileSearcher.getList(songs.get(index).getAbsolutePath());
                resultSongs = files;
                String str = "Songs in list: \r\n";
                ArrayList<String> results = new ArrayList<String>();
                for( int i = 0; i < files.size(); i++ ) {
                    str += "#" + i + " " + files.get(i).getName() + "\r\n";
                    if( str.length() > 400 ) {
                        results.add(str);
                        str = "";
                    }
                }
                resultSongs = files;
                if( !str.equals("") ) {
                    results.add(str);
                }
                for( String i : results ) {
                    bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
                }
                status = BotStatus.GOTLIST;
            } catch ( Exception e ) {
                bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
            }
        } else {
            List<String> al_list = performAlbums(u, text);
            try {
                int index = Integer.parseInt(text.substring(Commands.list.length()));
                if (index < 0 || index >= al_list.size()) {
                    throw new Exception();
                }

                ArrayList<File> files = new ArrayList<File>();
                String str = "Songs in album "+al_list.get(index)+": \r\n";
                ArrayList<String> results = new ArrayList<String>();
                int j = 0;
                for( int i = 0; i < tags.size(); i++ ) {
                    if( tags.get(i).getTag().getFields(FieldKey.ALBUM).stream().anyMatch(n -> n.toString().contains(al_list.get(index)))) {
                        files.add(tags.get(i).getFile());
                        str += "#" + j + " " + tags.get(i).getFile().getName() + "\r\n";
                        if( str.length() > 400 ) {
                            results.add(str);
                            str = "";
                        }
                        j++;
                    }
                }
                resultSongs = files;
                if( !str.equals("") ) {
                    results.add(str);
                }
                for( String i : results ) {
                    bot.execute(new SendMessage(u.message().chat().id(), i).parseMode(ParseMode.HTML));
                }
                status = BotStatus.GOTLIST;
            } catch ( Exception e ) {
                bot.execute(new SendMessage(u.message().chat().id(), "I can't understand this number."));
            }
        }
    }

    private void processGotFolders(Update u, String text) {
        if( text.startsWith(Commands.list) ) {
            performList(u, text, false);
        }
        showAvailableCommands(u);
    }

    private void processGotAlbums(Update u, String text) {
        if( text.startsWith(Commands.list) ) {
            performList(u, text, true);
        }
        showAvailableCommands(u);
    }
}
