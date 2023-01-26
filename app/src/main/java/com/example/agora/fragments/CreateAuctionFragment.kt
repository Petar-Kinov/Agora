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
import androidx.activity.result.launch
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
    private lateinit var imageName : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
        storage = Firebase.storage
        storageRef = storage.reference

        // onCreate is the latest you can registerForActivityResult
        pickMediaActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    val imageRef = storageRef.child(getFileName(requireActivity().contentResolver,uri)!!)
                    val somethingImageRef = storageRef.child("images/something.jpg")

                    // !! might be null
                    bitmap = getThumbnail(uri, requireContext())!!
                    binding.itemIV.setImageBitmap(bitmap)
                    imageName = getFileName(requireActivity().contentResolver,uri)!!

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

         cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()) { bitmap ->
             if (bitmap != null) {
                 //TODO set the storageReference to something

                 binding.itemIV.setImageBitmap(bitmap)
                 this.bitmap = bitmap
                 imageName = auth.currentUser!!.uid + LocalDateTime.now()

             } else {

             }


//        result: ActivityResult ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                //  you will get result here in result.data
//            }
        }

//        startForResult.launch(Intent(activity, CameraCaptureActivity::class.java))
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
                val item = Item(
                    seller = auth.currentUser?.displayName!!,
                    title = title,
                    description = description,
                    price = price,
                    downloadUrl = imageName
                )
//                viewModel.sellItem(item)
                uploadImage(seller = auth.currentUser?.displayName!!,
                    title = title,
                    description = description,
                    price = price,bitmap,storageRef.child(imageName))
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
        binding.cameraBtn.setOnClickListener {
            cameraActivityResultLauncher.launch()
        }
    }


    private fun uploadImage(seller: String, title: String,description: String, price:String ,bitmap: Bitmap, imageRef : StorageReference) {
        val baos = ByteArrayOutputStream()
        var  downloadUrl = ""

        lifecycleScope.launch(Dispatchers.IO) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnFailureListener {
                Log.d(TAG, "onViewCreated: Could not upload image")
            }.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful){
                        downloadUrl = it.result.toString()
                        val item = Item(seller,title,description,price,downloadUrl)
                        viewModel.sellItem(item)

                    }
                }
                Log.d(TAG, "onViewCreated: Successful upload of image ${taskSnapshot.metadata.toString()} "
                )
            }
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun getThumbnail(uri: Uri, context: Context): Bitmap? {
        var input: InputStream = context.contentResolver.openInputStream(uri)!!
        val onlyBoundsOptions = BitmapFactory.Options()
        onlyBoundsOptions.inJustDecodeBounds = true
        onlyBoundsOptions.inDither = true //optional
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
        bitmapOptions.inDither = true //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        input = context.getContentResolver().openInputStream(uri)!!
        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input.close()
        return bitmap
    }

    private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
        val k = Integer.highestOneBit(Math.floor(ratio).toInt())
        return if (k == 0) 1 else k
    }

//    @SuppressLint("Range")
//    fun getFileName(uri: Uri): String? {
//        var result: String? = null
//        if (uri.scheme == "content") {
//            val cursor: Cursor =
//                requireActivity().contentResolver.query(uri, null, null, null, null)
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//                }
//            } finally {
//                cursor.close()
//            }
//        }
//        if (result == null) {
//            result = uri.path
//            val cut = result!!.lastIndexOf('/')
//            if (cut != -1) {
//                result = result.substring(cut + 1)
//            }
//        }
//        return result
//    }

    private fun getFileName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}




