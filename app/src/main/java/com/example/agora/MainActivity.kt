package com.example.agora

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.agora.util.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

private lateinit var auth: FirebaseAuth

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        auth = FirebaseHelper.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navigateToMessagesFragment = intent.getBooleanExtra("navigate_to_messages_fragment", false)

        if (navigateToMessagesFragment) {
            navController.navigate(R.id.chatsFragment)
        }

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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Main Activity was destroyed")
        Log.d(TAG, "onDestroy: current user is ${FirebaseAuth.getInstance().currentUser.toString()}")
    }
}