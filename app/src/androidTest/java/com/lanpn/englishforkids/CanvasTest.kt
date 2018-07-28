package com.lanpn.englishforkids

import android.graphics.drawable.BitmapDrawable
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.lanpn.englishforkids.handlers.GcpRawHandler
import com.lanpn.englishforkids.views.drawAnnotations
import com.lanpn.englishforkids.views.scaleBitmap
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CanvasTest {
    @Test
    fun test_canvas_draw() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val key = appContext.resources.getString(R.string.gcp_api_key)
        val d = appContext.resources.getDrawable(R.mipmap.test) as BitmapDrawable
        val bitmap = scaleBitmap(d.bitmap, 0.5f)
        val b2 = d.bitmap
        var stop = false

        val handler = GcpRawHandler(key) { bm, annotations ->
            val newBm = drawAnnotations(bm, annotations)
            stop = true
        }
        handler.handleImage(bitmap)
        while (!stop) {}
    }
}