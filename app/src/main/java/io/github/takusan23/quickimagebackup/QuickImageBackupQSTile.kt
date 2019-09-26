package io.github.takusan23.quickimagebackup

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QuickImageBackupQSTile : TileService() {
    override fun onClick() {
        super.onClick()
        //クリックしたとき
        //実行する
        val quickBackup = QuickBackup(applicationContext)
        //写真
        quickBackup.getImageFromMediaStore()
        //動画
        quickBackup.getVideoFromMediaStore()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //色つける
        qsTile.state = Tile.STATE_ACTIVE
        return super.onStartCommand(intent, flags, startId)
    }


}