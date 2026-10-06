package com.spiritfenix.stromr.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.time.Instant
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

object RssParser {
    fun parse(xml:String, feedUrl:String): List<MediaItem.Episode>{
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val episodes = mutableListOf<MediaItem.Episode>()
		var podcastTitle = ""

		var currentGuid = ""
        var currentTitle = ""
        var currentAudioUrl = ""
        var currentDurationSec = 0
        var currentDescription = ""
        var insideItem = false
		var currentEpisodeNumber = 0
		var pubDate: Long = 0

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
							currentGuid = ""
							currentEpisodeNumber = 0
							pubDate = 0
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
						"guid" -> {
							val text = if (parser.next() == XmlPullParser.TEXT) parser.text else ""
							if (insideItem) currentGuid = text
						}
						"episode" -> {
							val text = if (parser.next() == XmlPullParser.TEXT) parser.text else ""
							if (insideItem) currentEpisodeNumber = text.toInt()
						}
						"pubDate" -> {
							val text = if (parser.next() == XmlPullParser.TEXT) parser.text else ""
							if (insideItem) {
								pubDate = try {
									Instant.from(RFC_1123_DATE_TIME.parse(text)).toEpochMilli()
								} catch (e: Exception) {
									0L
								}
							}
						}
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        insideItem = false
                        episodes.add(
                            MediaItem.Episode(
								id = "$feedUrl|${currentGuid.ifEmpty { currentAudioUrl }}",
								feedUrl = feedUrl,
								title = currentTitle,
								audioUrl = currentAudioUrl,
								imageUrl = "",
								durationSec = currentDurationSec,
								podcastTitle = podcastTitle,
								description = currentDescription,
								episodeNumber = currentEpisodeNumber,
								pubDate = pubDate
							)
                        )
                    }
                }
            }
            eventType = parser.next()
        }
		val total=episodes.size
        return episodes.mapIndexed { index, episode ->
			if(episode.episodeNumber!=0){
				episode
			}
			else{
				episode.copy(episodeNumber = total-index)
			}
		}
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