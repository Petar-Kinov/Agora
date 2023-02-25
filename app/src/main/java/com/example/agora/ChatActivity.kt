package com.example.agora

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.data.Messaging.Model.ImageMessage
import com.example.agora.data.Messaging.Model.MessageType
import com.example.agora.data.Messaging.Model.TextMessage
import com.example.agora.data.Messaging.recyclerViewItem.MessageItem
import com.example.agora.databinding.ActivityChatBinding
import com.example.agora.util.AppConstants
import com.example.agora.util.FirebaseHelper
import com.example.agora.util.FirestoreUtil
import com.example.agora.util.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import java.io.ByteArrayOutputStream
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section
    private var recyclerView: RecyclerView? = null
    private lateinit var currentChannelId: String
    private lateinit var pickMediaActivityResultLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Void?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = binding.recyclerViewMessages

        pickMediaActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val selectedImageBmp = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            this.contentResolver,
                            uri
                        )
                    )
                    val outPutStream = ByteArrayOutputStream()
                    selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outPutStream)
                    val selectedImageBytes = outPutStream.toByteArray()

                    StorageUtil.uploadMessageImage(selectedImageBytes) {
                        val messageToSend = ImageMessage(
                            imagePath = it,
                            Calendar.getInstance().time,
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        FirestoreUtil.sendMessage(messageToSend, currentChannelId)
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        //TODO send pictures from camera
        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                val outPutStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outPutStream)
                val selectedImageBytes = outPutStream.toByteArray()

                StorageUtil.uploadMessageImage(selectedImageBytes) {
                    val messageToSend = ImageMessage(
                        imagePath = it,
                        Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid
                    )
                    FirestoreUtil.sendMessage(messageToSend, currentChannelId)
                }
            } else {
                // No image was taken
            }
        }

        //TODO the back button should go to People fragment instead of the home destination of the MainActivity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)
        FirestoreUtil.getOrCreateChatChannel(otherUserId!!) { channelId ->
            currentChannelId = channelId
            messagesListenerRegistration = FirestoreUtil.addChatMessageListener(
                channelId = channelId,
                context = this,
                this::updateRecyclerView
            )

            binding.sendMessageBtn.setOnClickListener {
                if (!binding.editTextMessage.text.isNullOrEmpty()){
                    val messageToSend = TextMessage(
                        text = binding.editTextMessage.text.toString(),
                        time = Calendar.getInstance().time,
                        FirebaseHelper.getInstance().currentUser!!.uid,
                        type = MessageType.TEXT
                    )
                    binding.editTextMessage.setText("")
                    FirestoreUtil.sendMessage(messageToSend, channelId)
                }
            }
        }

        binding.sendImageFromGaleryBtn.setOnClickListener {
            pickMediaActivityResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.sendImageFromCameraBtn.setOnClickListener {
            cameraActivityResultLauncher.launch()
        }
    }

    private fun updateRecyclerView(messages: List<MessageItem<*>>) {
        fun init() {
            recyclerView!!.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupieAdapter().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView) {
            init()
        } else {
            updateItems()
        }
        recyclerView!!.scrollToPosition(recyclerView!!.adapter!!.itemCount.minus(1))
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView?.adapter = null
        recyclerView = null
        FirestoreUtil.removeListener(messagesListenerRegistration)
    }
}