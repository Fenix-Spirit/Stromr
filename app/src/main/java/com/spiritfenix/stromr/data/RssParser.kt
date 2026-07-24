package com.spiritfenix.stromr.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object RssParser {
    fun parse(xml:String): List<MediaItem.Episode>{
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val episodes = mutableListOf<MediaItem.Episode>()
        var podcastTitle = ""

        var currentTitle = ""
        var currentAudioUrl = ""
        var currentDurationSec = 0
        var currentDescription = ""
        var insideItem = false
        var episodeCounter = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "channel" -> { /* no-op, just context */ }
                        "item" -> {
                            insideItem = true
                            currentTitle = ""
                            currentAudioUrl = ""
                            currentDurationSec = 0
                            currentDescription = ""
                        }
                        "title" -> {
                            val text = if (parser.next() == XmlPullParser.TEXT) parser.text else ""
                            if (insideItem) currentTitle = text else podcastTitle = text
                        }
                        "enclosure" -> {
                            currentAudioUrl = parser.getAttributeValue(null, "url") ?: ""
                        }
                        "duration" -> {
                            val text = if (parser.next() == XmlPullParser.TEXT) parser.text else ""
                            currentDurationSec = parseDurationToSeconds(text)
                        }
                        "description" -> {
                            val text = if (parser.next() == XmlPullParser.TEXT) parser.text else ""
                            if (insideItem) currentDescription = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        insideItem = false
                        episodeCounter++
                        episodes.add(
                            MediaItem.Episode(
                                id = episodeCounter,
                                title = currentTitle,
                                audioUrl = currentAudioUrl,
                                imageUrl = "",
                                durationSec = currentDurationSec,
                                podcastTitle = podcastTitle,
                                description = currentDescription,
                                episodeNumber = episodeCounter
                            )
                        )
                    }
                }
            }
            eventType = parser.next()
        }
        return episodes
    }

    private fun parseDurationToSeconds(raw: String): Int {
        val parts = raw.trim().split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            2 -> parts[0] * 60 + parts[1]
            1 -> parts[0]
            else -> 0
        }
    }
}