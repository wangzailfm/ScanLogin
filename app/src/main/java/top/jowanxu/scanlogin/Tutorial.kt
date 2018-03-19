package top.jowanxu.scanlogin

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.jowanxu.scanlogin.hook.*

/**
 * @author Jowan
 */
class Tutorial : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpParam: XC_LoadPackage.LoadPackageParam) =
            when (lpParam.packageName) {
                Constant.TOP_JOWANXU_SCANLOGIN ->
                    HookScanLogin().checkModuleLoaded(lpParam)
                Constant.COM_TENCENT_MM ->
                    HookWeChat().autoConfirmWeChatLogin(lpParam)
                Constant.COM_TENCENT_TIM, Constant.COM_TENCENT_QQ ->
                    HookTIMQQ().run {
                        autoConfirmQQLogin(lpParam, Constant.DO_ON_CREATE)
                        autoWebConfirmQQLogin(lpParam)
                    }
                Constant.COM_TENCENT_QQ_LITE, Constant.COM_TENCENT_QQ_I ->
                    HookTIMQQ().run {
                        autoConfirmQQLogin(lpParam, Constant.ON_CREATE)
                        autoWebConfirmQQLogin(lpParam)
                    }
                Constant.WEICO_PACKAGE_NAME ->
                    HookWeico().run {
                        autoConfirmWeicoLogin(lpParam)
                        removeWeicoStartAD(lpParam)
                    }
                Constant.TV_DAMAKU_BILI, Constant.COM_BILI_APP -> {
                    HookBili().autoConfirmBiliLogin(lpParam)
                }
                Constant.COM_SINA_WEIBO -> {
                    HookWeibo().autoConfirmWeiboLogin(lpParam)
                }
                Constant.COM_TAOBAO_TAOBAO -> {
                    HookTaobao().autoConfirmTaobaoLogin(lpParam)
                }
                Constant.COM_JINGDONG_APP -> {
                    HookJingdong().autoConfirmJingdongLogin(lpParam)
                }
                else -> {
                }
            }
}
