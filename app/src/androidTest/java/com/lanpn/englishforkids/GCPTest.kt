package com.lanpn.englishforkids

import android.graphics.drawable.BitmapDrawable
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.lanpn.englishforkids.handlers.GcpClientHandler
import com.lanpn.englishforkids.handlers.GcpHttpHandler
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GCPTest {
    @Test
    fun test_api_call() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val key = appContext.resources.getString(R.string.gcp_api_key)
        val d = appContext.resources.getDrawable(R.mipmap.test_small) as BitmapDrawable
        val bitmap = d.bitmap
        var stop = false

        val handler = GcpHttpHandler(key) { _, annotations ->
            Log.d("Test", annotations.toString())
            stop = true
        }
        handler.handleImage(bitmap)
        while (!stop) {}
    }
}