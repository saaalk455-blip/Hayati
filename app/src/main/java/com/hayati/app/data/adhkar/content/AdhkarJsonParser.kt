package com.hayati.app.data.adhkar.content

import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.repository.AdhkarContentException
import org.json.JSONException
import org.json.JSONObject

/**
 * Parses `assets/adhkar.json`:
 * `{"version":1,"source":"…","adhkar":[{"id":"m1","category":"morning","text":"…","repeat":3,"source":"…","takhrij":"…"}]}`.
 *
 * Strict on purpose: an invalid entry fails the whole file rather than silently dropping or
 * changing religious text.
 */
object AdhkarJsonParser {
    fun parse(json: String): List<Dhikr> {
        try {
            val array = JSONObject(json).getJSONArray("adhkar")
            val seen = HashSet<String>()
            return (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                val id = item.getString("id").trim()
                val category = AdhkarCategory.fromKey(item.getString("category"))
                    ?: throw AdhkarContentException("Unknown category at entry $index")
                val text = item.getString("text").trim()
                // The repeat count is required: a missing one is never silently assumed to be 1.
                val repeat = if (item.has("repeat")) item.getInt("repeat") else {
                    throw AdhkarContentException("Missing repeat for '$id'")
                }
                if (id.isEmpty()) throw AdhkarContentException("Blank id at entry $index")
                if (!seen.add(id)) throw AdhkarContentException("Duplicate id '$id'")
                if (text.isEmpty()) throw AdhkarContentException("Blank text for '$id'")
                if (repeat < 1) throw AdhkarContentException("Invalid repeat for '$id'")
                Dhikr(
                    id, category, text, repeat,
                    source = item.optString("source", "").trim(),
                    takhrij = item.optString("takhrij", "").trim(),
                )
            }
        } catch (e: JSONException) {
            throw AdhkarContentException("Adhkar file is malformed", e)
        }
    }
}
