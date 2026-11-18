package com.hayati.app.data.adhkar.content

import android.content.Context
import com.hayati.app.domain.adhkar.model.Dhikr
import com.hayati.app.domain.adhkar.repository.AdhkarContentException
import com.hayati.app.domain.adhkar.repository.AdhkarContentSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AssetAdhkarContentSource(
    private val context: Context,
    private val fileName: String = "adhkar.json",
) : AdhkarContentSource {
    private var cached: List<Dhikr>? = null

    override suspend fun load(): List<Dhikr> = cached ?: withContext(Dispatchers.IO) {
        val text = try {
            context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: IOException) {
            throw AdhkarContentException("Cannot read $fileName", e)
        }
        AdhkarJsonParser.parse(text).also { cached = it }
    }
}
