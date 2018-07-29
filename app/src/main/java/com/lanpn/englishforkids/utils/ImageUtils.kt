package com.lanpn.englishforkids.utils

import android.graphics.*
import com.lanpn.englishforkids.models.BoundingPoly
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import kotlin.math.max
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.media.ExifInterface
import android.os.AsyncTask
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.drawable.Drawable
import android.graphics.Matrix.MSCALE_Y
import android.graphics.Matrix.MSCALE_X
import android.widget.ImageView


fun mutableBitmap(bitmap: Bitmap) : Bitmap {
    return bitmap.copy(bitmap.config, true)
}

private fun calculateInSampleSize(options: BitmapFactory.Options,
                                  reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun loadSampledBitmap(path: String, scale: Float = 0.3f) : Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    options.inSampleSize = calculateInSampleSize(options, (options.outWidth * scale).toInt(),
            (options.outHeight * scale).toInt())
    options.inJustDecodeBounds = false
    val bitmap = BitmapFactory.decodeFile(path, options)

    // Correctly rotate the image
    val exif = ExifInterface(path)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
    val matrix = Matrix()
    when (orientation) {
        6 -> matrix.postRotate(90f)
        3 -> matrix.postRotate(180f)
        8 -> matrix.postRotate(270f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


fun scaleBitmap(bitmap: Bitmap, scale: Float = 0.5f) : Bitmap {
    val m = Matrix()
    m.postScale(scale, scale)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, false)
}

const val OVERLAP_THRESHOLD = 1e-9

fun filterAnnotations(annotations: List<LocalizedObjectAnnotation>) : List<LocalizedObjectAnnotation> {
    val newAnnotations = ArrayList<LocalizedObjectAnnotation>()
    for (i in 0 until annotations.size) {
        try {
            val bc1 = annotations[i].boundingPoly!!.topCenter
            var overlapped = false
            for (j in 0 until i) {
                try {
                    val bc2 = annotations[j].boundingPoly!!.topCenter
                    if ((bc1.x!! - bc2.x!! <= OVERLAP_THRESHOLD) and
                            (bc1.y!! - bc2.y!! <= OVERLAP_THRESHOLD)) {
                        overlapped = true
                        break
                    }
                } catch (e: NullPointerException) {
                    continue
                }
            }

            if (!overlapped) newAnnotations.add(annotations[i])
        } catch (e: NullPointerException) {
            continue
        }
    }

    return newAnnotations
}

fun drawAnnotations(bitmap: Bitmap, annotations: List<LocalizedObjectAnnotation>, copy: Boolean = true) : Bitmap {
    val mutBitmap = if (copy) mutableBitmap(bitmap) else bitmap
    val canvas = Canvas(mutBitmap)

    // Paint for bounding polygons
    val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    rectPaint.style = Paint.Style.STROKE
    rectPaint.color = Color.rgb(244, 192, 78)
    rectPaint.strokeWidth = 0.01f * bitmap.width

    // Paint for text
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    textPaint.color = Color.rgb(244, 192, 78)
    textPaint.textSize = 0.05f * bitmap.width

    for (annotation in annotations) {
        if ((annotation.boundingPoly != null) and (annotation.name != null)) {
            drawPoly(canvas, rectPaint, annotation.boundingPoly!!)
            insertText(canvas, textPaint, annotation.boundingPoly!!, annotation.name!!)
        }
    }

    return mutBitmap
}

fun drawPoly(canvas: Canvas, paint: Paint, boundingPoly: BoundingPoly) {
    if (boundingPoly.normalizedVertices == null) return
    val cWidth = canvas.width.toFloat()
    val cHeight = canvas.height.toFloat()
    val size = boundingPoly.normalizedVertices!!.size
    for (i in 0 until size) {
        val start = boundingPoly.normalizedVertices!![i].deNormalize(cWidth, cHeight)
        val end = boundingPoly.normalizedVertices!![(i+1) % size].deNormalize(cWidth, cHeight)
        canvas.drawLine(start.x!!, start.y!!, end.x!!, end.y!!, paint)
    }
}

fun insertText(canvas: Canvas, paint: Paint, boundingPoly: BoundingPoly, text: String) {
    val cWidth = canvas.width.toFloat()
    val cHeight = canvas.height.toFloat()
    val bottomCenter = boundingPoly.topCenter.deNormalize(cWidth, cHeight) ?: return

    val textBound = Rect()
    paint.getTextBounds(text, 0, text.length, textBound)
    val tx = max(bottomCenter.x!! - textBound.width() / 2, 0f)
    val ty = max(bottomCenter.y!! - 10, 1f)
    canvas.drawText(text, tx, ty, paint)
}

class ImageAnnotationTask(private val image: Bitmap,
                          private val annotations: List<LocalizedObjectAnnotation>,
                          private val callback: (Bitmap) -> Unit)
    : AsyncTask<Void, Void, Bitmap>() {
    override fun doInBackground(vararg p0: Void?): Bitmap {
        return drawAnnotations(image, annotations)
    }

    override fun onPostExecute(result: Bitmap?) {
        callback(result!!)
    }
}

fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {
    val ret = IntArray(4)

    if (imageView?.drawable == null)
        return ret

    // Get image dimensions
    // Get image matrix values and place them in an array
    val f = FloatArray(9)
    imageView.imageMatrix.getValues(f)

    // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
    val scaleX = f[Matrix.MSCALE_X]
    val scaleY = f[Matrix.MSCALE_Y]

    // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
    val d = imageView.drawable
    val origW = d.intrinsicWidth
    val origH = d.intrinsicHeight

    // Calculate the actual dimensions
    val actW = Math.round(origW * scaleX)
    val actH = Math.round(origH * scaleY)

    ret[2] = actW
    ret[3] = actH

    // Get image position
    // We assume that the image is centered into ImageView
    val imgViewW = imageView.width
    val imgViewH = imageView.height

    val top = (imgViewH - actH) / 2
    val left = (imgViewW - actW) / 2

    ret[0] = left
    ret[1] = top

    return ret
}
