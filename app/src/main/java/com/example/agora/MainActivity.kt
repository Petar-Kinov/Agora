package com.example.agora

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.agora.authentication.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

private lateinit var auth: FirebaseAuth

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var navController: NavController
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseHelper.getInstance()
        val user = auth.currentUser


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Check if user is signed in (non-null) and update UI accordingly.
        if (user != null) {
            navController.navigate(R.id.homePage)
        }

        authStateListener = FirebaseAuth.AuthStateListener {
            // if user = auth.currentUser is outside of the listener it does not change its value on authStateChange
            val user = auth.currentUser
            if (user != null) {
                // User is signed in
                Log.d("AuthStateListener", "onAuthStateChanged:signed_in:" + user.uid)
                navController.navigate(R.id.homePage)
            } else {
                // User is signed out
                Log.d("AuthStateListener", "onAuthStateChanged:signed_out")
                navController.navigate(R.id.loginFragment)
            }
        }
        auth.addAuthStateListener(authStateListener)

        this.onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            when (navController.currentDestination?.id) {
                R.id.homePage -> {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { _, _ -> this@MainActivity.finish() }
                        .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                    val alert = builder.create()
                    alert.show()
                }
                R.id.loginFragment -> {
                    this@MainActivity.finish()
                }
                else -> {
                    navController.popBackStack()
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
    }

}