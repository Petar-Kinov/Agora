package com.example.agora

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.data.core.model.User
import com.example.agora.data.messaging.model.ImageMessage
import com.example.agora.data.messaging.model.TextMessage
import com.example.agora.data.messaging.recyclerViewItem.MessageItem
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

private const val TAG = "ChatActivity"

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section
    private var recyclerView: RecyclerView? = null
    private lateinit var currentChannelId: String
    private lateinit var currentUser: User
    private lateinit var otherUserId: String
    private lateinit var otherUserName: String

    private lateinit var pickMediaActivityResultLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Void?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = binding.recyclerViewMessages

        otherUserId = intent.getStringExtra(AppConstants.USER_ID)!!
        otherUserName = intent.getStringExtra(AppConstants.USER_NAME)!!

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = otherUserName

        pickMediaActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    sendImageFromStorage(uri)
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                sendImageFromCamera(bitmap)
            } else {
                Log.d(TAG, "onCreate: No image was taken")
            }
        }

        FirestoreUtil.getCurrentUser {
            currentUser = it
        }

        FirestoreUtil.getOrCreateChatChannel(otherUserId, otherUserName) { channelId ->
            currentChannelId = channelId
            messagesListenerRegistration = FirestoreUtil.addChatMessageListener(
                channelId = channelId,
                context = this,
                this::updateRecyclerView
            )
        }

        binding.sendMessageBtn.setOnClickListener {
            if (!binding.editTextMessage.text.isNullOrEmpty()) {
                val messageToSend = TextMessage(
                    text = binding.editTextMessage.text.toString(),
                    time = Calendar.getInstance().time,
                    FirebaseHelper.getInstance().currentUser!!.uid,
                    otherUserId,
                    currentUser.username
                )
                binding.editTextMessage.setText("")
                FirestoreUtil.sendMessage(messageToSend, currentChannelId, otherUserId)
            }
        }

        binding.sendImageFromGaleryBtn.setOnClickListener {
            pickMediaActivityResultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.sendImageFromCameraBtn.setOnClickListener {
            cameraActivityResultLauncher.launch()
        }

        this.onBackPressedDispatcher.addCallback(this) {
            goBack()
        }
    }

    private fun sendImageFromCamera(bitmap: Bitmap) {
        val outPutStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outPutStream)
        val selectedImageBytes = outPutStream.toByteArray()

        StorageUtil.uploadMessageImage(selectedImageBytes) {
            val messageToSend = ImageMessage(
                imagePath = it,
                Calendar.getInstance().time,
                FirebaseAuth.getInstance().currentUser!!.uid,
                otherUserId,
                currentUser.username
            )
            FirestoreUtil.sendMessage(messageToSend, currentChannelId, otherUserId)
        }
    }

    private fun sendImageFromStorage(uri: Uri) {
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
                FirebaseAuth.getInstance().currentUser!!.uid,
                otherUserId,
                currentUser.username
            )
            FirestoreUtil.sendMessage(messageToSend, currentChannelId, otherUserId)
        }
    }

    //overriding the actionbar back button, otherwise it goes to the home destination of the parent activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                goBack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goBack() {
        val fragmentManager = supportFragmentManager
        val backStackEntryCount = fragmentManager.backStackEntryCount
        if (backStackEntryCount > 0) {
            // If there are fragments in the back stack, popBackStack
            fragmentManager.popBackStack()
        } else {
            // If there are no fragments in the back stack, start MainActivity and navigate to MessagesFragment
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            mainActivityIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            mainActivityIntent.putExtra("navigate_to_messages_fragment", true)
            startActivity(mainActivityIntent)
            finish()
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