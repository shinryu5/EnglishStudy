package com.lanpn.englishforkids.models

import android.os.Parcelable
import java.io.Serializable
import kotlin.math.max

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

data class AnnotationResponse(var responses: List<ImageResponse>? = null)

data class ImageResponse(var localizedObjectAnnotations: ArrayList<LocalizedObjectAnnotation>? = null)

data class LocalizedObjectAnnotation (
    var mid: String? = null,
    var languageCode: String? = null,
    var name: String? = null,
    var score: String? = null,
    var boundingPoly: BoundingPoly? = null
) : Serializable {

}

data class BoundingPoly(var normalizedVertices: ArrayList<Vertex>? = null) : Serializable {
    val topCenter: Vertex?
        get() {
            if (normalizedVertices == null) return null
            val vTop = normalizedVertices!!.minBy { it.y!! }
            val vLeft = normalizedVertices!!.minBy { it.x!! }
            val vRight = normalizedVertices!!.maxBy { it.x!! }
            return Vertex((vLeft!!.x!! + vRight!!.x!!) / 2f, vTop!!.y)
        }
    val bottomCenter: Vertex?
        get() {
            if (normalizedVertices == null) return null
            val vBottom = normalizedVertices!!.maxBy { it.y!! }
            val vLeft = normalizedVertices!!.minBy { it.x!! }
            val vRight = normalizedVertices!!.maxBy { it.x!! }
            return Vertex((vLeft!!.x!! + vRight!!.x!!) / 2f, vBottom!!.y)
        }
    val leftMiddle: Vertex?
        get() {
            if (normalizedVertices == null) return null
            val vLeft = normalizedVertices!!.minBy { it.x!! }
            val vBottom = normalizedVertices!!.maxBy { it.y!! }
            val vTop = normalizedVertices!!.minBy { it.y!! }
            return Vertex(vLeft!!.x, (vBottom!!.y!! + vTop!!.y!!) / 2f)
        }
    val rightMiddle: Vertex?
        get() {
            if (normalizedVertices == null) return null
            val vRight = normalizedVertices!!.maxBy { it.x!! }
            val vBottom = normalizedVertices!!.maxBy { it.y!! }
            val vTop = normalizedVertices!!.minBy { it.y!! }
            return Vertex(vRight!!.x, (vBottom!!.y!! + vTop!!.y!!) / 2f)
        }

    val diameter: Float
        get() {
            return max(bottomCenter!!.y!! - topCenter!!.y!!, rightMiddle!!.x!! - leftMiddle!!.x!!)
        }

    fun isInside(x: Float, y: Float, width: Float, height: Float) : Boolean {
        return (x >= leftMiddle!!.deNormalize(width, height).x!!) and
                (x <= rightMiddle!!.deNormalize(width, height).x!!) and
                (y >= topCenter!!.deNormalize(width, height).y!!) and
                (y <= bottomCenter!!.deNormalize(width, height).y!!)
    }
}

data class Vertex(var x: Float? = null, var y: Float? = null) : Serializable {
    fun deNormalize(width: Float, height: Float) : Vertex {
        val nx = if (x == null) null else x!! *  width
        val ny = if (y == null) null else y!! *  height
        return Vertex(nx, ny)
    }
}
