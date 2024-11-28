package com.activityGuard.confuseapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     setContentView(R.layout.activity_main2)
        val view = findViewById<View>(R.id.main)
        println("view ----- " + view.tag)
    }
}