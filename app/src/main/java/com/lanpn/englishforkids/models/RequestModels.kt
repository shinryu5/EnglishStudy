package com.lanpn.englishforkids.models

const val MAX_RESULTS = 15

class SingleLocalizationRequest(base64: String) {
    val requests = listOf(AnnotateRequest(base64))
}

class AnnotateRequest(base64: String) {
    val image = JsonImage(base64)
    val features = listOf(JsonFeature("OBJECT_LOCALIZATION"))
}

class JsonImage(val content: String)

class JsonFeature(val type: String, val maxResults: Int = MAX_RESULTS)
