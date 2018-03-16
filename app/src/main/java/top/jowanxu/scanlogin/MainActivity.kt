package top.jowanxu.scanlogin

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.widget.CompoundButton.OnCheckedChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import top.jowanxu.scanlogin.provider.Preference

class MainActivity : Activity() {
    private var iconEnable: Boolean by Preference(this, Constant.ICON_ENABLE, true)
    private var weChatEnable: Boolean by Preference(this, Constant.WECHAT_ENABLE, true)
    private var timQQEnable: Boolean by Preference(this, Constant.TIM_QQ_ENABLE, true)
    private var webQQEnable: Boolean by Preference(this, Constant.WEB_QQ_ENABLE, true)
    private var weicoEnable: Boolean by Preference(this, Constant.WEICO_ENABLE, true)
    private var biliEnable: Boolean by Preference(this, Constant.BILI_ENABLE, true)
    private var weiboEnable: Boolean by Preference(this, Constant.WEIBO_ENABLE, true)

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MAIN_ACTIVITY = ".MainActivityAlias"
        private const val ICON_VISIBILITY_ERROR = "iconVisibilityError "
    }

    /**
     * 用于判断模块是否加载成功，通过自己Hook自己
     */
    private fun isModuleLoaded(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tryHook(TAG, ICON_VISIBILITY_ERROR) {
            packageManager.setComponentEnabled(
                ComponentName(
                    this.packageName,
                    this.packageName + MAIN_ACTIVITY
                ), iconEnable
            )
        }
        if (isModuleLoaded()) {
            moduleStatus.apply {
                text = getString(R.string.module_loaded_success)
            }
            iconVisibility.apply {
                isChecked = iconEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            weChat.apply {
                isChecked = weChatEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            timQQ.apply {
                isChecked = timQQEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            webQQ.apply {
                isChecked = webQQEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            weico.apply {
                isChecked = weicoEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            bili.apply {
                isChecked = weicoEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            weibo.apply {
                isChecked = weicoEnable
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    // SAM
    private val onCheckedChangeListener =
        OnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {
                R.id.iconVisibility -> {
                    iconEnable = isChecked
                    tryHook(TAG, ICON_VISIBILITY_ERROR) {
                        packageManager.setComponentEnabled(
                            ComponentName(
                                this.packageName,
                                this.packageName + MAIN_ACTIVITY
                            ), iconEnable
                        )
                    }
                }
                R.id.weChat -> {
                    weChatEnable = isChecked
                    toast(getString(R.string.restart_wechat))
                }
                R.id.timQQ -> {
                    timQQEnable = isChecked
                    toast(getString(R.string.restart_qq_tim))
                }
                R.id.webQQ -> {
                    webQQEnable = isChecked
                    toast(getString(R.string.restart_qq_tim))
                }
                R.id.weico -> {
                    weicoEnable = isChecked
                    toast(getString(R.string.restart_weico))
                }
                R.id.bili -> {
                    biliEnable = isChecked
                    toast(getString(R.string.restart_bili))
                }
                R.id.weibo -> {
                    weiboEnable = isChecked
                    toast(getString(R.string.restart_weibo))
                }
            }
        }
}
