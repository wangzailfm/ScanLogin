package top.jowanxu.scanlogin

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import de.robv.android.xposed.XposedBridge
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine


/**
 * 打印日志
 */
fun String.loge(content: String) = Log.e(this, content)

/**
 * try catch
 */
inline fun tryHook(tag: String, content: String, hook: () -> Unit) {
    try {
        hook()
    } catch (t: Throwable) {
        XposedBridge.log(content + t); tag.loge(content + t)
    }
}

/**
 * try catch
 */
inline fun tryHookException(tag: String, content: String, hook: () -> Unit, error: (Throwable) -> Unit) {
    try {
        hook()
    } catch (t: Throwable) {
        error(t)
        XposedBridge.log(content + t)
        tag.loge(content + t)
    }
}

/**
 * 获取SharedPreference里面的boolean类型值
 */
fun getPreferenceBoolean(context: Context,uriString: String, key: String, defValue: Boolean): Boolean {

    return getPreferenceValue(context, uriString, key, defValue) {
        // boolean类型的值特殊处理
        TextUtils.equals("true", it.getString(0))
    }
}

/**
 * 获取SharedPreference里面的值
 */
inline fun <T> getPreferenceValue(context: Context,
        uriString: String, key: String, defValue: T, handler: (cursor: Cursor) -> T): T {

    var cursor: Cursor? = null

    try {
        cursor = context.contentResolver.query(
                Uri.parse(uriString), null,
                key, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                return handler(it)
            }
        }
    } catch (t: Throwable) {
        XposedBridge.log("getPreferenceValueError------$t")
    } finally {
        cursor?.close()
    }
    return defValue
}

/**
 * 异步操作
 */
suspend fun <T> asyncRequestSuspend(block: (Continuation<T>) -> Unit) = suspendCoroutine<T> {
    block(it)
}