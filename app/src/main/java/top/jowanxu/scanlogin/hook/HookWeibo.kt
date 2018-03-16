package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.webkit.WebView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookWeibo {
    companion object {
        private const val HOOK_LOAD_CLASS = "loadClass"
        private const val COM_SINA_WEIBO_BROWSER = "com.sina.weibo.browser.WeiboBrowser"
        private const val HOOK_WEIBO_METHOD_NAME = "onWebViewPageFinished"
        private const val JS_CODE =
            "javascript:setTimeout( function() { var x = document.getElementsByTagName(\"a\"); for (var i = 0; i < x.length; i++) { if (x[i].innerText.indexOf(\"确认登录\") > -1) { x[i].click();} } } , 100);"
        private val TAG = HookWeibo::class.java.simpleName
    }

    fun autoBiliConfirmLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        tryHook(TAG, Constant.HOOK_ERROR) {
            // 获取loadClass
            XposedHelpers.findAndHookMethod(ClassLoader::class.java,
                HOOK_LOAD_CLASS,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val browserClass = param.result as Class<*>? ?: return
                        if (COM_SINA_WEIBO_BROWSER != browserClass.name) {
                            return
                        }
                        XposedHelpers.findAndHookMethod(
                            browserClass,
                            HOOK_WEIBO_METHOD_NAME,
                            WebView::class.java,
                            String::class.java,
                            object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    val activity = param.thisObject as Activity
                                    val enable = getPreferenceBoolean(activity, key = Constant.WEIBO_ENABLE)
                                    if (!enable) return
                                    val url = param.args[1] as String? ?: return
                                    if (!url.contains("passport.weibo.cn/signin/qrcode/scan")) {
                                        return
                                    }
                                    val webView = param.args[0] as WebView? ?: return
                                    webView.loadUrl(JS_CODE)
                                }
                            })
                    }
                })
        }
    }
}