package dev.nowplayingbridge

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BridgeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forward(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        forward(intent)
    }

    private fun forward(incoming: Intent) {
        IncomingIntentLog.write(incoming)
        try {
            if (incoming.action != MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) return
            val target = TargetResolver.from(this).resolve()
            if (target == null) {
                Toast.makeText(this, R.string.no_target, Toast.LENGTH_SHORT).show()
                return
            }
            val query = incoming.getStringExtra(android.app.SearchManager.QUERY)
                ?.takeIf(String::isNotBlank)
                ?: searchQuery(
                    incoming.getStringExtra(MediaStore.EXTRA_MEDIA_ARTIST),
                    incoming.getStringExtra(MediaStore.EXTRA_MEDIA_TITLE),
                )
            val exactWatchUri = exactYoutubeMusicWatchUri(incoming.data)
            if (exactWatchUri == null && query == null) {
                Toast.makeText(this, R.string.no_song_details, Toast.LENGTH_SHORT).show()
                return
            }
            val outgoing = Intent(
                Intent.ACTION_VIEW,
                exactWatchUri ?: Uri.parse(youtubeMusicSearchUrl(query!!)),
            )
                .setPackage(target)
                .addCategory(Intent.CATEGORY_DEFAULT)
            for (key in SEARCH_EXTRAS) {
                incoming.getStringExtra(key)?.let { outgoing.putExtra(key, it) }
            }
            if (!outgoing.hasExtra(MediaStore.EXTRA_MEDIA_FOCUS)) {
                outgoing.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/audio")
            }
            if (packageManager.resolveActivity(outgoing, PackageManager.MATCH_DEFAULT_ONLY) == null) {
                Toast.makeText(this, R.string.target_unavailable, Toast.LENGTH_SHORT).show()
                return
            }
            startActivity(outgoing)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.target_unavailable, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(this, R.string.target_unavailable, Toast.LENGTH_SHORT).show()
        } finally {
            // Theme.NoDisplay requires finishing before onResume, including failure paths.
            finish()
        }
    }

    companion object {
        private val SEARCH_EXTRAS = listOf(
            MediaStore.EXTRA_MEDIA_FOCUS,
            MediaStore.EXTRA_MEDIA_ARTIST,
            MediaStore.EXTRA_MEDIA_TITLE,
            MediaStore.EXTRA_MEDIA_ALBUM,
            MediaStore.EXTRA_MEDIA_GENRE,
            MediaStore.EXTRA_MEDIA_PLAYLIST,
            android.app.SearchManager.QUERY,
        )

        internal fun searchQuery(artist: String?, title: String?): String? =
            listOfNotNull(artist, title)
                .map(String::trim)
                .filter(String::isNotEmpty)
                .joinToString(" ")
                .ifEmpty { null }

        internal fun youtubeMusicSearchUrl(query: String): String =
            "https://music.youtube.com/search?q=${URLEncoder.encode(query, StandardCharsets.UTF_8.name())}"

        private fun exactYoutubeMusicWatchUri(data: Uri?): Uri? {
            if (data?.scheme != "https" || data.host != "music.youtube.com" || data.path != "/watch") {
                return null
            }
            if (data.getQueryParameter("vm") != "audio") return null
            val videoId = data.getQueryParameter("v")
                ?.takeIf { it.matches(Regex("[A-Za-z0-9_-]{11}")) }
                ?: return null
            return Uri.Builder()
                .scheme("https")
                .authority("music.youtube.com")
                .path("/watch")
                .appendQueryParameter("vm", "audio")
                .appendQueryParameter("v", videoId)
                .build()
        }
    }
}
