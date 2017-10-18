package top.jowanxu.scanlogin

import android.app.Activity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author Jowan
 */
class Tutorial : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam) {
        when (lpParam.packageName) {
            COM_TENCENT_MM -> autoConfirmWeChatLogin(lpParam)
            COM_TENCENT_TIM, COM_TENCENT_QQ -> autoConfirmQQLogin(lpParam)
        }
    }

    fun loge(tag: String, content: String) = Log.e(tag, content)

    fun tryHook(hook: () -> Unit) {
        try {
            hook()
        } catch (t: Throwable) {
            XposedBridge.log("$HOOK_ERROR$t"); loge(TAG, "$HOOK_ERROR$t")
        }
    }

    /**
     * 扫一扫电脑端二维码后，自动点击允许登录TIM/QQ按钮
     * @param lpParam LoadPackageParam
     */
    private fun autoConfirmQQLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val aClass = XposedHelpers.findClassIfExists(QR_CODE_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = aClass.declaredFields ?: return
        tryHook {
            // Hook指定方法
            XposedHelpers.findAndHookMethod(aClass, DO_ON_CREATE, Bundle::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    // 每次打开都会调用两次doOnCreate
                    if (count == 1) {
                        return
                    }
                    // 调用doOnCreate超过两次后，也就是第二次打开扫一扫后就设置为0
                    if (count > 1) {
                        count = 0
                    }
                    val activity = param.thisObject as Activity
                    val resultStr = getHookName(lpParam.packageName,
                            activity.packageManager.getPackageInfo(lpParam.packageName, 0).versionName)
                    declaredFields.filter {
                        it.genericType.toString().contains(ANDROID_WIDGET_BUTTON)
                    }.forEach {
                        val handlerClass = XposedHelpers.findClassIfExists(resultStr, lpParam.classLoader) ?: return
                        // 设置true
                        it.isAccessible = true
                        // 获取值
                        val loginButton = it.get(param.thisObject) as Button
                        tryHook {
                            // Hook方法，对handleMessage方法调用后，进行判断Button的Text进行判断，并且自动调用点击方法
                            XposedHelpers.findAndHookMethod(handlerClass, HANDLE_MESSAGE, Message::class.java, object : XC_MethodHook() {
                                @Throws(Throwable::class)
                                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                                    // 当Button的Text为允许登录TIM/允许登录QQ的时候才实现点击
                                    if (loginButton.text.toString().contains(CONTAIN_TEXT)) {
                                        if (count == 0) {
                                            loginButton.performClick()
                                        }
                                        // 每次都增加
                                        count++
                                    }
                                }
                            })
                        }
                    }
                }
            })
        }
    }

    /**
     * 自动确认微信电脑端登录

     * @param lpParam LoadPackageParam
     */
    private fun autoConfirmWeChatLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        loge(TAG, HOOK_ERROR + WECHAT_HOOK_CLASS_NAME)
        // 获取Class
        val loginClass = XposedHelpers.findClassIfExists(WECHAT_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = loginClass.declaredFields ?: return
        loge(TAG, HOOK_ERROR + "WeChat")
        tryHook {
            XposedHelpers.findAndHookMethod(loginClass, "onCreate", Bundle::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    val activity = param.thisObject as Activity
                    declaredFields.filter {
                        it.genericType.toString().contains(ANDROID_WIDGET_BUTTON)
                    }.forEach {
                        it.isAccessible = true
                        val loginButton = it.get(param.thisObject) as Button
                        loge(TAG, HOOK_ERROR + loginButton.text.toString())
                        if (WECHAT_LOGIN_TEXT == loginButton.text.toString()) {
                            loginButton.performClick()
                            Toast.makeText(activity, AUTO_LOGIN, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

    }

    /**
     * 根据包名和版本号获取需要Hook的类名

     * @param packageName 包名
     * *
     * @param versionName 版本号
     * *
     * @return 类名
     */
    private fun getHookName(packageName: String, versionName: String): String = when (packageName) {
        COM_TENCENT_TIM -> getTIMHookName(versionName)
        COM_TENCENT_QQ -> getQQHookName(versionName)
        else -> getQQHookName(versionName)
    }

    /**
     * 根据版本号获取TIM需要Hook的类名

     * @param versionName 版本号
     * *
     * @return 类名
     */
    private fun getTIMHookName(versionName: String): String = when (versionName) {
        "2.0.0" -> "hxq"
        "1.2.0" -> "hzq"
        "1.1.5" -> "ghk"
        "1.1.0" -> "giy"
        "1.0.5" -> "gjd"
        "1.0.4" -> "gir"
        "1.0.0" -> "gik"
        else -> "hzq"
    }

    /**
     * 根据版本号获取QQ需要Hook的类名

     * @param versionName 版本号
     * *
     * @return 类名
     */
    private fun getQQHookName(versionName: String): String = when (versionName) {
        "7.2.0" -> "myi"
        "7.1.8" -> "mco"
        "7.1.5" -> "mcf"
        "7.1.0" -> "lri"
        "7.0.0" -> "lhi"
        else -> "myi"
    }

    companion object {

        private val COM_TENCENT_TIM = "com.tencent.tim"
        private val COM_TENCENT_QQ = "com.tencent.mobileqq"
        private val QR_CODE_HOOK_CLASS_NAME = "com.tencent.biz.qrcode.activity.QRLoginActivity"
        private val DO_ON_CREATE = "doOnCreate"
        private val ANDROID_WIDGET_BUTTON = "android.widget.Button"
        private val HANDLE_MESSAGE = "handleMessage"
        private val CONTAIN_TEXT = "允许登录"
        private val HOOK_ERROR = "Hook 出错 "
        private val COM_TENCENT_MM = "com.tencent.mm"
        private val WECHAT_LOGIN_TEXT = "登录"
        private val AUTO_LOGIN = "自动登录成功"
        private val WECHAT_HOOK_CLASS_NAME = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI"
        private val TAG = Tutorial::class.java.simpleName


        /**
         * 判断doOnCreate
         */
        private var count = 0
    }
}
