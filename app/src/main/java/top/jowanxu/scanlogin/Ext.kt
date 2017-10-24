package top.jowanxu.scanlogin

import android.util.Log
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.nio.charset.Charset


fun loge(tag: String, content: String) = Log.e(tag, content)

inline fun tryHook(tag: String, content: String, hook: () -> Unit) {
    try {
        hook()
    } catch (t: Throwable) {
        XposedBridge.log(content + t); loge(tag, content + t)
    }
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

    fun pauseThread() {
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
                tryHook(tag, error) {
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