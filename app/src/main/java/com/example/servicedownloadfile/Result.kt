package com.example.servicedownloadfile

// Шаблонный результат - содержит или результат
// или исключение, выброшенное в процессе его получения
class Result<T> {
    var result: T? = null
    var exception: Exception? = null
}