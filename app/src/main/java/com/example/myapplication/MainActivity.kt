package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Chuyển đến MenuActivity ngay khi khởi động
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}