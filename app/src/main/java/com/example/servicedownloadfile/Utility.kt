package com.example.servicedownloadfile

import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


// Загрузка файла по УРЛ, возвращение либо полного пути к файлу
// либо исключения
object Utility {
    // Чтобы не сообщать о каждом прочитанном байте,
    // будем стараться сообщать только если количество прочитанных байт на столько
    // процентов больше того количества байт, о котором уже было сообщено
    private const val percentDifference = 5
    private const val ACTIVITY_PENDING_INTENT_ID = 444

    // PendingIntent для запуска активити из уведомления о
    // завершении загрузки файла
    fun getFileDownloadedPendingIntent(
        context: Context?,
        downloadedFileName: String?,
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MyService.DOWNLOAD_URL, downloadedFileName)
        return PendingIntent.getActivity(
            context,
            ACTIVITY_PENDING_INTENT_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // Общий код для создания уведомления
    fun getBuilder(context: Context?): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context!!)
        builder.setSmallIcon(R.drawable.ic_media_pause)
        builder.setAutoCancel(true)
        return builder
    }

    // Код для загрузки файла с листенером
    // загруженных процентов
    fun doDownload(url: String?, listener: MyService.DownloadListener?): Result<String> {
        val result = Result<String>()

        // Каталог, соответствующий external storage
        val storage = Environment.getExternalStorageDirectory()

        try {
            // Создадим файл с именем
            // abc.....def
            val output = File.createTempFile("acb", "def", storage)
            val target = URL(url)
            val connection = target.openConnection() as HttpURLConnection

            // Сколько всего байт в картинке
            val contentLength = connection.contentLength

            // Сколько байт прочитано
            var bytesWritten = 0

            // Про сколько байт сообщено
            var bytesSignalled = 0
            val input = connection.inputStream
            val fos = FileOutputStream(output.canonicalPath)
            try {
                // Счетчик сколько байт прочитали за раз
                var read = 0

                // Буфер, куда читаем байты из InputStream
                // в 1024 нет ничего магического - просто размер буфера
                // Используют еще 8*1024, 16*1024 и т.п.
                val bytes = ByteArray(1024)

                // Пока количество считанных байт не равно -1
                // (сигнал о том, что байты в InputBuffer закончились)
                // читаем в буфер
                while (input.read(bytes).also { read = it } != -1) {
                    // Поспим немножко
                    Thread.sleep(20)

                    // и пишем в OutputStream
                    fos.write(bytes, 0, read)
                    bytesWritten += read
                    if (listener != null && contentLength > 0) {
                        if ((bytesWritten - bytesSignalled) * 100 / contentLength >= percentDifference) {
                            listener.percentDownloaded(bytesWritten * 100 / contentLength)
                            bytesSignalled = bytesWritten
                        }
                    }
                }
                // Возвращаем в результате путь к файлу
                result.result = output.canonicalPath
            } // Обязательно закроем потоки ввода-вывода
            finally {
                input.close()
                fos.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Или исключение
            result.exception = e
        }
        return result
    }
}