package com.github.sey2.intlautosort.domain.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.*

class JsonSorter {
    fun sortJson(content: String): String {
        return JsonParser.parseString(content).asJsonObject.run {
            entrySet()
                .sortedBy { it.key.lowercase(Locale.getDefault()) }
                .associate { it.key to it.value }
                .let { sortedEntries ->
                    GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create()
                        .toJson(JsonObject().apply {
                            sortedEntries.forEach { (key, value) -> add(key, value) }
                        })
                }
        }
    }
}