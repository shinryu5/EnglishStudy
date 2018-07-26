package com.lanpn.englishforkids.handlers

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1p2beta1.Vision
import com.google.api.services.vision.v1p2beta1.VisionRequestInitializer
import com.google.api.services.vision.v1p2beta1.model.GoogleCloudVisionV1p2beta1AnnotateImageRequest as AnnotateImageRequest
import com.google.api.services.vision.v1p2beta1.model.GoogleCloudVisionV1p2beta1BatchAnnotateImagesRequest as BatchAnnotateImagesRequest
import com.google.api.services.vision.v1p2beta1.model.GoogleCloudVisionV1p2beta1Feature as Feature
import com.google.api.services.vision.v1p2beta1.model.GoogleCloudVisionV1p2beta1Image as Image
import java.io.ByteArrayOutputStream

class GcpHandler(private val apiKey: String) : ImageHandler {
    class LabelDetectionTask(private val request: Vision.Images.Annotate) : AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg p0: Any?): String {
            val responses = request.execute()
//            if (responses.responses.size < 1) {
//                return ""
//            }

            val label = responses.responses[0]
            Log.d("GCP Handler", label.toString())

            return ""
        }

        override fun onPostExecute(result: String?) {

        }
    }

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
//        feature.type = "LABEL_DETECTION"
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
        val task = LabelDetectionTask(request)
        task.execute()
    }
}
