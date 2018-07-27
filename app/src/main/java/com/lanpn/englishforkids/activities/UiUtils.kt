package com.lanpn.englishforkids.activities

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.R.attr.visibility



fun setHiddenActionBar(activity: AppCompatActivity) {
    val decorView = activity.window.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = uiOptions

    decorView.setOnSystemUiVisibilityChangeListener {
        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            // The system bars are visible.
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        } else {
            // The system bars are NOT visible.
            decorView.systemUiVisibility = uiOptions
        }
    }
}
