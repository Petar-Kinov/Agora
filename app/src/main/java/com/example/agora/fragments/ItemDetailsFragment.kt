package com.example.agora.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.adapters.PicturesListAdapter
import com.example.agora.databinding.FragmentItemDetailsBinding
import com.example.agora.model.Item
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ItemDetailsFragment : Fragment() {

    companion object {
        private const val TAG = "ItemDetailsFragment"
    }

    private var _binding : FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!

    private val args : ItemDetailsFragmentArgs by navArgs()
    private lateinit var item : Item
    private lateinit var uriList : ArrayList<Uri>

    private lateinit var recyclerView: RecyclerView
    private val recyclerAdapter = PicturesListAdapter{
        Log.d(TAG, "item: $it clicked")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = args.item
        uriList = arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentItemDetailsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        GlideApp.with(requireContext()).load(item.downloadUrl).into(binding.detailPictureIV)
        binding.titleTV.text = item.title
        binding.detailDescriptionTV.text = item.description
        binding.priceTextView.text = item.price
        binding.sellerTV.text = item.seller

        recyclerView = binding.imageRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL , false )
        recyclerView.adapter = recyclerAdapter

        getPictures(item.storageRef)
    }


    private fun getPictures(storageRef: String){
        val storagePathRef = Firebase.storage.getReference(storageRef)
        storagePathRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    item.downloadUrl.addOnSuccessListener {
                        uriList.add(it)
                        recyclerAdapter.submitList(uriList)
                    }

                }

            }
            .addOnFailureListener { e ->
                // Handle errors
            }
    }
}