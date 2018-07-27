package com.lanpn.englishforkids.models

import java.io.Serializable

class SingleLocalizationRequest(base64: String) {
    val requests = listOf(AnnotateRequest(base64))
}

class AnnotateRequest(base64: String) {
    val image = JsonImage(base64)
    val features = listOf(JsonFeature("OBJECT_LOCALIZATION"))
}

class JsonImage(val content: String)

class JsonFeature(val type: String, val maxResults: Int = 10)

class AnnotationResponse {
    var responses: List<ImageResponse>? = null
}

class ImageResponse {
    var localizedObjectAnnotations: ArrayList<LocalizedObjectAnnotation>? = null
}

class LocalizedObjectAnnotation : Serializable {
    var mid: String? = null
    var languageCode: String? = null
    var name: String? = null
    var score: String? = null
    var boundingPoly: BoundingPoly? = null
}

class BoundingPoly : Serializable {
    var vertices: ArrayList<Vertex>? = null
    var normalizedVertices: ArrayList<Vertex>? = null
}

class Vertex : Serializable {
    var x: Float? = null
    var y: Float? = null
}
