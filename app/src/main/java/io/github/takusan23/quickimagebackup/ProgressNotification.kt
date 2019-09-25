package io.github.takusan23.quickimagebackup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager

class ProgressNotification(val context: Context) {
    var pref_setting: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    lateinit var notification: NotificationCompat.Builder

    var notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    //一度に行うバックアップ件数
    val fileCount = pref_setting.getInt("file_count", 3)

    //通知ちゃんねるのID
    val id = "backup_progress"

    fun showProgressNotification() {
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
            .setContentTitle(context.getString(R.string.backup_progress_notification_text))
            .setContentText("")
            .setProgress(fileCount, 0, false)
        //表示
        notificationManager.notify(845, notification.build())
    }

    fun setProgress(progress: Int) {
        println(progress)
        //進捗設定
        notification.setContentText("${progress}/${fileCount}")
        notification.setProgress(fileCount, progress, false)
        //表示
        notificationManager.notify(845, notification.build())
        if (fileCount == progress) {
            //終了したら消す？
            notification = NotificationCompat.Builder(context, id)
                .setSmallIcon(R.drawable.ic_folder_open_black_24dp)
                .setContentTitle(context.getString(R.string.finish_backup))
                .setContentText(context.getString(R.string.finish_backup))
            //表示
            notificationManager.notify(845, notification.build())
        }
    }

}