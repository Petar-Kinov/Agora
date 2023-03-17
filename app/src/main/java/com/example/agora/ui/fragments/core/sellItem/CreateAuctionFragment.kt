package com.example.agora.ui.fragments.core.sellItem

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.R
import com.example.agora.data.core.model.Item
import com.example.agora.databinding.FragmentCreateAuctionBinding
import com.example.agora.domain.core.viewModel.ItemsViewModel
import com.example.agora.services.UploadService
import com.example.agora.ui.adapters.PictureBitmapListAdapter
import com.example.agora.util.ImageSource
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

class CreateAuctionFragment : Fragment() ,SelectImageSourceDialogFragment.OnImageSourceSelectedListener {

    private var _binding: FragmentCreateAuctionBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var viewModel: ItemsViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var pickMediaActivityResultLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Void?>
    private val args : CreateAuctionFragmentArgs by navArgs()


    private var downloadUrl: Uri? = null
    private var fileUri: Uri? = null
    private var uriList: MutableList<Uri> = mutableListOf()

    private lateinit var createBtn: Button
    private lateinit var recyclerView: RecyclerView
    private val recyclerAdapter = PictureBitmapListAdapter {
        bitmapList.removeAt(it)
        uriList.removeAt(it)
        imagesCount--
        if (uriList.size == 0){
            binding.addImageBtn.visibility = ImageButton.VISIBLE
        }
    }

    private lateinit var bitmapList: ArrayList<Bitmap>
    private var imagesCount: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
        storage = Firebase.storage
        storageRef = storage.reference
        bitmapList = arrayListOf()


        pickMediaActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uriList ->
                if (uriList != null) {
                    imagesCount += uriList.size
                    Log.d(TAG, "onCreate: $imagesCount")
                    for (uri in uriList) {
                        this.uriList.add(uri)
                        bitmapList.add(getThumbnail(uri, requireContext())!!)
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
                updateRecyclerView(bitmapList)
            }

        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                bitmapList.add(bitmap)
                imagesCount++

                // Convert bitmap to JPEG and insert into MediaStore
                savePicture(bitmap)
                updateRecyclerView(bitmapList)
            } else {
                Log.d(TAG, "onCreate: bitmap is null")
            }
        }
    }

    private fun updateRecyclerView(bitmapList : List<Bitmap>) {
        recyclerAdapter.swapData(bitmapList)
        if (bitmapList.isNotEmpty()){
            binding.addImageBtn.visibility = ImageButton.GONE
        }

    }

    private fun savePicture(bitmap: Bitmap) {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            uriList.add(imageUri)
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

        binding.categoryTV.text = args.category

        recyclerView = binding.imageRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
        recyclerView.adapter = recyclerAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        createBtn.setOnClickListener {
            createItem()
        }

        binding.addImageBtn.setOnClickListener {
            // create dialog fragment to choose image source
            val imageSourceDialogFragment = SelectImageSourceDialogFragment()
            imageSourceDialogFragment.listener = this
            imageSourceDialogFragment.show(childFragmentManager,"image_source_dialog_fragment")
        }
    }

    private fun createItem() {
        val title = binding.titleET.text.toString()
        val description = binding.descriptionET.text.toString()
        val price = binding.priceET.text.toString()

        val storageRef = auth.currentUser!!.uid + LocalDateTime.now()

        if (title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()&& imagesCount > 0) {
            val item = Item(
                seller = auth.currentUser?.displayName!!,
                sellerId = auth.currentUser?.uid.toString(),
                title = title,
                description = description,
                category = args.category,
                price = price,
                storageRef = storageRef,
                imagesCount = imagesCount
            )
            // Uploads the image to storage
            Log.d(TAG, "onViewCreated: item is $item with ${item.imagesCount} images")
            uploadFromUri(item, uriList)
            findNavController().popBackStack(R.id.homePage, false)
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill in all the fields",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onImageSourceSelected(source: ImageSource) {
        when (source) {
            ImageSource.GALLERY -> {
                pickMediaActivityResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            ImageSource.CAMERA -> {
                cameraActivityResultLauncher.launch()
            }
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

    private fun uploadFromUri(item: Item, uploadUriList: List<Uri>) {
        Log.d(TAG, "uploadFromUri:src: $uploadUriList")

        // Clear the last download, if any
//        updateUI(auth.currentUser)
//        downloadUrl = null

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        requireActivity().startService(
            Intent(requireContext(), UploadService::class.java)
                .putParcelableArrayListExtra(UploadService.URI_LIST, ArrayList(uploadUriList))
                .putExtra(UploadService.ITEM, item)
                .setAction(UploadService.ACTION_UPLOAD)
        )

        // Show loading spinner
//        showProgressBar(getString(R.string.progress_uploading))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}




