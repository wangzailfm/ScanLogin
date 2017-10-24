package top.jowanxu.scanlogin

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.widget.Button
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

/**
 * @author Jowan
 */
class Tutorial : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam) {
        when (lpParam.packageName) {
            TOP_JOWANXU_SCANLOGIN -> checkModuleLoaded(lpParam)
            COM_TENCENT_MM -> autoConfirmWeChatLogin(lpParam)
            COM_TENCENT_TIM, COM_TENCENT_QQ -> autoConfirmQQLogin(lpParam)
        }
    }

    /**
     * 判断模块是否加载成功
     */
    private fun checkModuleLoaded(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val activityClass = XposedHelpers.findClassIfExists(TOP_JOWANXU_SCANLOGIN_ACTIVITY, lpParam.classLoader) ?: return
        tryHook(TAG, HOOK_ERROR) {
            // 将方法返回值返回为true
            XposedHelpers.findAndHookMethod(activityClass, HOOK_SCANLOGIN_METHOD_NAME, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam?): Any = true
            })
        }
    }

    /**
     * 扫一扫电脑端二维码后，自动点击允许登录TIM/QQ按钮
     * @param lpParam LoadPackageParam
     */
    private fun autoConfirmQQLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取是否需要自动允许
        val file = File(Environment.getExternalStorageDirectory().path + TIM_QQ_FILE_NAME)
        var enable = true
        tryHook(TAG, HOOK_ERROR) {
            enable = file.readText().toBoolean()
        }
        if (!enable) {
            return
        }
        // 获取Class
        val aClass = XposedHelpers.findClassIfExists(QR_CODE_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = aClass.declaredFields ?: return
        tryHook(TAG, HOOK_ERROR) {
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
                        tryHook(TAG, HOOK_ERROR) {
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
        // 获取是否需要自动登录
        val file = File(Environment.getExternalStorageDirectory().path + WECHAT_FILE_NAME)
        var enable = true
        tryHook(TAG, HOOK_ERROR) {
            enable = file.readText().toBoolean()
        }
        if (!enable) {
            return
        }
        // 获取Class
        val loginClass = XposedHelpers.findClassIfExists(WECHAT_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = loginClass.declaredFields ?: return
        tryHook(TAG, HOOK_ERROR) {
            XposedHelpers.findAndHookMethod(loginClass, ON_CREATE, Bundle::class.java, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                    val activity = param.thisObject as Activity
                    declaredFields.filter {
                        it.genericType.toString().contains(ANDROID_WIDGET_BUTTON)
                    }.forEach {
                        it.isAccessible = true
                        val loginButton = it.get(param.thisObject) as Button
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
        private const val TOP_JOWANXU_SCANLOGIN = "top.jowanxu.scanlogin"
        private const val TOP_JOWANXU_SCANLOGIN_ACTIVITY = "top.jowanxu.scanlogin.MainActivity"
        private const val HOOK_SCANLOGIN_METHOD_NAME = "isModuleLoaded"
        private const val COM_TENCENT_TIM = "com.tencent.tim"
        private const val COM_TENCENT_QQ = "com.tencent.mobileqq"
        private const val QR_CODE_HOOK_CLASS_NAME = "com.tencent.biz.qrcode.activity.QRLoginActivity"
        private const val DO_ON_CREATE = "doOnCreate"
        private const val ANDROID_WIDGET_BUTTON = "android.widget.Button"
        private const val HANDLE_MESSAGE = "handleMessage"
        private const val CONTAIN_TEXT = "允许登录"
        private const val HOOK_ERROR = "Hook 出错 "
        private const val COM_TENCENT_MM = "com.tencent.mm"
        private const val WECHAT_LOGIN_TEXT = "登录"
        private const val AUTO_LOGIN = "自动登录成功"
        private const val ON_CREATE = "onCreate"
        private const val WECHAT_HOOK_CLASS_NAME = "com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI"
        private val TAG = Tutorial::class.java.simpleName
        private const val WECHAT_FILE_NAME = "/scanLoginWeChat.xml"
        private const val TIM_QQ_FILE_NAME = "/scanLoginTIMQQ.xml"


        /**
         * 判断doOnCreate
         */
        private var count = 0
    }
}
