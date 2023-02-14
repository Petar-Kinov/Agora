package com.example.agora.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.adapters.PictureBitmapListAdapter
import com.example.agora.databinding.FragmentCreateAuctionBinding
import com.example.agora.model.Item
import com.example.agora.viewModel.ItemsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import kotlin.math.floor

private const val TAG = "CreateAuctionFragment"
class CreateAuctionFragment : Fragment() {

    private var _binding: FragmentCreateAuctionBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var viewModel: ItemsViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var pickMediaActivityResultLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Void?>

    private lateinit var createBtn: Button
    private lateinit var recyclerView: RecyclerView
    private val recyclerAdapter = PictureBitmapListAdapter{
        bitmapList.removeAt(it)
        imagesCount--
    }

    private lateinit var bitmapList: ArrayList<Bitmap>
    private lateinit var storePathRef: String
    private var imagesCount: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
        storage = Firebase.storage
        storageRef = storage.reference
        storePathRef = ""
        bitmapList = arrayListOf()

        pickMediaActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uriList ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uriList != null) {
                    imagesCount += uriList.size
                    Log.d(TAG, "onCreate: $imagesCount")
                    for (uri in uriList) {
                        // !! might be null not sure
                        bitmapList.add(getThumbnail(uri, requireContext())!!)

                        //TODO load bitmap list into recycler view
//                        binding.itemIV.setImageBitmap(bitmap)

                        storePathRef = auth.currentUser!!.uid + LocalDateTime.now()
//                        imageName = getFileName(requireActivity().contentResolver, uri)!!
                    }

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
                recyclerAdapter.swapData(bitmapList)
            }

        // TODO when picture is from camera it is not uploaded
        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
               bitmapList.add(bitmap)
                imagesCount ++
                recyclerAdapter.swapData(bitmapList)
            } else {
                Log.d(TAG, "onCreate: bitmap is null ")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAuctionBinding.inflate(inflater, container, false)
        val view = binding.root
        createBtn = binding.createBtn
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.imageRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL , false )
        recyclerView.adapter = recyclerAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        createBtn.setOnClickListener {
            val title = binding.titleET.text.toString()
            val description = binding.descriptionET.text.toString()
            val price = binding.priceET.text.toString()
//            val imageRef = imageView.tag.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()) {
               viewModel.sellItem(
                    Item(
                        seller = auth.currentUser?.displayName!!,
                        title = title,
                        description = description,
                        price = price,
                        storageRef = storePathRef,
                        imagesCount = imagesCount
                    ), bitmapList = bitmapList
                )
                findNavController().popBackStack()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all the fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.fromStorageBtn.setOnClickListener {
            pickMediaActivityResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.fromCameraBtn.setOnClickListener {
            cameraActivityResultLauncher.launch()
        }
    }


    @Throws(FileNotFoundException::class, IOException::class)
    fun getThumbnail(uri: Uri, context: Context): Bitmap? {
        var input: InputStream = context.contentResolver.openInputStream(uri)!!
        val onlyBoundsOptions = BitmapFactory.Options()
        onlyBoundsOptions.inJustDecodeBounds = true
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
        input.close()
        if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
            return null
        }
        val originalSize =
            if (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) onlyBoundsOptions.outHeight else onlyBoundsOptions.outWidth
        val ratio = if (originalSize > 350) originalSize / 350.00 else 1.0
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio)
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        input = context.contentResolver.openInputStream(uri)!!
        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input.close()
        return bitmap
    }

    private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
        val k = Integer.highestOneBit(floor(ratio).toInt())
        return if (k == 0) 1 else k
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




