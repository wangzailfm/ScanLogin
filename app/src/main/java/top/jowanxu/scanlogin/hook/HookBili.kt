package top.jowanxu.scanlogin.hook

import android.webkit.WebView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.tryHook

class HookBili {
    companion object {
        private const val TV_DAMAKU_BILI_UI_WEBVIEW_ACTIVITY = "tv.danmaku.bili.ui.webview.MWebActivity$9"
        private const val HOOK_BILIBILI_METHOD_NAME = "onPageFinished"
        private val TAG = HookBili::class.java.simpleName
    }

    fun autoBiliConfirmQQLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val activityClass = XposedHelpers.findClassIfExists(TV_DAMAKU_BILI_UI_WEBVIEW_ACTIVITY, lpParam.classLoader) ?: return
        tryHook(TAG, Constant.HOOK_ERROR) {
            XposedHelpers.findAndHookMethod(activityClass, HOOK_BILIBILI_METHOD_NAME, WebView::class.java, String::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val url = param.args[1] as String? ?: return
                    if (!url.contains("passport.bilibili.com/mobile/h5-confirm.html")) {
                        return
                    }
                    val webView = param.args[0] as WebView? ?: return
                    webView.loadUrl("javascript:setTimeout(function() { document.querySelector('a[class=\"btn sure-btn\"]').click()}, 500);")
                }
            })
        }
    }
}