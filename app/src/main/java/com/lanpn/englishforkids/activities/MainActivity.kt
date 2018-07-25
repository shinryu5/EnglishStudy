package com.lanpn.englishforkids.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.lanpn.englishforkids.R
import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import kotterknife.bindView

const val REQUEST_CAPTURE_CODE = 1

class MainActivity : AppCompatActivity() {
    private val imageView: ImageView by bindView(R.id.imageView)
    private val startButton: Button by bindView(R.id.startButton)

    private fun dispatchPictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CAPTURE_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE_CODE && resultCode == RESULT_OK) {
            val extras = data!!.extras
            val imageBitmap = extras.get("data") as Bitmap

            handleImage(imageBitmap)
        }
    }

    private fun handleImage(image: Bitmap) {

    }

    private fun showImage(image: Bitmap) {
        startButton.visibility = View.INVISIBLE
        imageView.setImageBitmap(image)
        imageView.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun onStartClick(view: View) {
        dispatchPictureIntent()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onBackPressed() {
        if (imageView.visibility == View.VISIBLE) {
            imageView.visibility = View.INVISIBLE
            startButton.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}
