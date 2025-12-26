package com.veyra.tv.data

import com.veyra.tv.model.Playlist

object DefaultPlaylists {

    private val topPriorityUrls = listOf(
        "internal://debug", // Fastest local test
        "https://shyam-vadgama.github.io/iptv-json-parser/channels.json", // Fast JSON
        "https://raw.githubusercontent.com/FunctionError/PiratesTv/main/combined_playlist.m3u", // High chance of Sony channels
        "https://iptv-org.github.io/iptv/index.country.m3u",
        "https://iptv-org.github.io/iptv/index.language.m3u"
    )

    private val userRequestedUrls = listOf(
        "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8",
        "https://apsattv.com/ssungusa.m3u",
        "https://tvpass.org/playlist/m3u",
        "https://epghub.xyz/",
        "https://www.apsattv.com/xumo.m3u",
        "https://www.apsattv.com/localnow.m3u",
        "https://www.apsattv.com/lg.m3u",
        "https://www.apsattv.com/rok.m3u",
        "https://www.apsattv.com/redbox.m3u",
        "https://www.apsattv.com/distro.m3u",
        "https://www.apsattv.com/xiaomi.m3u",
        "https://www.apsattv.com/tablo.m3u",
        "https://www.apsattv.com/vizio.m3u"
    )

    // Raw content from the text file provided
    private const val rawTextFileContent = """
========================100+ FREE IPTV M3U LINKS BY TUTORIASL
GEEK=======================================
                                       11-01-2033
-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------
===================================================================================
=======================
http://platin4k.eu:80/get.php?username=eagleiptv&password=SsFAljPDDi&type=m3u_plus

http://almanya888.com:8080/get.php?
username=ufukucur&password=GAbhqErmuv&type=m3u_plus

http://iptvworld.nl:2095/get.php?
username=nlinenodric&password=4sR2SwYYeg&type=m3u_plus

http://4kiptv.pro:15000/get.php?
username=5UfQvAtjaR&password=AGbT2dW0xA&type=m3u_plus

http://top.streancdn.fun:80/get.php?
username=0987654321&password=1234567890&type=m3u

http://vipiptv101.com:8080/get.php?
username=Mustafa.Eken&password=bN29kLc5aS&type=m3u_plus

http://iptv41.com:8080/get.php?
username=utkubingol&password=CgvrbSmwxF&type=m3u_plus

http://iptvhogar.club:25461/get.php?
username=Orlando_Herrera&password=aQiWECxfFm&type=m3u_plus

http://iptvhogar.club:25461/get.php?
username=Yoana_Marin&password=4452053058&type=m3u_plus

http://vipiptv101.com:8080/get.php?username=tahsinsalihli&password=29102019-
thsin&type=m3u_plus

http://m3u.iptvott.live:8080/get.php?
username=iFnzn9zOnd&password=P4gsG7edtl&type=m3u

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://vipiptv101.com:8080/get.php?
username=ersoy.2508&password=seker.2022&type=m3u_plus

http://vipiptv101.com:8080/get.php?
username=gencosman&password=020919.cvf&type=m3u_plus

http://saw.duplex-ott.net:2052/get.php?
username=802462699227605&password=802446701881523&type=m3u_plus

http://saw.duplex-ott.net:2052/get.php?
username=802479429936404&password=802440660870517&type=m3u_plus

http://vipuhdteam.com:8080/get.php?
username=pimptv&password=3TL4ezQGmR&type=m3u_plus

http://bptv.me/get.php?username=1702725pau&password=486669&type=m3u
https://cdn.discordapp.com/attachments/739384288256196653/786290789428756510/
clip.tv.m3u

http://odenfull.co:2086/get.php?username=NATALY4334&password=20TORRE50s&type=m3u

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------
Visit Our Blog For More Updates www.sateliteguru.blogspot.com

http://iptvstream.es:8080/get.php?username=julen0003&password=zUHcdLH8om&type=m3u

http://topstb.com:8000/get.php?
username=50656250782258&password=27115840859269&type=m3u

http://formagapppppppppppppinturkey.ingiltereozel.com:8080/get.php?
username=doganbey&password=doganbey2021&type=m3u

http://cdn.miptv.ws:8880/get.php?username=yztbz&password=cojdzn&type=m3u

http://ologyconnect1081.com:25461/get.php?username=darwin&password=1234&type=m3u

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://ologyconnect1081.com:25461/get.php?username=jess&password=1234&type=m3u

http://ologyconnect1081.com:25461/get.php?username=lloyd&password=1234&type=m3u

http://n2086.securepoint.io:25461/get.php?
username=ZudaWKj4eP&password=dQbUQHUZxL&type=m3u

http://platin4k.eu:80/get.php?username=kralkerim&password=8TKLyJivVEA&type=m3u

https://m3u-editor.com:443/get.php?
username=kcik4t9z&password=wqwyg39s&type=m3u_plus

http://asterix-iptv.club:25461/get.php?
username=esoarene&password=Ct225SCUDn&type=m3u_plus

http://ck40.131221.net:8080//get.php?
username=USERRFYFU665&password=IyEwwRZV0A&type=m3u_plus
http://ck40.131221.net:8080//get.php?
username=USERFGFTSDTDG&password=GsfITKqhSQ&type=m3u_plus

http://4436c57651ca.iedem.com/playlists/uplist/8ef3adafc12b5a5afd38b58d3f2e1aa9/
playlist.m3u8

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://stream.rediptv.tk/get.php?username=AYNqWGV9Ep&password=jvJwqhrf2R&type=m3u

http://ck43.deskanet.com:8080/get.php?
username=Redchh4&password=MpoxtLo3sn&type=m3u_plus

http://ck43.deskanet.com:8080/get.php?
username=3xfpl6DKgH&password=15BRx4XWkS&type=m3u_plus
http://ipro.tv:80/get.php?username=mWYqJ61c4FI&password=eQbcjzmEIk&type=m3u

http://smart.nicehotone.xyz/get.php?
username=iptv011&password=9436589286&type=m3u_plus

http://pprotv.com:80/get.php?username=kalid&password=kalid123&type=m3u

http://gbox.goldeniptv.com:25461/get.php?
username=123321&password=123321&type=m3u_plus

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://fortv.cc:8080/get.php?username=jsc&password=jsc&type=m3u

http://red.ipfox.org:8080/get.php?username=Karam2022&password=Karam2023&type=m3u

http://168.205.87.198:8555/get.php?username=1431&password=123456&type=m3u

http://fortv.cc:8080/get.php?username=carls&password=1234&type=m3u_plus

http://red.ipfox.org:8080/get.php?username=Karam2022&password=Karam2023&type=m3u

http://gbox.goldeniptv.com:25461/get.php?username=123321&password=123321&type=m3u

http://red.ipfox.org:8080/get.php?username=Karam2022&password=Karam2023&type=m3u

http://m3u.iptvott.live:8080/get.php?
username=iFnzn9zOnd&password=P4gsG7edtl&type=m3u

http://chimeratv.live:25461/get.php?
username=amaxmovies88&password=tHubsYGxeH&type=m3u

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://168.205.87.198:8555/get.php?username=5803&password=123456&type=m3u

http://c.proserver.in:8080/get.php?username=vodstest&password=lw5p6ojDQq&type=m3u

http://c.proserver.in:8080/get.php?
username=vodstest&password=lw5p6ojDQq&type=m3u_plus

http://163.172.33.7:25461/get.php?
username=serveramsterdam&password=2292020&type=m3u

http://ccdn.so/get.php?username=vsabG&password=499AKVptc&type=m3u

http://misket.tv:2020/get.php?username=anka-icin-test&password=fhoPny6SRC&type=m3u

http://pablotv.us:8080/get.php?username=Abdo22&password=12341234&type=m3u_plus

http://cineapp.org:8000/get.php?username=Fernando&password=Fernando&type=m3u_plus

http://mains.services:2086/get.php?
username=A.Jones01&password=1904550&type=m3u_plus

http://tr.rchtv.com:8080/get.php?username=aslan11&password=aslan22&type=m3u_plus
http://cdn.globalserver.me:8080/get.php?username=399977&password=528931&type=m3u

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://168.205.87.198:8555/get.php?username=1041&password=123456&type=m3u

http://168.205.87.198:8555/get.php?username=1057&password=123456&type=m3u

http://168.205.87.198:8555/get.php?username=1113&password=123456&type=m3u

http://168.205.87.198:8555/get.php?username=1120&password=123456&type=m3u

http://168.205.87.198:8555/get.php?username=1124&password=123456&type=m3u

http://168.205.87.198:8555/get.php?username=1129&password=123456&type=m3u

http://streamgo.vip:8008/get.php?username=ISABEL&password=isabel&type=m3u_plus

http://live.vipserver.com.ua:80/get.php?
username=ziabekibremen&password=XOfLF1tALD&type=m3u

http://tuercatv2021.dynns.com:8080/get.php?
username=Iptvr2022d&password=abono&type=m3u

http://streamgo.vip:8008/get.php?username=User&password=user&type=m3u_plus

http://portal.geniptv.com:8080/get.php?
username=tolgax1x&password=JNKsdsadsa1a&type=m3u

http://rctv.tech/get.php?username=EbhTbZp8Zudg&password=kQyPeRtp2jRM&type=m3u

http://www.tvxclnt.com:8080/get.php?
username=tretvx2022&password=QP3J8nmeTvIZp82J&type=m3u

-----------------------------http://
www.sateliteguru.blogspot.com----------------------------------------

http://4k.dragonprox.live:8080/get.php?
username=MYSLwbpJfB&password=WMd2PAF8Qw&type=m3u&output=mpegts

http://dns.clientetv.net:8080/get.php?
username=Leeoofranca&password=VSk5nNXn72xq&type=m3u

http://ghewp.com/get.php?username=0077018667&password=8040849602&type=m3u

http://saw.duplex-ott.net:2052/get.php?
username=009727523905&password=169677264921&type=m3u_plus

http://restream.skyhd-iptv.com:25461/get.php?
username=8AWxutmEyj&password=dAKnp5memU&type=m3u

http://saw.duplex-ott.net:2052/get.php?
username=001465520525&password=001465520525&type=m3u_plus

http://ghewp.com/get.php?username=70587697500732&password=18736851093397&type=m3u

http://31.14.41.118:8080/get.php?username=TViBOXRU&password=DbTVQJpf1z&type=m3u
    """

    fun getPlaylists(): List<Playlist> {
        val allUrls = LinkedHashSet<String>() // Use Set to avoid duplicates
        
        // 1. Top Priority
        allUrls.addAll(topPriorityUrls)
        
        // 2. User Requested
        allUrls.addAll(userRequestedUrls)
        
        // 3. Parsed from Raw Text
        val parsedUrls = parseRawText()
        allUrls.addAll(parsedUrls)
        
        return allUrls.mapIndexed { index, url ->
            val name = when {
                url == "internal://debug" -> "Debug / Fast Test"
                url.contains("channels.json") -> "IPTV-ORG (JSON - Fast)"
                url.contains("PiratesTv") -> "Pirates TV (Community)"
                url.contains("index.country") -> "IPTV-ORG (Country)"
                url.contains("index.language") -> "IPTV-ORG (Language)"
                url.contains("ssungusa") -> "Samsung TV+"
                url.contains("xumo") -> "Xumo TV"
                url.contains("localnow") -> "Local Now"
                url.contains("lg.m3u") -> "LG Channels"
                url.contains("rok.m3u") -> "Roku Channels"
                url.contains("redbox") -> "Redbox TV"
                url.contains("distro") -> "Distro TV"
                url.contains("xiaomi") -> "Xiaomi TV"
                url.contains("tablo") -> "Tablo TV"
                url.contains("vizio") -> "Vizio Channels"
                else -> "Playlist ${index + 1}"
            }
            Playlist(name = name, url = url.trim(), isSelected = url.contains("channels.json"))
        }
    }

    private fun parseRawText(): List<String> {
        val lines = rawTextFileContent.lines()
        val urls = mutableListOf<String>()
        var currentUrl = ""

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("=") || trimmed.startsWith("-") || trimmed.contains("sateliteguru") || trimmed.contains("TUTORIASL")) continue
            
            if (trimmed.startsWith("http")) {
                if (currentUrl.isNotEmpty()) {
                    urls.add(currentUrl)
                }
                currentUrl = trimmed
            } else if (currentUrl.isNotEmpty() && !trimmed.startsWith("http")) {
                // Continuation of broken line
                currentUrl += trimmed
            }
        }
        if (currentUrl.isNotEmpty()) {
            urls.add(currentUrl)
        }
        return urls
    }
}
