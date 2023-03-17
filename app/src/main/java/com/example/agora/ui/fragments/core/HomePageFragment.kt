package com.example.agora.ui.fragments.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.agora.R
import com.example.agora.data.authentication.login.LoginViewModelFactory
import com.example.agora.databinding.FragmentHomePageBinding
import com.example.agora.domain.auth.viewModel.AuthViewModel
import com.example.agora.ui.adapters.ViewPagerAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "HomePage"

class HomePage : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private var viewPager: ViewPager2? = null
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(requireActivity(), LoginViewModelFactory())[AuthViewModel::class.java]
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Snackbar.make(requireView(),"No notifications will be shown", Snackbar.LENGTH_LONG)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore

        Log.d(TAG, "onCreate: activity is $activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        val view = binding.root

//        val name = auth.currentUser?.displayName
//        binding.welcomeMsg.text = getString(R.string.welcome_msg, name)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        askNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
        })

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Installation ID: " + task.result)
            } else {
                Log.e(TAG, "Unable to get Installation ID")
            }
        }


        viewPager = binding.viewPager
        viewPager!!.adapter = ViewPagerAdapter(childFragmentManager,viewLifecycleOwner.lifecycle)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0) {
                tab.text = getString(R.string.auctions)
            } else {
                tab.text = getString(R.string.my_items)
            }
        }.attach()

        binding.popupMenuBtn.setOnClickListener {
            binding.popupMenuBtn.isEnabled = false
            val popupMenu = PopupMenu(requireContext(), it)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.pop_up_menu, popupMenu.menu)

            popupMenu.menu.setGroupEnabled(0,false)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.homeBtn -> Toast.makeText(
                        requireContext(),
                        "You clicked ${item.title}",
                        Toast.LENGTH_LONG
                    ).show()
                    R.id.messagesBtn -> {
                        val action = HomePageDirections.actionHomePageToChatsFragment()
                        findNavController().navigate(action)
                    }
                    R.id.settings -> {
                        val action = HomePageDirections.actionHomePageToSettingsFragment()
                        findNavController().navigate(action)
                    }
                    R.id.logout -> logOut()
                }
                true
            }
            popupMenu.setOnDismissListener {
                popupMenu.menu.setGroupEnabled(0,false)
                binding.popupMenuBtn.isEnabled = true
            }
            popupMenu.show()

            // Enable the menu items after a short delay to avoid
            // accidental clicks during enter animations
            Handler(Looper.getMainLooper()).postDelayed({
                popupMenu.menu.setGroupEnabled(0, true)
            }, 500)
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "askNotificationPermission: Permission should be granted")
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Log.d(TAG, "askNotificationPermission: It should ask for permission")
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Log.d(TAG, "askNotificationPermission: It should really ask for permission by now")
            }
        }
    }

    private fun logOut() {
        authViewModel.logout()
        val action = HomePageDirections.actionHomePageToLoginActivity()
        findNavController().navigate(action)
        requireActivity().viewModelStore.clear()
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager?.adapter = null
        viewPager = null
        _binding = null
    }
}