package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.widget.TextView
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.Constant.ANDROID_WIDGET_TEXTVIEW
import top.jowanxu.scanlogin.Constant.AUTO_LOGIN
import top.jowanxu.scanlogin.Constant.HOOK_ERROR
import top.jowanxu.scanlogin.Constant.ON_CLICK
import top.jowanxu.scanlogin.Constant.WEICO_LOGIN_TEXT
import top.jowanxu.scanlogin.Constant.WEICO_LOGIN_TEXT_CF
import top.jowanxu.scanlogin.Constant.WEICO_LOGIN_TEXT_EN
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookWeico {
    companion object {
        private val TAG = HookWeico::class.java.simpleName
        private const val QR_CODE_HOOK_WEICO_CLASS_NAME = "com.weico.international.activity.scan.ScanWebSureLoginActivity"
        private const val WEICO_HOOK_METHOD_NAME = "scanCodeSuccess"
    }

    /**
     * Weico
     * @param lpParam LoadPackageParam
     */
    fun autoConfirmWeicoLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val loginClass = XposedHelpers.findClassIfExists(QR_CODE_HOOK_WEICO_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = loginClass.declaredFields ?: return
        tryHook(TAG, HOOK_ERROR) {
            XposedHelpers.findAndHookMethod(loginClass, WEICO_HOOK_METHOD_NAME, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    val enable = getPreferenceBoolean(activity,
                            Constant.PREFERENCE_BOOLEAN,
                            Constant.WEICO_ENABLE, true)
                    if (!enable) {
                        return
                    }
                    Thread.sleep(500)
                    declaredFields.filter {
                        it.genericType.toString().contains(ANDROID_WIDGET_TEXTVIEW)
                    }.forEach {
                        it.isAccessible = true
                        val loginButton = it.get(param.thisObject) as TextView
                        val loginButtonText = loginButton.text.toString()
                        if (loginButtonText == WEICO_LOGIN_TEXT
                                || loginButtonText == WEICO_LOGIN_TEXT_EN
                                || loginButtonText == WEICO_LOGIN_TEXT_CF) {
                            loginButton.post {
                                XposedHelpers.callMethod(param.thisObject, ON_CLICK)
                                Toast.makeText(activity, AUTO_LOGIN, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
    }
}