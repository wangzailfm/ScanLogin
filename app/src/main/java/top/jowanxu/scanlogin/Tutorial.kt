package top.jowanxu.scanlogin

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.Constant.COM_TENCENT_MM
import top.jowanxu.scanlogin.Constant.COM_TENCENT_QQ
import top.jowanxu.scanlogin.Constant.COM_TENCENT_TIM
import top.jowanxu.scanlogin.Constant.TOP_JOWANXU_SCANLOGIN
import top.jowanxu.scanlogin.Constant.WEICO_PACKAGE_NAME
import top.jowanxu.scanlogin.hook.HookScanLogin
import top.jowanxu.scanlogin.hook.HookTIMQQ
import top.jowanxu.scanlogin.hook.HookWeChat
import top.jowanxu.scanlogin.hook.HookWeico

/**
 * @author Jowan
 */
class Tutorial : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam) =
            when (lpParam.packageName) {
                TOP_JOWANXU_SCANLOGIN -> HookScanLogin().checkModuleLoaded(lpParam)
                COM_TENCENT_MM -> HookWeChat().autoConfirmWeChatLogin(lpParam)
                COM_TENCENT_TIM, COM_TENCENT_QQ -> HookTIMQQ().run {
                    autoConfirmQQLogin(lpParam)
                    autoWebConfirmQQLogin(lpParam)
                }
                WEICO_PACKAGE_NAME -> HookWeico().run {
                    autoConfirmWeicoLogin(lpParam)
                    removeWeicoStartAD(lpParam)
                }
                else -> {
                }
            }
}
