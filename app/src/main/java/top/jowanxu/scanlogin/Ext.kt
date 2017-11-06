package top.jowanxu.scanlogin

import android.util.Log
import de.robv.android.xposed.XposedBridge
import java.io.*
import java.nio.charset.Charset


fun String.loge(content: String) = Log.e(this, content)

inline fun tryHook(tag: String, content: String, hook: () -> Unit) {
    try {
        hook()
    } catch (t: Throwable) {
        XposedBridge.log(content + t); tag.loge(content + t)
    }
}

fun tryHook(tag: String, content: String, hook: () -> Unit, error: () -> Unit) {
    try {
        hook()
    } catch (t: Throwable) {
        error()
        XposedBridge.log(content + t)
        tag.loge(content + t)
    }
}

inline fun tryHookException(tag: String, content: String, hook: () -> Unit) {
    try {
        hook()
    } catch (e: Exception) {
        XposedBridge.log(content + e.message)
        tag.loge(content + e.message)
    }
}

fun tryHookException(tag: String, content: String, hook: () -> Unit, error: () -> Unit) {
    try {
        hook()
    } catch (e: Exception) {
        error()
        XposedBridge.log(content + e.message)
        tag.loge(content + e.message)
    }
}

fun readFileToBytes(path: String): ByteArray {
    var ins: FileInputStream? = null
    return try {
        ins = FileInputStream(path)
        ins.readBytes()
    } finally {
        ins?.close()
    }
}

fun readFileToObject(path: String): Any? {
    val bytes = readFileToBytes(path)
    val ins = ByteArrayInputStream(bytes)
    return ObjectInputStream(ins).use {
        val obj = it.readObject()
        it.close()
        obj
    }
}

fun writeBytesToFile(path: String, content: ByteArray) {
    val file = File(path)
    file.parentFile.mkdirs()
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(file)
        out.write(content)
    } finally {
        out?.close()
    }
}

fun writeObjectToFile(path: String, obj: Serializable) {
    val out = ByteArrayOutputStream()
    ObjectOutputStream(out).apply {
        writeObject(obj)
        close()
    }
    writeBytesToFile(path, out.toByteArray())
}

class MyThread(private val tag: String, private val error: String) : Thread() {
    private var stopThread = false
    private var pauseThread = true
    private lateinit var filePath: String
    private lateinit var content: String

    fun stopThread() {
        pauseThread = true
        stopThread = true
    }

    private fun pauseThread() {
        pauseThread = true
    }

    fun resumeThread(newFilePath: String, newContent: String) {
        filePath = newFilePath
        content = newContent
        pauseThread = false
    }

    override fun run() {
        while (!stopThread) {
            if (!pauseThread) {
                tryHookException(tag, error) {
                    val file = File(filePath)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    file.writeText(content, Charset.defaultCharset())
                }
                pauseThread()
            }
        }
    }
}