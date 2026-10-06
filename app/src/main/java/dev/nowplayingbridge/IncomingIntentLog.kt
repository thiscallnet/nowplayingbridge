package dev.nowplayingbridge

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import java.lang.reflect.Array

/** Full request diagnostics are available only from debug builds. */
internal object IncomingIntentLog {
    private const val TAG = "NowPlayingBridgeIntent"

    fun write(intent: Intent) {
        if (!BuildConfig.LOG_INCOMING_INTENTS) return
        Log.i(TAG, "BEGIN incoming intent")
        describe(intent).lineSequence().forEach { line ->
            val parts = line.chunked(MAX_LOG_LINE_CHARS)
            parts.forEachIndexed { index, part ->
                Log.i(TAG, if (parts.size == 1) part else "part ${index + 1}/${parts.size}: $part")
            }
        }
        Log.i(TAG, "END incoming intent")
    }

    private fun describe(intent: Intent, depth: Int = 0): String = buildString {
        val prefix = "  ".repeat(depth)
        appendLine("Intent {")
        appendLine("${prefix}  action=${safe { intent.action }}")
        appendLine("${prefix}  data=${safe { intent.dataString }}")
        appendLine("${prefix}  mimeType=${safe { intent.type }}")
        appendLine("${prefix}  categories=${safe { intent.categories?.sorted() }}")
        appendLine("${prefix}  flags=0x${intent.flags.toString(16)}")
        appendLine("${prefix}  component=${safe { intent.component }}")
        appendLine("${prefix}  package=${safe { intent.`package` }}")
        if (depth < MAX_DEPTH) {
            val selector = safe { intent.selector }
            appendLine(
                "${prefix}  selector=${if (selector is Intent) describe(selector, depth + 1) else selector}",
            )
        } else {
            appendLine("${prefix}  selector=<depth limit>")
        }
        appendLine("${prefix}  clipData=${describeClip(intent.clipData)}")
        appendLine("${prefix}  extras:")
        val extras = safe { intent.extras } as? Bundle
        if (extras == null || extras.isEmpty) {
            appendLine("${prefix}    <none>")
        } else {
            extras.keySet().sorted().forEach { key ->
                val value = safe { extras.get(key) }
                val type = value?.javaClass?.name ?: "null"
                appendLine("${prefix}    $key [$type] = ${formatValue(value, depth)}")
            }
        }
        append("${prefix}}}")
    }

    private fun describeClip(clip: ClipData?): String {
        if (clip == null) return "null"
        return buildString {
            append("label=${safe { clip.description.label }}, ")
            append("mimeTypes=${safe { (0 until clip.description.mimeTypeCount).map(clip.description::getMimeType) }}, ")
            append("items=[")
            for (index in 0 until clip.itemCount) {
                if (index > 0) append(", ")
                val item = clip.getItemAt(index)
                append("{text=${safe { item.text }}, html=${safe { item.htmlText }}, ")
                append("uri=${safe { item.uri }}, intent=${safe { item.intent?.let(::describe) }}}")
            }
            append("]")
        }
    }

    private fun formatValue(value: Any?, depth: Int): String = when {
        value == null -> "null"
        value is Bundle && depth < MAX_DEPTH -> value.keySet().sorted().joinToString(", ", "{", "}") { key ->
            val nested = safe { value.get(key) }
            "$key [${nested?.javaClass?.name ?: "null"}]=${formatValue(nested, depth + 1)}"
        }
        value is Bundle -> "<bundle depth limit>"
        value is Uri -> value.toString()
        value.javaClass.isArray -> (0 until Array.getLength(value)).joinToString(prefix = "[", postfix = "]") {
            formatValue(Array.get(value, it), depth + 1)
        }
        else -> safe { value.toString() }?.toString() ?: "<toString failed>"
    }

    private inline fun safe(block: () -> Any?): Any? = try {
        block()
    } catch (error: Exception) {
        "<error: ${error.javaClass.name}: ${error.message}>"
    }

    private const val MAX_DEPTH = 6
    private const val MAX_LOG_LINE_CHARS = 3000
}
