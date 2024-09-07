package com.github.sey2.intlautosort.domain.json

import com.google.gson.JsonParser

class JsonFixer {
    fun fixMalformedJson(content: String): Result<String> {
        return runCatching {
            content
                .replace("\n", "")
                .replace(",}", "}")
                .apply { JsonParser.parseString(this) }
        }
    }
}
