package dev.nowplayingbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BridgeActivityTest {
    @Test fun buildsMissingQueryFromArtistAndTitle() {
        assertEquals("Example Artist Example Track", BridgeActivity.searchQuery("Example Artist", "Example Track"))
    }

    @Test fun handlesMissingOrBlankSearchTerms() {
        assertEquals("Example Track", BridgeActivity.searchQuery(" ", "Example Track"))
        assertEquals("Example Artist", BridgeActivity.searchQuery("Example Artist", null))
        assertNull(BridgeActivity.searchQuery(null, " "))
    }

    @Test fun encodesSearchTextForYoutubeMusicDeepLink() {
        assertEquals(
            "https://music.youtube.com/search?q=Example+Track+by+Artist+%26+Guest",
            BridgeActivity.youtubeMusicSearchUrl("Example Track by Artist & Guest"),
        )
    }
}
