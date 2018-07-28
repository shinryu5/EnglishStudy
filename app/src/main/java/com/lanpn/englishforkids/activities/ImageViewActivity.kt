package com.lanpn.englishforkids.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import com.lanpn.englishforkids.R
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import com.lanpn.englishforkids.models.Vertex
import com.lanpn.englishforkids.views.loadSampledBitmap
import kotterknife.bindView
import java.util.*

@Suppress("UNCHECKED_CAST")
class ImageViewActivity : AppCompatActivity() {
    private var imagePath: String? = null
    private var annotations: ArrayList<LocalizedObjectAnnotation>? = null
    private var textToSpeech: TextToSpeech? = null

    val imageView: ImageView by bindView(R.id.imageView)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        imagePath = intent.getStringExtra("imagePath")!!
        val image = BitmapFactory.decodeFile(imagePath)

        annotations = intent.getSerializableExtra("annotations") as ArrayList<LocalizedObjectAnnotation>
        annotations!!.sortBy { it.boundingPoly?.diameter }

        imageView.setImageBitmap(image)
        imageView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val x = motionEvent.x
                val y = motionEvent.y
                Log.d("Position", Vertex(x, y).toString())

                for (annotation in annotations!!) {
                    if (annotation.boundingPoly!!.isInside(x, y, image.width.toFloat(), image.height.toFloat())) {
                        textToSpeech?.speak(annotation.name, TextToSpeech.QUEUE_FLUSH, null)
                        break
                    }
                }

            }
            return@setOnTouchListener true
        }
    }

    override fun onStart() {
        // Set up TTS
        textToSpeech = TextToSpeech(this) {
            textToSpeech?.language = Locale.US
        }
        super.onStart()
    }

    override fun onStop() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onStop()
    }
}
