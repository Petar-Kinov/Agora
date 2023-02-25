package com.example.agora.ui.fragments.core

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.R
import com.example.agora.data.core.model.Item
import com.example.agora.databinding.FragmentItemDetailsBinding
import com.example.agora.ui.adapters.PicturesUriListAdapter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

private const val TAG = "ItemDetailsFragment"
class ItemDetailsFragment : Fragment() {

    private var _binding : FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!

    private val args : ItemDetailsFragmentArgs by navArgs()
    private lateinit var item : Item
    private lateinit var uriList : ArrayList<Uri>

    private lateinit var recyclerView: RecyclerView
    private val recyclerAdapter = PicturesUriListAdapter{
        Log.d(TAG, "item: $it clicked")
    }

    private lateinit var pictureIndexTV : TextView

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
        binding.titleTV.text = item.title
        binding.detailDescriptionTV.text = item.description
        binding.priceTextView.text = item.price
        binding.sellerTV.text = item.seller

        pictureIndexTV = binding.picIndexTV
        pictureIndexTV.text = getString(R.string.picture_index,1 ,item.imagesCount)

        recyclerView = binding.imageRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL , false )
        recyclerView.adapter = recyclerAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        getPictures(item.storageRef,item.imagesCount)

        // shows which picture is being shown out of how many pictures there are
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                pictureIndexTV.text = getString(R.string.picture_index,firstVisibleItemPosition + 1 ,item.imagesCount)
            }
    })
    }

// gets the uri of every picture in the storageRef and sends the list to the adapter
    private fun getPictures(storageRef: String, imagesCount : Int){
        val storagePathRef = Firebase.storage.getReference(storageRef)
        storagePathRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    item.downloadUrl.addOnSuccessListener {
                        uriList.add(it)
                        if (uriList.size == imagesCount){
                            uriList.sort()
                            recyclerAdapter.submitList(uriList)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle errors
                Log.d(TAG, "getPictures: failureListener with error  $e")
            }
    }
}