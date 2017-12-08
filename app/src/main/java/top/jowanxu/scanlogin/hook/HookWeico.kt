package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.widget.TextView
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.Constant.ANDROID_WIDGET_TEXTVIEW
import top.jowanxu.scanlogin.Constant.WEICO_LOGIN_TEXT
import top.jowanxu.scanlogin.Constant.WEICO_LOGIN_TEXT_CF
import top.jowanxu.scanlogin.Constant.WEICO_LOGIN_TEXT_EN
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookWeico {
    companion object {
        private val TAG = HookWeico::class.java.simpleName
        private const val QR_CODE_HOOK_WEICO_CLASS_NAME = "com.weico.international.activity.scan.ScanWebSureLoginActivity"
        private const val HOOK_WEICO_START_ACTIVITY_NAME = "com.weico.international.activity.v4.Setting"
        private const val WEICO_HOOK_METHOD_NAME = "scanCodeSuccess"
        private const val DISPLAY_AD = "display_ad"
        private const val WEICO_HOOK_DISPLAY_AD_METHOD_NAME = "loadInt"
        private const val AD_DISPLAY_TIME = "ad_display_time"
        private const val WEICO_HOOK_AD_DISPLAY_TIME_METHOD_NAME = "loadLong"
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
        tryHook(TAG, Constant.HOOK_ERROR) {
            XposedHelpers.findAndHookMethod(loginClass, WEICO_HOOK_METHOD_NAME, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    val enable = getPreferenceBoolean(activity, key = Constant.WEICO_ENABLE)
                    if (!enable) return

                    Thread.sleep(500)
                    declaredFields.filter {
                        it.type.canonicalName.toString() == (ANDROID_WIDGET_TEXTVIEW)
                    }.forEach {
                        it.isAccessible = true
                        val loginButton = it.get(param.thisObject) as TextView
                        val loginButtonText = loginButton.text.toString()
                        if (loginButtonText == WEICO_LOGIN_TEXT
                                || loginButtonText == WEICO_LOGIN_TEXT_EN
                                || loginButtonText == WEICO_LOGIN_TEXT_CF) {
                            loginButton.post {
                                XposedHelpers.callMethod(param.thisObject, Constant.ON_CLICK)
                                Toast.makeText(activity, Constant.AUTO_LOGIN, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
    }

    /**
     * 去除weico启动广告
     * @param lpParam LoadPackageParam
     */
    fun removeWeicoStartAD(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Activity的Class
        val aClass = XposedHelpers.findClassIfExists(HOOK_WEICO_START_ACTIVITY_NAME, lpParam.classLoader) ?: return
        tryHook(TAG, HOOK_WEICO_START_ACTIVITY_NAME) {
            // Hook，将display_ad返回的值设置为-1
            XposedHelpers.findAndHookMethod(aClass, WEICO_HOOK_DISPLAY_AD_METHOD_NAME,
                    String::class.java, object : XC_MethodHook() {

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    val param1 = param.args[0] as String? ?: return
                    // 如果参数为display_ad的时候将返回值改为-1
                    if (DISPLAY_AD == param1) {
                        param.result = -1
                    }
                }
            })
        }
        tryHook(TAG, HOOK_WEICO_START_ACTIVITY_NAME) {
            // 从后台返回前台也会出现广告，当时间超过30分钟，就会出现广告，所以要将ad_display_time设置为当前时间
            XposedHelpers.findAndHookMethod(aClass, WEICO_HOOK_AD_DISPLAY_TIME_METHOD_NAME,
                    String::class.java, object : XC_MethodHook() {

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    val param1 = param.args[0] as String? ?: return
                    // 如果参数为display_ad_time的时候将返回值改为当前时间戳
                    if (param1 == AD_DISPLAY_TIME) {
                        param.result = System.currentTimeMillis()
                    }
                }
            })
        }
    }
}