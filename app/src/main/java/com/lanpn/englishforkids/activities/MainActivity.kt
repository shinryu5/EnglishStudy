package com.lanpn.englishforkids.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.lanpn.englishforkids.R
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import com.lanpn.englishforkids.handlers.GcpHttpHandler
import com.lanpn.englishforkids.handlers.ImageHandler
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.github.clans.fab.FloatingActionMenu
import com.lanpn.englishforkids.utils.*
import kotterknife.bindView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


const val REQUEST_CAPTURE_CODE = 1
const val REQUEST_GALLERY_CODE = 2

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var googleApiKey: String? = null
    private var imageHandler: ImageHandler? = null
    private var imagePath: String? = null
    private var textToSpeech: TextToSpeech? = null
    private var annotations = emptyList<LocalizedObjectAnnotation>()
    private var imageBitmap: Bitmap? = null
    private var decoratedBitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private val progressBar: ProgressBar by bindView(R.id.progressBar)
    private val imageView: ImageView by bindView(R.id.imageView2)
    private val floatMenu: FloatingActionMenu by bindView(R.id.floatMenu)

    private fun dispatchPictureIntent() {
        if (PermissionUtils.requestPermission(this, REQUEST_CAPTURE_CODE,
                Manifest.permission.CAMERA)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                try {
                    val photoFile = createImageFile()
                    imagePath = photoFile.absolutePath
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

    private fun dispatchGalleryIntent() {
        if (PermissionUtils.requestPermission(this, REQUEST_GALLERY_CODE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY_CODE)
        }
    }

    private fun loadingOn() {
        imageView.visibility = View.GONE
        floatMenu.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    private fun loadingOff() {
        floatMenu.isEnabled = true
        floatMenu.close(true)
        progressBar.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loadingOn()
        AsyncTask.execute {
            var stop = false
            if (requestCode == REQUEST_CAPTURE_CODE && resultCode == RESULT_OK) {
                imageBitmap = loadSampledBitmap(imagePath!!)
            } else if (requestCode == REQUEST_GALLERY_CODE && resultCode == RESULT_OK) {
                imagePath = getPath(this, data?.data!!)
                if (imagePath != null) {
                    imageBitmap = loadSampledBitmap(imagePath!!)
                } else {
                    stop = true
                }
            }

            if ((imageBitmap != null) or stop)
                imageHandler!!.handleImage(imageBitmap!!)
            else {
                runOnUiThread {
                    loadingOff()
                }
            }
        }

    }

    private fun showImage(image: Bitmap, annotations: List<LocalizedObjectAnnotation>) {
        this.annotations = annotations.sortedBy { it.boundingPoly?.diameter }
        val task = ImageAnnotationTask(image, annotations) {
            loadingOff()
            if (annotations.isEmpty()) {
                Toast.makeText(this, "No objects found in image", Toast.LENGTH_LONG).show()
            }
            imageView.setImageBitmap(it)
            imageBitmap = it
            decoratedBitmap = mutableBitmap(imageBitmap!!)
            canvas = Canvas(decoratedBitmap)
            imageView.visibility = View.VISIBLE
        }
        task.execute()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleApiKey = resources.getString(R.string.gcp_api_key)
        imageHandler = GcpHttpHandler(googleApiKey!!, ::showImage)

        setContentView(R.layout.activity_main)

        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        highlightPaint.color = Color.rgb(56, 121, 226)
        highlightPaint.style = Paint.Style.FILL_AND_STROKE

        imageView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    val (left, top, width, height) = getBitmapPositionInsideImageView(imageView)
                    val x = motionEvent.x - left
                    val y = motionEvent.y - top

                    // Check if the user clicked on an object
                    for (annotation in annotations) {
                        if (annotation.boundingPoly!!.isInside(x, y, width.toFloat(), height.toFloat())) {
                            // Activate TTS with option to use network model
                            // instead of embedded model
                            textToSpeech?.speak(annotation.name, TextToSpeech.QUEUE_FLUSH,
                                    HashMap(mapOf(
                                    Pair(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true")
                            )))
                            // Hightlight the object
                            highlightPaint.strokeWidth = 0.01f * imageBitmap!!.width
                            drawPoly(canvas!!, highlightPaint, annotation.boundingPoly!!)
                            imageView.setImageBitmap(decoratedBitmap)

                            view.invalidate()
                            break
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    try {
                        // Had to do this because restore() won't fucking work
                        imageView.setImageBitmap(imageBitmap)
                        decoratedBitmap = mutableBitmap(imageBitmap!!)
                        canvas = Canvas(decoratedBitmap)
                        view.invalidate()
                    } catch (e: IllegalStateException) {

                    }
                }
                else -> {}
            }

            return@setOnTouchListener true
        }
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
        return image
    }

    fun onTakePictureClick(view: View) {
        dispatchPictureIntent()
    }

    fun onGalleryClick(view: View) {
        dispatchGalleryIntent()
    }

    override fun onBackPressed() {
        if (imageView.visibility == View.GONE)
            super.onBackPressed()
        else {
            releaseImage()
        }
    }

    private fun releaseImage() {
        imageView.visibility = View.GONE
        annotations = emptyList()
        imageBitmap = null
        decoratedBitmap = null
        canvas = null
    }

    override fun onStart() {
        // Set up TTS
        textToSpeech = TextToSpeech(this) {
            textToSpeech?.language = Locale.US
        }
        super.onStart()
    }

    override fun onStop() {
        releaseImage()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onStop()
    }
}
