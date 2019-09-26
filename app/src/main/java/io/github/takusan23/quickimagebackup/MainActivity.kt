package io.github.takusan23.quickimagebackup

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    lateinit var pref_setting: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this)
        loadSetting()

        //パーミッションの確認
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //権限を求める
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 100
            )
        } else {
            //権限持ってる
            //バックアップ実行？
            backup_start_photo_button.setOnClickListener {
                val quickBackup = QuickBackup(this)
                quickBackup.getImageFromMediaStore()
            }
            backup_start_video_button.setOnClickListener {
                val quickBackup = QuickBackup(this)
                quickBackup.getVideoFromMediaStore()
            }
        }

        //保存ボタン
        setting_save_button.setOnClickListener {
            saveSetting()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun saveSetting() {
        val editor = pref_setting.edit()
        editor.putInt("photo_file_count", setting_photo_file_count.text.toString().toInt())
        editor.putInt("video_file_count", setting_movie_file_count.text.toString().toInt())
        editor.apply()
    }

    fun loadSetting() {
        setting_photo_file_count.setText(pref_setting.getInt("photo_file_count", 3).toString())
        setting_movie_file_count.setText(pref_setting.getInt("video_file_count", 3).toString())
    }

}
