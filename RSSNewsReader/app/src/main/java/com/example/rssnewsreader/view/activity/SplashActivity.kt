package com.example.rssnewsreader.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rssnewsreader.R
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        activityScope.launch {
            delay(1300)

            val intent = Intent(this@SplashActivity, NewsListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        activityScope.cancel()
        super.onPause()
    }
}
