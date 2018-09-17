package com.lanpn.englishforkids.handlers

import android.graphics.Bitmap
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import java.io.ByteArrayOutputStream

class GcpClientHandler(private val apiKey: String,
                       private val callback: (Bitmap, List<LocalizedObjectAnnotation>) -> Unit) : ImageHandler {
    private fun prepareRequest(image: Bitmap): Vision.Images.Annotate {
        val httpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        val requestInitializer = VisionRequestInitializer(apiKey)

        val builder = Vision.Builder(httpTransport, jsonFactory, null)
        builder.setVisionRequestInitializer(requestInitializer)
        val vision = builder.build()

        // Construct request
        val annotateImageRequest = AnnotateImageRequest()
        val cImage = Image()
        val byteStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, byteStream)
        cImage.encodeContent(byteStream.toByteArray())
        annotateImageRequest.image = cImage

        val feature = Feature()
        feature.type = "OBJECT_LOCALIZATION"
        feature.maxResults = 10
        annotateImageRequest.features = listOf(feature)

        val requestBatch = BatchAnnotateImagesRequest()
        requestBatch.requests = listOf(annotateImageRequest)

        val request = vision.images().annotate(requestBatch)
        request.disableGZipContent = true
        return request
    }

    override fun handleImage(image: Bitmap) {
        val request = prepareRequest(image)
        val response = request.execute()
        val labels = response.responses[0]
    }

}

