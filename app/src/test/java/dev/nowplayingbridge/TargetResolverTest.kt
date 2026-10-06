package dev.nowplayingbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetResolverTest {
    private val morphe = "app.morphe.android.apps.youtube.music"
    private val anddea = "anddea.youtube.music"
    private val revanced = "app.revanced.android.apps.youtube.music"
    private val rvx = "app.rvx.android.apps.youtube.music"
    private val rvxClone = "com.rvx.android.apps.youtube.music"
    private val official = "com.google.android.apps.youtube.music"

    private fun resolver(vararg handlers: String) =
        TargetResolver("com.pandora.android", { handlers.toSet() })

    @Test fun prefersMorpheRegardlessOfDiscoveryOrder() {
        assertEquals(morphe, resolver(official, morphe).resolve())
        assertEquals(morphe, resolver(morphe, official).resolve())
    }

    @Test fun fallsBackToOfficialWhenMorpheCannotHandleSearch() {
        assertEquals(official, resolver(official).resolve())
    }

    @Test fun prefersVerifiedPatchedVariantToOfficial() {
        assertEquals(anddea, resolver(official, anddea).resolve())
    }

    @Test fun prefersKnownPatchedPackagesInConfiguredOrder() {
        assertEquals(anddea, resolver(official, rvxClone, rvx, revanced, anddea).resolve())
        assertEquals(revanced, resolver(official, rvxClone, rvx, revanced).resolve())
        assertEquals(rvx, resolver(official, rvxClone, rvx).resolve())
        assertEquals(rvxClone, resolver(official, rvxClone).resolve())
    }

    @Test fun rejectsUnrelatedHandlersAndMissingTargets() {
        assertNull(resolver("com.spotify.music", "com.aspiro.tidal", "deezer.android.app",
            "com.pandora.android", "unknown.revanced.music").resolve())
        assertNull(resolver().resolve())
    }

    @Test fun excludesSelfEvenIfConfiguredAsTarget() {
        val targets = listOf(MusicTarget("com.pandora.android", 200), MusicTarget(official, 50))
        assertEquals(official, TargetResolver("com.pandora.android",
            { setOf("com.pandora.android", official) }, targets).resolve())
    }

    @Test fun supportsExplicitCustomVariantsAndStableTies() {
        val targets = listOf(MusicTarget("custom.ytm", 150), MusicTarget(morphe, 150))
        assertEquals("custom.ytm", TargetResolver("com.pandora.android",
            { setOf(morphe, "custom.ytm") }, targets).resolve())
    }
}
