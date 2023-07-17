package com.example.servicedownloadfile

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager


/*
    Идея приложения
    ---------------

    По нажатию на кнопку загружается картинка.
    Если у приложения нет прав на запись на sd-карту, они запрашиваются.
    Картинка загружается сервисом.
    Активити регистрирует ресивер на получение броадкастов о проценте загрузки.
    Активити регистрирует ресивер на получение броадкаста об окончании загрузки.
    Если броадкаст обрабатывается ресивером, он фиксирует это в интенте броадкаста.
    Сервис посылает синхронные броадкасты, передавая в них процент загрузки.
    Сервис посылает синхронный броадкаст, когда картинка загружена.
    Если сервис обнаруживает, что броадкаст не был обработан, сервис показывает уведомление.
    При нажатии на уведомление об окончании загрузки файла, через Pending Intent в
    уведомлении запускается активити, получающая через интент путь к файлу загруженной картинки
 */

// Задача: поправить приложение везде, где есть комментарий -
/******* нужно что-то сделать   */ // в активити, сервисе и ресивере
class MainActivity : AppCompatActivity() {
    // Что загружаем
    private val url = "https://imgv3.fotor.com/images/blog-richtext-image/take-a-picture-with-camera.png"
    private var downloadedFileName: String? = null
    private var image: ImageView? = null
    private var progress: ProgressBar? = null

    // Ресивер, который будет получать сообщения от сервиса
    private val receiver: BroadcastReceiver = MyReceiver(this)

    // Регистрируем ресивер на получение сообщений от сервиса
    override fun onResume() {
        super.onResume()
        val manager = LocalBroadcastManager.getInstance(this)
        manager.registerReceiver(
            receiver,  // Интент фильтр сообщений, которые нам интересны
            IntentFilter(MyService.DOWNLOAD_URL)
        )
        manager.registerReceiver(
            receiver,  // Интент фильтр сообщений, которые нам интересны
            IntentFilter(MyService.DOWNLOAD_PROGRESS)
        )
    }

    // Дерегистрируем ресивер
    override fun onPause() {
        super.onPause()
        // Не хотим получать броадкасты при остановленной активити
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!TextUtils.isEmpty(downloadedFileName)) outState.putString(
            MyService.DOWNLOAD_URL,
            downloadedFileName
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image = findViewById<View>(R.id.image) as ImageView
        progress = findViewById<View>(R.id.progress) as ProgressBar
        if (savedInstanceState != null && savedInstanceState.containsKey(MyService.DOWNLOAD_URL)) {
            downloadedFileName = savedInstanceState.getString(MyService.DOWNLOAD_URL)
        } else {
            val activityStartIntent = intent
            if (activityStartIntent.hasExtra(MyService.DOWNLOAD_URL)) {
                downloadedFileName = activityStartIntent.getStringExtra(MyService.DOWNLOAD_URL)
            }
        }
        if (!TextUtils.isEmpty(downloadedFileName)) updateImage(downloadedFileName)
    }

    // Вызывается из ресивера
    fun updateImage(path: String?) {
        downloadedFileName = path

        // Показываем картинку
        /******* нужно что-то сделать   */
        Thread {
            kotlin.run {
                runOnUiThread {
                    image?.setImageBitmap(BitmapFactory.decodeFile(downloadedFileName))
                    progress?.visibility = View.INVISIBLE
                }
            }
        }


    }

    fun updateProgress(downloadedSoFar: Int) {
        // Показываем прогресс
        /******* нужно что-то сделать   */
        progress?.visibility = View.VISIBLE
        progress?.progress = downloadedSoFar
    }

    private fun doDownload() {
        // Если версия андроид больше или равна M и нужных прав нет,
        // попросим пользователя дать их нам
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_WRITE_EXTERNAL_STORAGE
            )
        } else {
            // Если права уже есть или версия андроид меньше M
            // просто запустим сервис
            val downloadServiceIntent = Intent(this, MyService::class.java)
            // передав ему УРЛ файла для загрузки
            downloadServiceIntent.putExtra(MyService.DOWNLOAD_URL, url)
            startService(downloadServiceIntent)
        }
    }

    // Вызывается, когда пользователь закрывает диалог
    // с предложением выбать права
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            // Если права даны
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                doDownload()
            } else {
                // Пользователь не выдал нужных прав
                Toast.makeText(
                    this,
                    "Cannot download images without permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Вызывается по нажатию на кнопку
    fun download(view: View?) {
        doDownload()
    }

    companion object {
        // Для запроса прав на запись на sd-карту
        private const val PERMISSIONS_WRITE_EXTERNAL_STORAGE = 25
    }
}