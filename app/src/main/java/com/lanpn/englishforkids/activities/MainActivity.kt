package com.lanpn.englishforkids.activities

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.lanpn.englishforkids.R
import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import com.lanpn.englishforkids.handlers.GcpRawHandler
import com.lanpn.englishforkids.handlers.ImageHandler
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import android.support.v4.content.FileProvider
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.lanpn.englishforkids.views.drawAnnotations
import com.lanpn.englishforkids.views.loadSampledBitmap
import kotterknife.bindView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val REQUEST_CAPTURE_CODE = 1

class MainActivity : AppCompatActivity() {
    private var googleApiKey: String? = null
    private var imageHandler: ImageHandler? = null
    private var imagePath: String? = null

    val pictureButton: Button by bindView(R.id.button)
    val galleryButton: Button by bindView(R.id.button2)
    val progressBar: ProgressBar by bindView(R.id.progressBar)

    private fun dispatchPictureIntent() {
        if (PermissionUtils.requestPermission(this, REQUEST_CAPTURE_CODE,
                Manifest.permission.CAMERA)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                try {
                    val photoFile = createImageFile()
                    val photoUri = FileProvider.getUriForFile(this,
                            "com.lanpn.englishforkids.fileprovider",
                            photoFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, REQUEST_CAPTURE_CODE)
                } catch(e: IOException) {
                    Toast.makeText(this, "Cannot save image. Aborting", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    fun loadingOn() {
        pictureButton.isEnabled = false
        galleryButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    fun loadingOff() {
        pictureButton.isEnabled = true
        galleryButton.isEnabled = true
        progressBar.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE_CODE && resultCode == RESULT_OK) {
            val imageBitmap = loadSampledBitmap(imagePath!!, scale = 0.25f)
            imageHandler!!.handleImage(imageBitmap)
        }
    }

    private fun showImage(image: Bitmap, annotations: ArrayList<LocalizedObjectAnnotation>) {
        loadingOn()
        val decoratedImage = drawAnnotations(image, annotations)
        val decorPath = createImageFile("decorated")
        decoratedImage.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(decorPath))

        val intent = Intent(this, ImageViewActivity::class.java).apply {
            putExtra("imagePath", decorPath.absolutePath)
            putExtra("annotations", annotations)
        }
        loadingOff()

        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleApiKey = resources.getString(R.string.gcp_api_key)
        imageHandler = GcpRawHandler(googleApiKey!!, ::showImage)

        setContentView(R.layout.activity_main)
    }

    @Throws(IOException::class)
    private fun createImageFile(postfix: String = ""): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "PNG_" + timeStamp + "_" + postfix
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".png", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        imagePath = image.absolutePath
        return image
    }

    fun onTakePictureClick(view: View) {
        dispatchPictureIntent()
    }

    override fun onResume() {
        super.onResume()

    }

}
