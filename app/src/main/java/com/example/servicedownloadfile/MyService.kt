package com.example.servicedownloadfile

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MyService : IntentService("MyService") {
    var listener: DownloadListener = object : DownloadListener {
        // Анонимная реализация загрузочного интерфейса
        override fun percentDownloaded(percentDownloaded: Int) {

            // Для передачи процента загрузки,
            // создадим интент с акцией, на которую подписан
            // ресивер
            val percentDownloadedIntent = Intent(DOWNLOAD_PROGRESS)

            // Добавим в этот интент процент загрузки файла
            /******* нужно что-то сделать   */
             percentDownloadedIntent.putExtra(DOWNLOAD_PROGRESS, percentDownloaded);

            // Методика позволяет синхронно послать броадкаст
            LocalBroadcastManager.getInstance(this@MyService)
                .sendBroadcastSync(percentDownloadedIntent)
            // И проверить, был ли он кем-либо обработан
            val handled = percentDownloadedIntent.getBooleanExtra(DOWNLOAD_HANDLED, false)
            // Обработан он может быть только активити
            if (!handled) // Поэтому, если броадкаст не обработан,
            // повесим уведомление
                showNotification(percentDownloaded) else  // Если же броадкаст обработан, уберем уведомление, если оно
            // вдруг висит
                cancelNotification()
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        // Получим URL из Intent, с которым нас вызвали
        val url = intent!!.getStringExtra(DOWNLOAD_URL)

        // Получим результат
        /******* нужно что-то сделать   */
        val res: Result<String> = Utility.doDownload(url, listener)

        // Если файл успешно загружен,
        // нужно уведомить об этом активити
        if (res.result != null) {

            // Создадим интент, который будет содержать путь к загруженному файлу, с
            // акцией, на которую подписан ресивер
            val fileDownloadedIntent = Intent(DOWNLOAD_URL)
            // Положим в этот интент путь к файлу
            /******* нужно что-то сделать   */
             fileDownloadedIntent.putExtra(DOWNLOAD_URL, res.result);

            // Есть два типа броадкастов:
            // Общесистемый - все приложения в системе увидят (при желании)
            // если зарегистрированы на соответствующий IntentFilter
            // sendBroadcast(i);

            // Броадкаст внутри приложения через LocalBroadcastManager
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(fileDownloadedIntent)
            val handled = fileDownloadedIntent.getBooleanExtra(DOWNLOAD_HANDLED, false)
            if (!handled) // Если интент не был обработан ресивером,
            // покажем уведомление
                showNotification(res.result)
        }
    }

    // Уведомление, содержащее в себе прогресс
    @SuppressLint("MissingPermission")
    fun showNotification(downloadProgress: Int) {
        val builder: NotificationCompat.Builder = Utility.getBuilder(this)
        builder.setContentTitle("Downloading file")
        builder.setContentText("Downloaded Percent")
        builder.setProgress(100, downloadProgress, false)
        NotificationManagerCompat.from(this).notify(DOWNLOAD_NOTIFICATION_ID, builder.build())
    }

    // Отмена уведомления
    fun cancelNotification() {
        NotificationManagerCompat.from(this).cancel(DOWNLOAD_NOTIFICATION_ID)
    }

    // Уведомление, содержащее в себе путь к загруженному файлу
    @SuppressLint("MissingPermission")
    fun showNotification(downloadFileName: String?) {
        val builder: NotificationCompat.Builder = Utility.getBuilder(this)
        builder.setContentTitle("Download complete")
        builder.setContentText("File Downloaded")
        builder.setContentIntent(Utility.getFileDownloadedPendingIntent(this, downloadFileName))
        NotificationManagerCompat.from(this).notify(DOWNLOAD_NOTIFICATION_ID, builder.build())
    }

    // Загрузочный интерфейс - позволяет коду, осуществляющему загрузку,
    // уведомить о проценте загрузки
    interface DownloadListener {
        fun percentDownloaded(i: Int)
    }

    companion object {
        const val DOWNLOAD_URL = "DOWNLOAD_URL"
        const val DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS"
        const val DOWNLOAD_HANDLED = "DOWNLOAD_HANDLED"

        // Идентификатор уведомления как о прогрессе так и об
        // окончании загрузки файла
        const val DOWNLOAD_NOTIFICATION_ID = 555
    }
}
