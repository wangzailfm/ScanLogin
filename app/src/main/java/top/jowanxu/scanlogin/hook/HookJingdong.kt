package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.widget.Button
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookJingdong {
    companion object {
        private const val HOOK_LOAD_CLASS = "loadClass"
        private const val COM_JD_LIB_BROWSER = "com.jd.lib.login.ScanCodeLoginActivity"
        private const val COM_JD_LIB_BROWSER_CLASS = "ScanCodeLoginActivity"
        private const val COM_JD_LIB_LOGIN_CI = "com.jd.lib.login.ci"
        private val TAG = HookJingdong::class.java.simpleName
    }

    fun autoConfirmJingdongLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        tryHook(TAG, Constant.HOOK_ERROR) {
            // com.jd.lib.login.ci
            XposedHelpers.findAndHookMethod(ClassLoader::class.java,
                HOOK_LOAD_CLASS,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(loadClassParam: MethodHookParam) {
                        val ciClass = loadClassParam.result as Class<*>? ?: return
                        if (COM_JD_LIB_LOGIN_CI != ciClass.name) {
                            return
                        }
                        // com.jd.lib.login.ScanCodeLoginActivity
                        val loginClass = XposedHelpers.findClassIfExists(
                            COM_JD_LIB_BROWSER,
                            loadClassParam.thisObject as ClassLoader
                        ) ?: return
                        // ciDeclaredFields
                        val ciDeclaredFields = ciClass.declaredFields ?: return
                        // loginDeclaredFields
                        val loginDeclaredFields = loginClass.declaredFields ?: return
                        // hook run method
                        XposedHelpers.findAndHookMethod(
                            ciClass,
                            Constant.METHOD_RUN,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    // check String value
                                    ciDeclaredFields.filter {
                                        it.type.canonicalName == Constant.JAVA_LANG_STRING
                                    }.forEach {
                                        it.isAccessible = true
                                        val loginText = it.get(param.thisObject) as String
                                        // checked
                                        if (loginText == Constant.JINGDONG_LOGIN_TEXT) {
                                            // get Activity field
                                            val activityField = ciDeclaredFields.first {
                                                it.type.canonicalName.contains(COM_JD_LIB_BROWSER_CLASS)
                                            } ?: return
                                            // set access
                                            activityField.isAccessible = true
                                            // get Activity
                                            val activity =
                                                activityField.get(param.thisObject) as Activity? ?: return
                                            // if disable return
                                            val enable = getPreferenceBoolean(activity, key = Constant.JINGDONG_ENABLE)
                                            if (!enable) return
                                            // check button
                                            loginDeclaredFields.filter {
                                                it.type.canonicalName.toString() == Constant.ANDROID_WIDGET_BUTTON
                                            }.forEach {
                                                it.isAccessible = true
                                                // get button
                                                val loginButton = it.get(activity) as Button
                                                val loginButtonText = loginButton.text.toString()
                                                // check button text
                                                if (loginButtonText == Constant.JINGDONG_LOGIN_TEXT) {
                                                    // auto click
                                                    loginButton.performClick()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}