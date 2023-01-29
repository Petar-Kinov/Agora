package com.example.agora.fragments

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import kotlin.math.floor


class CreateAuctionFragment : DialogFragment() {

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
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapList: ArrayList<Bitmap>
    private lateinit var imageName: String
    private var imageURIList = arrayListOf<String>()
    private lateinit var storePathRef: String
    private lateinit var imagesNames: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
        storage = Firebase.storage
        storageRef = storage.reference
        storePathRef = ""
        bitmapList = arrayListOf()
        imagesNames = arrayListOf()

        pickMediaActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uriList ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uriList != null) {
                    for (uri in uriList) {
                        // !! might be null not sure
                        bitmapList.add(getThumbnail(uri, requireContext())!!)
                        imagesNames.add(getFileName(requireActivity().contentResolver, uri)!!)

                        //TODO load bitmap list into recycler view
//                        binding.itemIV.setImageBitmap(bitmap)

                        storePathRef = auth.currentUser!!.uid + LocalDateTime.now()
//                        imageName = getFileName(requireActivity().contentResolver, uri)!!
                    }


                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        // TODO camera

//        cameraActivityResultLauncher = registerForActivityResult(
//            ActivityResultContracts.TakePicturePreview()
//        ) { bitmap ->
//            if (bitmap != null) {
//                binding.itemIV.setImageBitmap(bitmap)
//                this.bitmap = bitmap
//                imageName = auth.currentUser!!.uid + LocalDateTime.now()
//            } else {
//                Log.d(TAG, "onCreate: bitmap is null ")
//            }
//        }
    }

    companion object {
        private const val TAG = "CreateAuctionFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAuctionBinding.inflate(inflater, container, false)
        val view = binding.root
        createBtn = binding.createBtn
        imageView = binding.itemIV
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createBtn.setOnClickListener {
            val title = binding.titleET.text.toString()
            val description = binding.descriptionET.text.toString()
            val price = binding.priceET.text.toString()
//            val imageRef = imageView.tag.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()) {
                uploadImages(
                    seller = auth.currentUser?.displayName!!,
                    title = title,
                    description = description,
                    price = price,
                    bitmapList = bitmapList,
                    pathRef = storePathRef,
                    imagesNamesList = imagesNames
                )
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all the fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        imageView.setOnClickListener {
            pickMediaActivityResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        //TODO camera
//        binding.cameraBtn.setOnClickListener {
//            cameraActivityResultLauncher.launch()
//        }
    }

    private fun uploadImages(
        seller: String,
        title: String,
        description: String,
        price: String,
        bitmapList: ArrayList<Bitmap>,
        pathRef: String,
        imagesNamesList: ArrayList<String>
    ) {
        var baos = ByteArrayOutputStream()
        var downloadUrl: String

        var counter = 0

        lifecycleScope.launch(Dispatchers.IO) {

            for (i in 0 until bitmapList.size) {
                val baos = ByteArrayOutputStream()
                bitmapList[i].compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val uploadTask = storageRef.child(pathRef).child(imagesNamesList[i]).putBytes(data)
                uploadTask.addOnFailureListener {
                    Log.d(TAG, "onViewCreated: Could not upload image")
                }.addOnSuccessListener { taskSnapshot ->
                    counter++
//                    imageRef.downloadUrl.addOnCompleteListener {
//                        if (it.isSuccessful) {
//                            downloadUrl = it.result.toString()
//                            val item = Item(seller, title, description, price, downloadUrl)
//                            viewModel.sellItem(item)

                    Log.d(
                        TAG,
                        "onViewCreated: Successful upload of image ${taskSnapshot.metadata.toString()} "
                    )
                    if (counter == bitmapList.size) {
                        viewModel.sellItem(Item(seller, title, description, price, pathRef))
                    }
                }
            }
        }
//        counter = 0
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

    private fun getFileName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}




