package dev.nowplayingbridge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

data class MusicTarget(val packageName: String, val priority: Int)

/** Only configured packages can be selected, regardless of other installed handlers. */
class TargetResolver(
    private val ownPackage: String,
    private val discoverPackages: () -> Set<String>,
    knownTargets: List<MusicTarget> = KNOWN_TARGETS,
) {
    // Equal priorities retain configuration order.
    private val targets = knownTargets.sortedByDescending { it.priority }

    fun resolve(): String? {
        val handlers = discoverPackages()
        return targets.firstOrNull {
            it.packageName != ownPackage && it.packageName in handlers
        }?.packageName
    }

    companion object {
        val KNOWN_TARGETS = listOf(
            MusicTarget("app.morphe.android.apps.youtube.music", 100),
            // Explicitly supported patched YouTube Music variant.
            MusicTarget("anddea.youtube.music", 75),
            MusicTarget("com.google.android.apps.youtube.music", 50),
        )

        fun from(context: Context): TargetResolver = TargetResolver(context.packageName, {
            val search = Intent(Intent.ACTION_VIEW, Uri.parse(PROBE_SEARCH_URL))
                .addCategory(Intent.CATEGORY_DEFAULT)
            context.packageManager.queryIntentActivities(search, PackageManager.MATCH_DEFAULT_ONLY)
                .asSequence()
                .filter { it.activityInfo.exported }
                .map { it.activityInfo.packageName }
                .toSet()
        })

        private const val PROBE_SEARCH_URL =
            "https://music.youtube.com/search?q=now+playing+bridge"
    }
}
