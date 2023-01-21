package com.example.agora

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

private lateinit var auth: FirebaseAuth

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        editor = sharedPref.edit()

//        editor.putBoolean("a",false)
        Log.d(TAG, "onViewCreated: shared pref is ${sharedPref.getBoolean("a",false)}")

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        if(currentUser != null){
//            reload();
//        }
    }

}