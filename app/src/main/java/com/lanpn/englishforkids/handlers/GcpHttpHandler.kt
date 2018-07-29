package com.lanpn.englishforkids.handlers

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.lanpn.englishforkids.models.AnnotationResponse
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import com.lanpn.englishforkids.models.SingleLocalizationRequest
import com.lanpn.englishforkids.utils.filterAnnotations
import java.io.ByteArrayOutputStream

class GcpHttpHandler(private val apiKey: String,
                     private val callback: (Bitmap, List<LocalizedObjectAnnotation>) -> Unit) : ImageHandler {
    private fun bitmapToBase64(bitmap: Bitmap) : String {
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
        return Base64.encodeToString(byteStream.toByteArray(), Base64.DEFAULT)
    }

    private fun constructRequestBody(base64: String) : String {
        val requestObj = SingleLocalizationRequest(base64)
        return Gson().toJson(requestObj)
    }

    override fun handleImage(image: Bitmap) {
        val b64 = bitmapToBase64(image)
        val requestBody = constructRequestBody(b64)

        val url = "https://vision.googleapis.com/v1p3beta1/images:annotate?key=$apiKey"
        Fuel.post(url).body(requestBody)
                .header(Pair("Accept", "application/json"))
                .header(Pair("Content-Type", "application/json"))
                .responseString { _, _, result ->
                    result.fold({ d ->
                        val responseObj: AnnotationResponse = Gson().fromJson(d, AnnotationResponse::class.java)
                        Log.d("Response", responseObj.toString())

                        if (responseObj.responses == null) {
                            callback(image, ArrayList())
                        } else {
                            if (responseObj.responses!!.isEmpty()) {
                                callback(image, ArrayList())
                            } else {
                                val annotations = responseObj.responses!![0].localizedObjectAnnotations
                                if (annotations == null) {
                                    callback(image, ArrayList())
                                } else {
                                    callback(image, filterAnnotations(annotations))
                                }
                            }
                        }
                    }, { err ->
                        Log.e("Fuel Error", err.message)
                        callback(image, ArrayList())
                    })
                }
    }

}