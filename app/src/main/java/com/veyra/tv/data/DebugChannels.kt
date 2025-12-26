package com.veyra.tv.data

object DebugChannels {
    val m3uContent = """
#EXTM3U

#EXTINF:-1 tvg-country="IN" group-title="Entertainment",Sony SAB HD (Source 1)
https://tv.bdixtv24.com/toffee/live.php?e=.m3u8&id=b7167fe646a4

#EXTINF:-1 tvg-country="IN" group-title="Entertainment",Sony SAB HD (Source 2)
https://nxtlive.net/sliv/stream.php?id=0897678986&e=.m3u8

#EXTINF:-1 tvg-country="IN" group-title="Entertainment",Sony MAX HD
http://221.120.204.4/SONY-MAX-LOCKLE/index.m3u8

#EXTINF:-1 tvg-country="IN" tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/NDTV_India.svg/1200px-NDTV_India.svg.png" tvg-country="IN" group-title="News",NDTV India (Live)
https://ndtvindia-live.akamaized.net/hls/live/2028377/ndtvindia/master.m3u8

#EXTINF:-1 tvg-country="IN" group-title="News",India Today (Live)
https://indiatodaylive.akamaized.net/hls/live/2014338/indiatoday/master.m3u8

#EXTINF:-1 group-title="Test",Big Buck Bunny (Test Stream)
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4

#EXTINF:-1 group-title="Test",Sintel (Test Stream)
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4
    """.trimIndent()
}