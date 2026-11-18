package com.hayati.app.data.adhkar.content

import com.hayati.app.domain.adhkar.model.Dhikr

/**
 * Extra rules for the content file that ships in the app. The parser only checks that the file is
 * well formed; these rules check that every entry is documented, so nothing undocumented or
 * placeholder-like can ship. The rules cannot judge whether a text is *correct*: that needs a human
 * comparison with the original source (see docs/ADHKAR_CONTENT.md).
 */
object AdhkarReleaseRules {
    private val placeholderMarkers = listOf("اختبار", "تجريبي", "TODO", "lorem", "placeholder")

    /** Returns human-readable problems; empty means the list satisfies the release rules. */
    fun violations(adhkar: List<Dhikr>): List<String> = buildList {
        adhkar.forEach { dhikr ->
            if (dhikr.source.isBlank()) add("${dhikr.id}: missing source")
            if (dhikr.takhrij.isBlank()) add("${dhikr.id}: missing takhrij")
            val marker = placeholderMarkers.firstOrNull { dhikr.text.contains(it, ignoreCase = true) }
            if (marker != null) add("${dhikr.id}: text contains placeholder marker '$marker'")
        }
        val texts = adhkar.groupBy { it.category to it.text }
        texts.filterValues { it.size > 1 }.values.forEach { dups ->
            add("duplicate text in ${dups.first().category}: ${dups.joinToString { it.id }}")
        }
    }
}
