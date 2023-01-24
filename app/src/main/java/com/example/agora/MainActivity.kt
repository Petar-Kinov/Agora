package com.example.agora

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.agora.fragments.HomePage
import com.example.agora.fragments.LoginFragment
import com.example.agora.fragments.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

private lateinit var auth: FirebaseAuth

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Check if user is signed in (non-null) and update UI accordingly.
        if (user != null) {
            navController.navigate(R.id.homePage)
        }


    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)

        if (navHostFragment != null) {
            if (navHostFragment.childFragmentManager.fragments.get(0) is HomePage) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ -> this.finish() }
                    .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                val alert = builder.create()
                alert.show()
            } else if (navHostFragment.childFragmentManager.fragments[0] is SettingsFragment) {

                navController.popBackStack(R.id.homePage, false)
            } else if (navHostFragment.childFragmentManager.fragments[0] is LoginFragment)
                this.finish()
        }
        // else super.onBackPressed to cover all other options

    }
}