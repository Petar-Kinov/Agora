package com.example.agora.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.agora.R

 abstract class BaseTaskService : Service() {
    private var numTasks = 0

    private val manager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun taskStarted() {
        changeNumberOfTasks(1)
    }

    fun taskCompleted() {
        changeNumberOfTasks(-1)
    }

    @Synchronized
    private fun changeNumberOfTasks(delta: Int) {
        Log.d(TAG, "changeNumberOfTasks:$numTasks:$delta")
        numTasks += delta

        // If there are no tasks left, stop the service
        if (numTasks <= 0) {
            Log.d(TAG, "stopping")
            stopSelf()
        }
    }

    private fun createDefaultChannel() {
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID_DEFAULT,
                "Default",
                NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification with a progress bar.
     */
    protected fun showProgressNotification(caption: String, completedUnits: Long, totalUnits: Long) {
        var percentComplete = 0
        if (totalUnits > 0) {
            percentComplete = (100 * completedUnits / totalUnits).toInt()
        }

        createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.info_icon)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(caption)
            .setProgress(100, percentComplete, false)
            .setOngoing(true)
            .setAutoCancel(false)

        manager.notify(PROGRESS_NOTIFICATION_ID, builder.build())
    }

    /**
     * Show notification that the activity finished.
     */
    protected fun showFinishedNotification(caption: String, intent: Intent, success: Boolean) {
        // Make PendingIntent for notification

        // Make PendingIntent for notification
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(this, 0 /* requestCode */, intent,
            flag)

        val icon = if (success) R.drawable.agora_icon else R.drawable.delete_icon

        createDefaultChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(icon)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(caption)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(FINISHED_NOTIFICATION_ID, builder.build())
    }

    /**
     * Dismiss the progress notification.
     */
    protected fun dismissProgressNotification() {
        manager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    companion object {

        private const val CHANNEL_ID_DEFAULT = "default"

        internal const val PROGRESS_NOTIFICATION_ID = 0
        internal const val FINISHED_NOTIFICATION_ID = 1

        private const val TAG = "MyBaseTaskService"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}