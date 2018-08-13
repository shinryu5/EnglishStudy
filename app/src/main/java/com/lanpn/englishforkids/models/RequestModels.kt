package com.lanpn.englishforkids.models

const val DEFAULT_MAX_RESULTS = 15

class ObjectLocalizationRequest(base64: String) {
    val requests = listOf(AnnotationRequest(base64))
}

class AnnotationRequest(base64: String) {
    val image = B64Image(base64)
    val features = listOf(RequestFeature("OBJECT_LOCALIZATION"))
}

class B64Image(val content: String)

class RequestFeature(val type: String, val maxResults: Int = DEFAULT_MAX_RESULTS)
