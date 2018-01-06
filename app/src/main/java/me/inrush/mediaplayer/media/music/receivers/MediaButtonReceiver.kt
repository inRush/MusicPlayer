package me.inrush.mediaplayer.media.music.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.inrush.mediaplayer.App
import me.inrush.mediaplayer.media.music.MusicPlayerInitializer
import me.inrush.mediaplayer.media.music.listeners.OnServiceBindCompleteListener
import me.inrush.mediaplayer.media.music.services.MusicService
import net.qiujuer.genius.kit.handler.Run

/**
 * 耳机线控指令接收器
 *
 * @author inrush
 * @date 2018/1/5.
 */

class MediaButtonReceiver : BroadcastReceiver() {
    private var mPlayer: MusicService? = null
    private var mInitializer: MusicPlayerInitializer? = null

    /**
     * 初始化绑定音乐服务
     */
    @Synchronized private fun init() {
        if (mInitializer == null) {
            MusicPlayerInitializer.init(App.getInstance())
            mInitializer = MusicPlayerInitializer(App.getInstance(), OnServiceBindCompleteListener { player ->
                mPlayer = player
                process()
            })
            mInitializer!!.bindToService()
        }
    }

    private fun process() {
        // 耳机线按钮点击,这次耳机操作的时间距离上次要超过预设的临界值
        if (!isRun && System.currentTimeMillis() - mLastOperaTime > HEADSET_OPERA_LIMIT) {
            isRun = true
            // 延迟一秒执行检测点击耳机按键的事件
            Run.getBackgroundHandler().postDelayed({
                when (mClickCount) {
                    // 1秒以内点击1次,切换播放状态
                    HEADSET_ONCE_CLICK -> mPlayer!!.play()
                    // 1秒以内点击2次,下一首音乐
                    HEADSET_TWICE_CLICK ->
                        mPlayer!!.nextMusic()
                    // 1秒以内点击3次,上一首音乐
                    HEADSET_THRICE_CLICK ->
                        mPlayer!!.preMusic()
                }
                mClickCount = 0
                mLastOperaTime = System.currentTimeMillis()
                isRun = false
            }, 1000)
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        if (mInitializer == null) {
            init()
        } else {
            process()
        }
        // 防止在等待期间设置了mClickCount的值
        if (System.currentTimeMillis() - mLastOperaTime > HEADSET_OPERA_LIMIT) {
            mClickCount++
        }
    }

    companion object {
        /**
         * 最后一次执行耳机操作的时间
         */
        private var mLastOperaTime: Long = 0
        /**
         * 耳机在当前操作执行的点击次数
         */
        private var mClickCount = 0
        private var isRun = false
        /**
         * 因为不知什么原因下面的onReceive会执行两次,所以这里的次数都要乘2
         */
        private val HEADSET_ONCE_CLICK = 2
        private val HEADSET_TWICE_CLICK = 4
        private val HEADSET_THRICE_CLICK = 6
        /**
         * 两次耳机操作的时间间隔
         */
        private val HEADSET_OPERA_LIMIT = 500
    }
}
