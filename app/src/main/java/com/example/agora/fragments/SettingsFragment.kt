package com.example.agora.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.agora.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    companion object {
        private const val TAG = "SettingsFragment"
    }

    private var _binding : FragmentSettingsBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        val view = binding.root

        binding.deleteAccBtn.setOnClickListener {

            auth.currentUser!!.delete().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(requireContext(), "User Deleted", Toast.LENGTH_SHORT).show()
                    val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
                    findNavController().navigate(action)
                }
            }
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}