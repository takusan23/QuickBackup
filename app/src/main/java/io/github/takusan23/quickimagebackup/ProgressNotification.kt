package io.github.takusan23.quickimagebackup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager

class ProgressNotification(val context: Context) {

    companion object {
        val videoNotifyId = 845
        val photoNotifyId = 820
    }

    var pref_setting: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    lateinit var notification: NotificationCompat.Builder

    var notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    //一度に行うバックアップ件数
    val fileCount = pref_setting.getInt("photo_file_count", 3)

    //通知ちゃんねるのID
    val id = "backup_progress"

    fun showProgressNotification(contentText: String, notifyId: Int) {
        //面倒な通知チャンネル
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    id,
                    context.getString(R.string.backup_progress_notification),
                    NotificationManager.IMPORTANCE_LOW
                )
            if (notificationManager.getNotificationChannel(id) == null) {
                notificationManager.createNotificationChannel(channel)
            }
        }
        notification = NotificationCompat.Builder(context, id)
            .setSmallIcon(R.drawable.ic_folder_open_black_24dp)
            .setContentTitle(contentText)
            .setContentText("")
            .setProgress(fileCount, 0, true)
        //表示
        notificationManager.notify(notifyId, notification.build())
    }

    fun setProgress(progress: Int, fileCount: Int, notifyId: Int) {
        println(progress)
        //進捗設定
        notification.setContentText("${progress}/${fileCount}")
        notification.setProgress(fileCount, progress, false)
        //表示
        notificationManager.notify(notifyId, notification.build())
        if (fileCount == progress) {
            //終了通知
            var content = context.getString(R.string.finish_photo_backup)
            if (notifyId == videoNotifyId) {
                content = context.getString(R.string.finish_video_backup)
            }
            //終了したら消す？
            notification = NotificationCompat.Builder(context, id)
                .setSmallIcon(R.drawable.ic_folder_open_black_24dp)
                .setContentTitle(context.getString(R.string.finish_backup))
                .setContentText(content)
            //表示
            notificationManager.notify(notifyId, notification.build())
        }
    }

}