package top.jowanxu.scanlogin.hook

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook


class HookTaobao {
    companion object {
        private const val HOOK_LOAD_CLASS = "loadClass"
        private const val COM_TAOBAO_WEBVIEW = "c8.lz"
        private const val HOOK_TAOBAO_METHOD_NAME = "onPageFinished"
        private const val CALL_METHOD_NAME = "loadUrl"
        private const val COMFIRM_URL = "login.m.taobao.com/qrcodeCheck.htm"
        private const val JS_CODE =
            "javascript:setTimeout( function() { var x = document.getElementsByTagName(\"button\"); for (var i = 0; i < x.length; i++) { if (x[i].innerText.indexOf(\"确认登录\") > -1) { x[i].click();} } } , 100);"
        private val TAG = HookTaobao::class.java.simpleName
    }

    fun autoConfirmTaobaoLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        tryHook(TAG, Constant.HOOK_ERROR) {
            // 获取loadClass
            XposedHelpers.findAndHookMethod(ClassLoader::class.java,
                HOOK_LOAD_CLASS,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val browserClass = param.result as Class<*>? ?: return
                        if (COM_TAOBAO_WEBVIEW != browserClass.name) {
                            return
                        }
                        val contextClass = XposedHelpers.findClassIfExists(
                            Constant.ANDROID_CONTENT_CONTEXTWRAPPER,
                            lpParam.classLoader
                        ) ?: return
                        XposedHelpers.findAndHookMethod(
                            contextClass,
                            Constant.GET_APPLICATION_CONTEXT,
                            object : XC_MethodHook() {
                                @Throws(Throwable::class)
                                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam) {
                                    val context =
                                        param.result as Context
                                    val enable =
                                        getPreferenceBoolean(context, key = Constant.TAOBAO_ENABLE)
                                    if (!enable) return
                                    browserClass.declaredMethods?.filter {
                                        it.name == HOOK_TAOBAO_METHOD_NAME
                                    }?.forEach {
                                        it.parameterTypes ?: return
                                        XposedHelpers.findAndHookMethod(
                                            browserClass,
                                            it.name,
                                            it.parameterTypes[0],
                                            it.parameterTypes[1],
                                            object : XC_MethodHook() {
                                                override fun afterHookedMethod(param: MethodHookParam) {
                                                    val url = param.args[1] as String? ?: return
                                                    if (!url.contains(COMFIRM_URL)) {
                                                        return
                                                    }
                                                    XposedHelpers.callMethod(
                                                        param.args[0],
                                                        CALL_METHOD_NAME,
                                                        JS_CODE
                                                    )
                                                }
                                            }
                                        )
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