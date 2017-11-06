package top.jowanxu.scanlogin.hook

import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant
import top.jowanxu.scanlogin.tryHook

class HookScanLogin {
    companion object {
        private const val TOP_JOWANXU_SCANLOGIN_ACTIVITY = "top.jowanxu.scanlogin.MainActivity"
        private const val HOOK_SCANLOGIN_METHOD_NAME = "isModuleLoaded"
        private val TAG = HookScanLogin::class.java.simpleName
    }

    /**
     * 判断模块是否加载成功
     */
    fun checkModuleLoaded(lpParam: XC_LoadPackage.LoadPackageParam) {
        // 获取Class
        val activityClass = XposedHelpers.findClassIfExists(TOP_JOWANXU_SCANLOGIN_ACTIVITY, lpParam.classLoader) ?: return
        tryHook(TAG, Constant.HOOK_ERROR) {
            // 将方法返回值返回为true
            XposedHelpers.findAndHookMethod(activityClass, HOOK_SCANLOGIN_METHOD_NAME, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam?): Any = true
            })
        }
    }
}