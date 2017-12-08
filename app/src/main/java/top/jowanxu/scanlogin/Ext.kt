package top.jowanxu.scanlogin

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.DONT_KILL_APP
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import de.robv.android.xposed.XposedBridge


/**
 * 打印日志
 */
fun String.loge(content: String) = Log.e(this, content)

/**
 * Toast
 */
fun Context.toast(content: String) = Toast.makeText(this, content, Toast.LENGTH_SHORT).show()

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
fun getPreferenceBoolean(context: Context, uriString: String = Constant.PREFERENCE_BOOLEAN, key: String, defValue: Boolean = true): Boolean {

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
    tryHook(Constant.HOOK_ERROR, Constant.GET_PREFERENCE_PRE) {
        context.contentResolver.query(
                Uri.parse(uriString), null,
                key, null, null).use {
            it?.let {
                if (it.moveToFirst()) return handler(it)
            }
        }
    }
    return defValue
}

/**
 * 是否显示桌面图标
 */
fun PackageManager.setComponentEnabled(componentName: ComponentName, enable: Boolean) =
        this.setComponentEnabledSetting(componentName, if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP)
