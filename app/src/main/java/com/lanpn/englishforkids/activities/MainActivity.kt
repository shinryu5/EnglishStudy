package com.lanpn.englishforkids.activities

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.lanpn.englishforkids.R
import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.lanpn.englishforkids.handlers.GcpRawHandler
import com.lanpn.englishforkids.handlers.ImageHandler
import com.lanpn.englishforkids.models.LocalizedObjectAnnotation
import kotterknife.bindView

const val REQUEST_CAPTURE_CODE = 1

class MainActivity : AppCompatActivity() {

    private val googleApiKey = resources.getString(R.string.gcp_api_key)
    private val imageHandler: ImageHandler = GcpRawHandler(googleApiKey, ::showImage)

    private fun dispatchPictureIntent() {
        if (PermissionUtils.requestPermission(this, REQUEST_CAPTURE_CODE,
                Manifest.permission.CAMERA)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CAPTURE_CODE)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE_CODE && resultCode == RESULT_OK) {
            val extras = data!!.extras
            val imageBitmap = extras.get("data") as Bitmap
            imageHandler.handleImage(imageBitmap)
        }
    }

    private fun showImage(image: Bitmap, annotations: ArrayList<LocalizedObjectAnnotation>) {
        val intent = Intent(this, ImageViewActivity::class.java).apply {
            putExtra("image", image)
            putExtra("annotations", annotations)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setHiddenActionBar(this)
    }

    fun onTakePictureClick(view: View) {
        dispatchPictureIntent()
    }

    override fun onResume() {
        super.onResume()

    }

}
