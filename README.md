
# Telegram Music Torrents Bot

Create and manage your torrent music library through Telegram.

![Bot usage screenshot](https://github.com/alterdekim/telegram.music.bot/blob/main/example1.gif?raw=true)

# Installation

## Requirements

* [Jackett](https://github.com/Jackett/Jackett) - torrent tracker indexer.

* **Java** and **Maven**.

* [Empty Telegram bot](https://core.telegram.org/bots/tutorial) - click for tutorial.

# Configuring

After first run of compiled jar, you'll see generated config.xml file.

## Default config.xml

~~~ xml
<config>
   <telegram_bot_token>xxx</telegram_bot_token>
   <telegram_admin_login></telegram_admin_login>
   <jackett_config>
      <jackett_server_ip>127.0.0.1</jackett_server_ip>
      <jackett_server_port>9117</jackett_server_port>
      <jackett_api_key>apikey</jackett_api_key>
      <!-- store here jackett rss links like that: http://{REPLACE_IP}:9117/.../api?apikey=xxx&amp;t=search&amp;cat=3000&amp;q= -->
      <jackett_rss_trackers>
         <jackett_tracker>
            <tracker_name>thepiratebay</tracker_name>
            <cats>3000</cats>
            <is_magnet>true</is_magnet>
         </jackett_tracker>
      </jackett_rss_trackers>
   </jackett_config>
   <bittorrent_config>
      <download_path>data/torrent</download_path>
   </bittorrent_config>
</config>
~~~

### Telegram configuration

Element name  | Description
------------- | -------------
telegram_bot_token  | token of your telegram bot.
telegram_admin_login  | login of user, who will use this bot.

### Jackett configuration

All Jackett configuration stored inside _<jacket_config>_ element.

Element name  | Description
------------- | -------------
jackett_server_ip  | IPv4 address of your jackett server.
jackett_server_port  | Jackett server port.
jackett_api_key  | Your Jackett server api key.

Configuration of trackers, which will be indexed by Jackett.

Element name  | Description
------------- | -------------
tracker_name  | Torrent tracker name, copied from RSS Feed link.
cats  | Tracker categories, which made for music ( default 3000 ).
is_magnet  | Set `true` if tracker provides magnet links, `false` if not.

### BitTorrent configuration

This bot downloads torrents via bt-library. Here is configuration for downloading. ( stored inside _<bittorrent_config>_ element )

Element name  | Description
------------- | -------------
download_path  | Directory, where downloaded torrents will be stored and indexed. Can be relative or absolute.

# Commands

Here are commands, what you'll use for interact with bot.

Command name  | Description
------------- | -------------
`/help`  | List of all available commands.
`/searchtor query` | Search available torrents.
`/stoptor` | Stop downloading torrent.
`/download N` | Start downloading torrent.
`/folders` | List of all folders inside `download_path`.
`/songs` | List of all songs.
`/albums` | List of your albums.
`/artists` | List of artists.
`/list N` | Command, used for get list of something ( e.g. songs in album ).
`/search query` | Search in your library for songs, that contains `query` in name.
`/album query` | Search in your library for albums, that contains `query` in name.
`/artist query` | Search in your library for artists, that contains `query` in name.
`/year query` | Search in your library for songs, that created in certain year.
`/play N` | Get Nth audio file from list.
`/playall` | Get all audio files from list.
`esc` | Escape from state

# License

[MIT License](https://choosealicense.com/licenses/mit/)
