package top.jowanxu.scanlogin.hook

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.getPreferenceBoolean
import top.jowanxu.scanlogin.tryHook

class HookBili {
    companion object {
        private const val TV_DAMAKU_BILI_UI_WEBVIEW_ACTIVITY =
            "tv.danmaku.bili.ui.webview.MWebActivity"
        private const val TV_DAMAKU_BILI_UI_WEBVIEW_ANON_ACTIVITY =
            "tv.danmaku.bili.ui.webview.MWebActivity$9"
        private const val HOOK_BILIBILI_METHOD_NAME = "onPageFinished"
        private const val COMFIRM_URL = "passport.bilibili.com/mobile/h5-confirm.html"
        private const val JS_CODE =
            "javascript:setTimeout(function() { document.querySelector('a[class=\"btn sure-btn\"]').click()}, 100);"
        private val TAG = HookBili::class.java.simpleName
    }

    fun autoConfirmBiliLogin(lpParam: XC_LoadPackage.LoadPackageParam) {
        val activityClass =
            XposedHelpers.findClassIfExists(TV_DAMAKU_BILI_UI_WEBVIEW_ACTIVITY, lpParam.classLoader)
        if (activityClass != null) {
            tryHook(TAG, Constant.HOOK_ERROR) {
                XposedHelpers.findAndHookMethod(
                    activityClass,
                    Constant.ON_CREATE,
                    Bundle::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val activity = param.thisObject as Activity
                            val enable = getPreferenceBoolean(activity, key = Constant.BILI_ENABLE)
                            if (!enable) return
                            // 获取Class
                            val anonClass =
                                XposedHelpers.findClassIfExists(
                                    TV_DAMAKU_BILI_UI_WEBVIEW_ANON_ACTIVITY,
                                    lpParam.classLoader
                                ) ?: return

                            XposedHelpers.findAndHookMethod(
                                anonClass,
                                HOOK_BILIBILI_METHOD_NAME,
                                WebView::class.java,
                                String::class.java,
                                object : XC_MethodHook() {
                                    override fun afterHookedMethod(param: MethodHookParam) {
                                        val url = param.args[1] as String? ?: return
                                        if (!url.contains(COMFIRM_URL)) {
                                            return
                                        }
                                        val webView = param.args[0] as WebView? ?: return
                                        webView.loadUrl(JS_CODE)
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}