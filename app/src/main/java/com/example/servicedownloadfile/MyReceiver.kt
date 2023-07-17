package com.example.servicedownloadfile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import java.lang.ref.WeakReference


class MyReceiver(activity: MainActivity) : BroadcastReceiver() {
    // По всей вероятности, этого можно было бы и не делать,
    // так как в данном случае время жизненный цикл ресивера
    // и активити одинаков
    private val activity: WeakReference<MainActivity>?

    init {
        this.activity = WeakReference(activity)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Сразу же устанавливаем флаг, что
        // intent был обработан, чтобы
        // сервис не стал "поджигать" уведомления
        intent.putExtra(MyService.DOWNLOAD_HANDLED, true)
        val action = intent.action
        if (!TextUtils.isEmpty(action) && activity != null) {
            val main = activity.get()
            if (main != null) {
                if (action == MyService.DOWNLOAD_URL) {
                    /******* нужно что-то сделать   */
                    val filename = intent.getStringExtra(MyService.DOWNLOAD_URL)
                    if (filename != null) {
                        main.updateImage(filename)
                    }
                } else if (action == MyService.DOWNLOAD_PROGRESS) {
                    /******* нужно что-то сделать   */
                    val progress = intent.getIntExtra(MyService.DOWNLOAD_PROGRESS, -1)
                    if (progress > -1) {
                        main.updateProgress(progress)
                    }
                }
            }
        }
    }
}
