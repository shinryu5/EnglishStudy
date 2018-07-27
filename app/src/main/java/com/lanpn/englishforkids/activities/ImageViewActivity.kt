package com.lanpn.englishforkids.activities

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.lanpn.englishforkids.R
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import kotterknife.bindView

@Suppress("UNCHECKED_CAST")
class ImageViewActivity : AppCompatActivity() {
    private var image: Bitmap? = null
    private var annotations: ArrayList<LocalizedObjectAnnotation>? = null

    val imageView: ImageView by bindView(R.id.imageView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        image = intent.getParcelableExtra("image")!!
        annotations = intent.getSerializableExtra("annotations") as ArrayList<LocalizedObjectAnnotation>

        imageView.setImageBitmap(image)
    }
}
