package com.example.reelsapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.reelsapplication.reels.ReelActivity1

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.reel1).setOnClickListener {
            val intent = Intent(this,ReelActivity1::class.java)
            startActivity(intent)
        }
    }
}