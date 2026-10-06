package com.spiritfenix.stromr.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RssParserTest {
	private val feedUrl="https://example.com/feed"
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
        val episodes = RssParser.parse(sampleFeed, feedUrl)
        assertEquals(2, episodes.size)
    }

    @Test
    fun `parses episode title and podcast title correctly`() {
        val episodes = RssParser.parse(sampleFeed, feedUrl)
        assertEquals("Episode One", episodes[0].title)
        assertEquals("Test Podcast", episodes[0].podcastTitle)
    }

    @Test
    fun `parses audio url from enclosure`() {
        val episodes = RssParser.parse(sampleFeed, feedUrl)
        assertEquals("https://example.com/ep1.mp3", episodes[0].audioUrl)
    }

    @Test
    fun `parses hh-mm-ss duration format`() {
        val episodes = RssParser.parse(sampleFeed, feedUrl)
        assertEquals(34 * 60 + 12, episodes[0].durationSec)
    }

    @Test
    fun `parses raw-seconds duration format`() {
        val episodes = RssParser.parse(sampleFeed, feedUrl)
        assertEquals(1245, episodes[1].durationSec)
    }
    @Test
    fun `parses episode description`() {
        val episodes = RssParser.parse(sampleFeed, feedUrl)
        assertEquals("First episode description", episodes[0].description)
        assertEquals("Second episode description", episodes[1].description)
    }
	@Test
	fun `id falls back to audio url when guid is missing`() {
		val episodes = RssParser.parse(sampleFeed, feedUrl)
		assertEquals("$feedUrl|https://example.com/ep1.mp3", episodes[0].id)
	}

	@Test
	fun `ids differ across feeds for the same guid`() {
		val feed = """
        <rss><channel><title>T</title>
          <item><title>E</title><guid>1</guid>
            <enclosure url="https://example.com/a.mp3" /></item>
        </channel></rss>
    """.trimIndent()
		val a = RssParser.parse(feed, "https://a.com/feed")[0].id
		val b = RssParser.parse(feed, "https://b.com/feed")[0].id
		assertNotEquals(a, b)
	}

	@Test
	fun `parses feed with pubDate successfully`() {
		val feed = """
        <rss><channel><title>T</title>
          <item>
            <title>E</title>
            <guid>1</guid>
            <pubDate>Sun, 19 May 2024 15:30:00 GMT</pubDate>
            <enclosure url="https://example.com/a.mp3" />
          </item>
        </channel></rss>
    """.trimIndent()
		val episodes = RssParser.parse(feed, "https://example.com/feed")
		assertEquals(1, episodes.size)
		assertEquals("E", episodes[0].title)
		assertEquals(1716132600000L, episodes[0].pubDate)
	}

	@Test
	fun `handles malformed pubDate gracefully`() {
		val feed = """
        <rss><channel><title>T</title>
          <item>
            <title>E</title>
            <guid>1</guid>
            <pubDate>malformed-date</pubDate>
            <enclosure url="https://example.com/a.mp3" />
          </item>
        </channel></rss>
    """.trimIndent()
		val episodes = RssParser.parse(feed, "https://example.com/feed")
		assertEquals(1, episodes.size)
		assertEquals("E", episodes[0].title)
		assertEquals(0L, episodes[0].pubDate)
	}

	@Test
	fun `handles empty pubDate gracefully`() {
		val feed = """
        <rss><channel><title>T</title>
          <item>
            <title>E</title>
            <guid>1</guid>
            <pubDate></pubDate>
            <enclosure url="https://example.com/a.mp3" />
          </item>
        </channel></rss>
    """.trimIndent()
		val episodes = RssParser.parse(feed, "https://example.com/feed")
		assertEquals(1, episodes.size)
		assertEquals("E", episodes[0].title)
		assertEquals(0L, episodes[0].pubDate)
	}
}