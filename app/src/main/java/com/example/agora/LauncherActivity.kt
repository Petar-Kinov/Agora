package com.example.agora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.agora.util.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.SplashStyle)
        val decorView = window.decorView
        decorView.setBackgroundResource(R.drawable.splash_background)

        auth = FirebaseHelper.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = auth.currentUser
            Log.d("LauncherActivity", "onAuthStateChanged called: user=$user")
                    val intent = if (user != null) {
                        Intent(this@LauncherActivity, MainActivity::class.java)
                    } else {
                        Intent(this@LauncherActivity, LoginActivity::class.java)
                    }
                    startActivity(intent)
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
                    finish()
        }
        auth.addAuthStateListener(authStateListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authStateListener)
    }
}