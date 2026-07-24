package com.spiritfenix.stromr.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RssParserTest {
    private val sampleFeed = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
          <channel>
            <title>Test Podcast</title>
            <item>
              <title>Episode One</title>
              <description>First episode description</description>
              <enclosure url="https://example.com/ep1.mp3" type="audio/mpeg" length="123" />
              <itunes:duration>00:34:12</itunes:duration>
            </item>
            <item>
              <title>Episode Two</title>
              <description>Second episode description</description>
              <enclosure url="https://example.com/ep2.mp3" type="audio/mpeg" length="456" />
              <itunes:duration>1245</itunes:duration>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    @Test
    fun `parses correct number of episodes`() {
        val episodes = RssParser.parse(sampleFeed)
        assertEquals(2, episodes.size)
    }

    @Test
    fun `parses episode title and podcast title correctly`() {
        val episodes = RssParser.parse(sampleFeed)
        assertEquals("Episode One", episodes[0].title)
        assertEquals("Test Podcast", episodes[0].podcastTitle)
    }

    @Test
    fun `parses audio url from enclosure`() {
        val episodes = RssParser.parse(sampleFeed)
        assertEquals("https://example.com/ep1.mp3", episodes[0].audioUrl)
    }

    @Test
    fun `parses hh-mm-ss duration format`() {
        val episodes = RssParser.parse(sampleFeed)
        assertEquals(34 * 60 + 12, episodes[0].durationSec)
    }

    @Test
    fun `parses raw-seconds duration format`() {
        val episodes = RssParser.parse(sampleFeed)
        assertEquals(1245, episodes[1].durationSec)
    }
}