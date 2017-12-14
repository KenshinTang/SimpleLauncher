package com.kapplication.launcher

import android.app.Activity
import android.os.Bundle
import android.widget.Button

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(Button(this))
    }
}
