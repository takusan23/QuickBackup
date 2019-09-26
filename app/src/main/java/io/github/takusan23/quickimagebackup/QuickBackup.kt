package io.github.takusan23.quickimagebackup

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class QuickBackup(val context: Context) {

    var pref_setting: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    //通知
    val progressNotification = ProgressNotification(context)

    //バックアップ件数を取得
    //var fileCount = pref_setting.getInt("photo_file_count", 3)

    //MediaStoreから画像を取得する？
    fun getImageFromMediaStore() {

        val id = ProgressNotification.photoNotifyId

        //表示
        val fileCount = pref_setting.getInt("photo_file_count", 3)
        progressNotification.showProgressNotification(
            context.getString(R.string.backup_progress_notification_text_image),
            id
        )

        //保存するときに使う配列
        val uriList = arrayListOf<Uri>()
        val nameList = arrayListOf<String>()

        val contentResolver = context.contentResolver
        val cursor =
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
        //最後にまず移動
        cursor?.moveToLast()
        for (i in 0 until fileCount) {
            //名前取得
            val name =
                cursor?.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            //URIが　欲しい！！！！
            //IDを取得→Uriきた
            val id = cursor?.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
            val uri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id ?: 0)
            //配列に入れる
            uriList.add(uri)
            nameList.add(name ?: "test.jpg")
            //前に移動
            cursor?.moveToPrevious()
        }
        cursor?.close()
        //書き込む
        try {
            writeFileScopedStorage(
                uriList, nameList, fileCount, id
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }


    fun getVideoFromMediaStore() {

        val id = ProgressNotification.videoNotifyId

        //表示
        val fileCount = pref_setting.getInt("video_file_count", 3)
        progressNotification.showProgressNotification(
            context.getString(R.string.backup_progress_notification_text_video),
            id
        )

        //保存するときに使う配列
        val uriList = arrayListOf<Uri>()
        val nameList = arrayListOf<String>()

        val contentResolver = context.contentResolver
        val cursor =
            contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
        //最後にまず移動
        cursor?.moveToLast()
        for (i in 0 until fileCount) {
            //名前取得
            val name =
                cursor?.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            //URIが　欲しい！！！！
            //IDを取得→Uriきた
            val id = cursor?.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
            val uri =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id ?: 0)
            //配列に入れる
            uriList.add(uri)
            nameList.add(name ?: "test.mp4")
            //前に移動
            cursor?.moveToPrevious()
        }
        cursor?.close()

        //書き込む
        writeFileScopedStorage(
            uriList, nameList, fileCount, id
        )
    }

    //ScopedStorageに書き込む
    //Android 10だけね
    fun writeFileScopedStorage(
        uriList: ArrayList<Uri>, nameList: ArrayList<String>, size: Int,
        notifyId: Int
    ) {
        //ScopedStorage
        val file = context.getExternalFilesDir(null)
        //バックアップディレクトリ作成？
        val backupFile = File(file?.path + "/${getNowDate()}")
        backupFile.mkdir()
        //コピーを実行する
        var progress = 0
        for (count in 0 until uriList.size) {
            //重いので非同期処理で
            thread {
                //ファイル生成（画像が入る）
                val picFile = File(backupFile.path + "/${nameList[count]}")
                //元データ
                try {
                    val inputStream = context.contentResolver.openInputStream(uriList[count])
                    //書き込み先
                    val outputStream = picFile.outputStream()
                    //書き込む
                    while (true) {
                        val data = inputStream?.read()
                        if (data == -1) {
                            //UIスレッド
                            val handler = Handler(Looper.getMainLooper());
                            handler.post {
                                //通知更新
                                progress += 1
                                progressNotification.setProgress(progress, size, notifyId)
                                if (size == progress) {
                                    //終了メッセージ
                                    if (context is AppCompatActivity) {
                                        context.runOnUiThread {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.finish_backup),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                            //終わったらwhile文抜ける
                            break
                        }
                        if (data != null) {
                            outputStream.write(data)
                        }
                    }
                    //最後は閉じる
                    outputStream.close()
                    inputStream?.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    //フォルダ名
    fun getNowDate(): String {
        val calender = Calendar.getInstance()
        val sdf = SimpleDateFormat("MM-dd HH:mm:ss");
        return sdf.format(calender.time)
    }

}