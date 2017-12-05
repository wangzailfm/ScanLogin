package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.Constant.ANDROID_WIDGET_BUTTON
import top.jowanxu.scanlogin.Constant.CONTAIN_TEXT
import top.jowanxu.scanlogin.asyncRequestSuspend
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookTIMQQ {
    companion object {
        private val TAG = HookTIMQQ::class.java.simpleName
        private const val QR_CODE_HOOK_CLASS_NAME = "com.tencent.biz.qrcode.activity.QRLoginActivity"
        private const val DO_ON_CREATE = "doOnCreate"

        /**
         * 判断doOnCreate
         */
        private var count = 0
    }

    /**
     * 扫一扫电脑端二维码后，自动点击允许登录TIM/QQ按钮
     * @param lpParam LoadPackageParam
     */
    fun autoConfirmQQLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val aClass = XposedHelpers.findClassIfExists(QR_CODE_HOOK_CLASS_NAME, lpParam.classLoader) ?: return
        // 获取Class里面的Field
        val declaredFields = aClass.declaredFields ?: return
        tryHook(TAG, Constant.HOOK_ERROR) {
            launch {
                val asyncDoOnCreate = async {
                    asyncRequestSuspend<XC_MethodHook.MethodHookParam> {
                        tryHook(TAG, Constant.HOOK_ERROR) {
                            XposedHelpers.findAndHookMethod(aClass, DO_ON_CREATE, Bundle::class.java, object : XC_MethodHook() {
                                @Throws(Throwable::class)
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    it.resume(param)
                                }
                            })
                        }
                    }
                }
                val param = asyncDoOnCreate.await()
                val activity = param.thisObject as Activity
                val enable = getPreferenceBoolean(activity,
                        Constant.PREFERENCE_BOOLEAN,
                        Constant.TIM_QQ_ENABLE, true)
                if (!enable) {
                    return@launch
                }
                declaredFields.filter {
                    it.type.canonicalName.toString() == (ANDROID_WIDGET_BUTTON)
                }.forEach {
                    // 设置true
                    it.isAccessible = true
                    // 获取值
                    val loginButton = it.get(param.thisObject) as Button
                    delay(500)
                    // 当Button的Text为允许登录TIM/允许登录QQ的时候才实现点击
                    if (loginButton.text.toString().contains(CONTAIN_TEXT)) {
                        launch(UI) {
                            loginButton.performClick()
                        }
                    }
                }
            }
        }
    }
}