package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.Constant.ON_CREATE
import top.jowanxu.scanlogin.Constant.WECHAT_LOGIN_TEXT
import top.jowanxu.scanlogin.Constant.WECHAT_LOGIN_TEXT_CF
import top.jowanxu.scanlogin.Constant.WECHAT_LOGIN_TEXT_EN
import top.jowanxu.scanlogin.Constant.WECHAT_LOGIN_TEXT_JP
import top.jowanxu.scanlogin.tryHook
import top.jowanxu.scanlogin.tryHookException
import java.io.File

class HookWeChat {
    companion object {
        private const val WECHAT_HOOK_CLASS_NAME = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI"
        private const val WECHAT_FILE_NAME = "/scanLoginWeChat.xml"
        private val TAG = HookWeChat::class.java.simpleName
    }

    /**
     * 自动确认微信电脑端登录

     * @param lpParam LoadPackageParam
     */
    fun autoConfirmWeChatLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取是否需要自动登录
        val file = File(Environment.getExternalStorageDirectory().path + WECHAT_FILE_NAME)
        var enable = true
        tryHookException(TAG, Constant.HOOK_ERROR) {
            enable = file.readText().toBoolean()
        }
        if (!enable) {
            return
        }
        // 获取Class
        val loginClass = XposedHelpers.findClassIfExists(WECHAT_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = loginClass.declaredFields ?: return
        tryHook(TAG, Constant.HOOK_ERROR) {
            XposedHelpers.findAndHookMethod(loginClass, ON_CREATE, Bundle::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    declaredFields.filter {
                        it.genericType.toString().contains(Constant.ANDROID_WIDGET_BUTTON)
                    }.forEach {
                        it.isAccessible = true
                        val loginButton = it.get(param.thisObject) as Button
                        val loginButtonText = loginButton.text.toString()
                        if (WECHAT_LOGIN_TEXT == loginButtonText
                                || WECHAT_LOGIN_TEXT_CF == loginButtonText
                                || WECHAT_LOGIN_TEXT_EN == loginButtonText
                                || WECHAT_LOGIN_TEXT_JP == loginButtonText) {
                            loginButton.performClick()
                            Toast.makeText(activity, Constant.AUTO_LOGIN, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

    }

}