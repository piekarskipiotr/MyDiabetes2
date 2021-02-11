package com.apps.bacon.mydiabetes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate.*

private const val DELAY: Long = 1000
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = this.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val theme = sharedPref.getInt("THEME", MODE_NIGHT_NO)
        setDefaultNightMode(theme)
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

        }, DELAY)

    }

    override fun onBackPressed() {
    }
}