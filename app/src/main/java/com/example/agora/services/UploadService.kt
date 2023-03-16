package com.example.agora.services

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.agora.MainActivity
import com.example.agora.R
import com.example.agora.data.core.model.Item
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage

class UploadService : BaseTaskService() {

    private lateinit var storageRef: StorageReference

    override fun onCreate() {
        super.onCreate()

        storageRef = Firebase.storage.reference
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")

        if (ACTION_UPLOAD == intent.action) {

            val item = intent.getParcelableExtra<Parcelable>(ITEM) as Item


            val uriList =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ({
//                intent.getParcelableExtra(URI_LIST, ArrayList::class.java)
//            }) as ArrayList<Uri> else {
                intent.getParcelableArrayListExtra<Parcelable>(URI_LIST) as ArrayList<Uri>
//            }


//            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                intent.getParcelableExtra(EXTRA_FILE_URI, Uri::class.java)!!
//            } else {
//                intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)!!
//            }


            // Make sure we have permission to read the data
//            contentResolver.takePersistableUriPermission(
//                fileUri,
//                Intent.FLAG_GRANT_READ_URI_PERMISSION
//            )

            uploadFilesFromUri(item, uriList)

//            uploadFromUri(fileUri,storageRef)
        }

        return START_REDELIVER_INTENT
    }

    private fun uploadFilesFromUri(item : Item, uriList: List<Uri>) {
        Log.d(TAG, "uploadFromUri:src:$uriList")

        taskStarted()
        showProgressNotification(getString(R.string.progress_uploading), 0, 0)

        var uploadCount = 0

        // pictures are uploaded to firebase storage with their list index as their name
        for ((index, fileUri) in uriList.withIndex()) {

            Log.d(TAG, "uploadFilesFromUri: storageRef is $storageRef")
                val photoRef = storageRef.child("items").child(item.storageRef)
                    .child(index.toString())
                // [END get_child_ref]

            // Upload file to Firebase Storage
                Log.d(TAG, "uploadFromUri:dst:" + photoRef.path)
                photoRef.putFile(fileUri)
                    .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                        showProgressNotification(
                            getString(R.string.project_id),
                            bytesTransferred,
                            totalByteCount
                        )
                    }.continueWithTask { task ->
                    // Forward any exceptions
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }

                    Log.d(TAG, "uploadFromUri: upload success")

                    // Request the public download URL
                    photoRef.downloadUrl
                }.addOnSuccessListener { downloadUri ->
                    // Upload succeeded
                    Log.d(TAG, "uploadFromUri: getDownloadUri success")

                    // [START_EXCLUDE]
                        uploadCount++

                        if (uploadCount == uriList.size) {
                            uploadItemToFirestore(item)
                        }

                    broadcastUploadFinished(downloadUri, fileUri)
                    showUploadFinishedNotification(downloadUri, fileUri)
                    taskCompleted()
                    // [END_EXCLUDE]
                }.addOnFailureListener { exception ->
                    // Upload failed
                    Log.w(TAG, "uploadFromUri:onFailure", exception)

                    // [START_EXCLUDE]
//                broadcastUploadFinished(null, fileUri)
//                showUploadFinishedNotification(null, fileUri)
//                taskCompleted()
                    // [END_EXCLUDE]
                }

        }
    }

    private fun uploadItemToFirestore(item : Item) {
        Firebase.firestore.collection("items").add(item).addOnSuccessListener {
            Log.d(TAG, "uploadItemToFirestore: Uploaded item successfully to firestore")
        }
    }


//        val data = intent?.clipData?.getItemAt(0)?.uri
//
//        val storageRef = Firebase.storage.reference.child("uploads/${data?.lastPathSegment}")
//        val uploadTask = storageRef.putFile(data!!)
//
//        val notification = Notification.Builder(this, "CHANEL_ID")
//            .setContentTitle("uploading Photos")
//            .setContentText("Please wait...")
//            .build()
//
//        startForeground(1,notification)
//
//        // Set up listeners to monitor the upload progress
//        uploadTask.addOnSuccessListener {
//            // Handle successful upload
//            stopForeground(STOP_FOREGROUND_DETACH)
//            stopSelf()
//        }.addOnFailureListener {
//            // Handle failed upload
//            stopForeground(STOP_FOREGROUND_DETACH)
//            stopSelf()
//        }
//
//        return START_NOT_STICKY
//    }


    private fun broadcastUploadFinished(downloadUrl: Uri?, fileUri: Uri?): Boolean {
        val success = downloadUrl != null

        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcast = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
        return LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcast)
    }

    private fun showUploadFinishedNotification(downloadUrl: Uri?, fileUri: Uri?) {
        // Hide the progress notification
        dismissProgressNotification()

        // Make Intent to MainActivity
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = downloadUrl != null
        val caption =
            if (success) getString(R.string.upload_success) else getString(R.string.upload_failure)
        showFinishedNotification(caption, intent, success)
    }

    companion object {

        private const val TAG = "UploadService"

        /** Intent Actions  */
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        //
//        /** Intent Extras  */
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"
        const val DOCUMENT_REF = "document_ref"
        const val URI_LIST = "uri_list"
        const val ITEM = "item"
//
//        val intentFilter: IntentFilter
//            get() {
//                val filter = IntentFilter()
//                filter.addAction(UPLOAD_COMPLETED)
//                filter.addAction(UPLOAD_ERROR)
//
//                return filter
//            }
    }

}