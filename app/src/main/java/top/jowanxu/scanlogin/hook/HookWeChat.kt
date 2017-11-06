package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.os.Bundle
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
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookWeChat {
    companion object {
        private const val WECHAT_HOOK_CLASS_NAME = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI"
        private val TAG = HookWeChat::class.java.simpleName
    }

    /**
     * 自动确认微信电脑端登录

     * @param lpParam LoadPackageParam
     */
    fun autoConfirmWeChatLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val loginClass = XposedHelpers.findClassIfExists(WECHAT_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = loginClass.declaredFields ?: return
        tryHook(TAG, Constant.HOOK_ERROR) {
            XposedHelpers.findAndHookMethod(loginClass, ON_CREATE, Bundle::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    val enable = getPreferenceBoolean(activity,
                            Constant.PREFERENCE_BOOLEAN,
                            Constant.WECHAT_ENABLE, true)
                    if (!enable) {
                        return
                    }
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