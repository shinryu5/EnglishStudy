package com.lanpn.englishforkids.models

import java.io.Serializable
import kotlin.math.max

data class AnnotationResponse(var responses: List<ImageResponse>? = null)
data class ImageResponse(var localizedObjectAnnotations: ArrayList<LocalizedObjectAnnotation>? = null)
data class LocalizedObjectAnnotation (
    var mid: String? = null,
    var languageCode: String? = null,
    var name: String? = null,
    var score: String? = null,
    var boundingPoly: BoundingPoly? = null
) : Serializable

data class BoundingPoly(var normalizedVertices: ArrayList<Vertex>? = null) : Serializable {
    val topCenter: Vertex
        get() {
            if (normalizedVertices == null) return Vertex(0f, 0f)
            val vTop = normalizedVertices!!.minBy { if (it.y != null) it.y!! else 0f }
            val vLeft = normalizedVertices!!.minBy { if (it.x != null) it.x!! else 0f }
            val vRight = normalizedVertices!!.maxBy { if (it.x != null) it.x!! else 0f }
            return Vertex((vLeft!!.x!! + vRight!!.x!!) / 2f, vTop!!.y)
        }
    val bottomCenter: Vertex
        get() {
            if (normalizedVertices == null) return Vertex(0f, 0f)
            val vBottom = normalizedVertices!!.maxBy { it.y!! }
            val vLeft = normalizedVertices!!.minBy { it.x!! }
            val vRight = normalizedVertices!!.maxBy { it.x!! }
            return Vertex((vLeft!!.x!! + vRight!!.x!!) / 2f, vBottom!!.y)
        }
    val leftMiddle: Vertex
        get() {
            if (normalizedVertices == null) return Vertex(0f, 0f)
            val vLeft = normalizedVertices!!.minBy { it.x!! }
            val vBottom = normalizedVertices!!.maxBy { it.y!! }
            val vTop = normalizedVertices!!.minBy { it.y!! }
            return Vertex(vLeft!!.x, (vBottom!!.y!! + vTop!!.y!!) / 2f)
        }

    val rightMiddle: Vertex
        get() {
            if (normalizedVertices == null) return Vertex(0f, 0f)
            val vRight = normalizedVertices!!.maxBy { it.x!! }
            val vBottom = normalizedVertices!!.maxBy { it.y!! }
            val vTop = normalizedVertices!!.minBy { it.y!! }
            return Vertex(vRight!!.x, (vBottom!!.y!! + vTop!!.y!!) / 2f)
        }

    val diameter: Float
        get() {
            try {
                return max(bottomCenter.y!! - topCenter.y!!, rightMiddle.x!! - leftMiddle.x!!)
            } catch (e: NullPointerException) {
                return 0f
            }
        }

    fun isInside(x: Float, y: Float, width: Float, height: Float) : Boolean {
        try {
            return (x >= leftMiddle.deNormalize(width, height).x!!) and
                    (x <= rightMiddle.deNormalize(width, height).x!!) and
                    (y >= topCenter.deNormalize(width, height).y!!) and
                    (y <= bottomCenter.deNormalize(width, height).y!!)
        } catch (e: NullPointerException) {
            return false
        }
    }
}

data class Vertex(var x: Float? = null, var y: Float? = null) : Serializable {
    fun deNormalize(width: Float, height: Float) : Vertex {
        val nx = if (x == null) null else x!! *  width
        val ny = if (y == null) null else y!! *  height
        return Vertex(nx, ny)
    }
}