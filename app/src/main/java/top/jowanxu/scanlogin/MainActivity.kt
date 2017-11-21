package top.jowanxu.scanlogin

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import android.os.Bundle
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import top.jowanxu.scanlogin.provider.Preference

class MainActivity : Activity() {
    private var iconEnable: Boolean by Preference(this, Constant.ICON_ENABLE, true)
    private var weChatEnable: Boolean by Preference(this, Constant.WECHAT_ENABLE, true)
    private var timQQEnable: Boolean by Preference(this, Constant.TIM_QQ_ENABLE, true)
    private var weicoEnable: Boolean by Preference(this, Constant.WEICO_ENABLE, true)

    // SAM
    private val onCheckedChangeListener = OnCheckedChangeListener {
        buttonView, isChecked ->
        when (buttonView.id) {
            R.id.iconVisibility -> {
                iconEnable = isChecked
                tryHook(TAG, ICON_VISIBILITY_ERROR) {
                    this.packageManager.setComponentEnabledSetting(
                            ComponentName(this.packageName, this.packageName + MAIN_ACTIVITY),
                            if (isChecked) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_DISABLED,
                            DONT_KILL_APP)
                }
            }
            R.id.weChat -> {
                weChatEnable = isChecked
                Toast.makeText(this, getString(R.string.restart_wechat), Toast.LENGTH_SHORT).show()
            }
            R.id.timQQ -> {
                timQQEnable = isChecked
                Toast.makeText(this, getString(R.string.restart_qq_tim), Toast.LENGTH_SHORT).show()
            }
            R.id.weico -> {
                weicoEnable = isChecked
                Toast.makeText(this, getString(R.string.restart_weico), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tryHook(TAG, ICON_VISIBILITY_ERROR) {
            this.packageManager.setComponentEnabledSetting(ComponentName(this.packageName,
                    this.packageName + MAIN_ACTIVITY),
                    if (iconEnable)
                        COMPONENT_ENABLED_STATE_DISABLED
                    else
                        COMPONENT_ENABLED_STATE_DISABLED,
                    DONT_KILL_APP)
        }
        if (isModuleLoaded()) {
            val text: TextView = findViewById(R.id.moduleStatus) as TextView
            text.text = getString(R.string.module_loaded_success)
            val icon: Switch = findViewById(R.id.iconVisibility) as Switch
            icon.isChecked = iconEnable
            icon.setOnCheckedChangeListener(onCheckedChangeListener)
            val weChat: Switch = findViewById(R.id.weChat) as Switch
            weChat.isChecked = weChatEnable
            weChat.setOnCheckedChangeListener(onCheckedChangeListener)
            val timQQ: Switch = findViewById(R.id.timQQ) as Switch
            timQQ.isChecked = timQQEnable
            timQQ.setOnCheckedChangeListener(onCheckedChangeListener)
            val weico: Switch = findViewById(R.id.weico) as Switch
            weico.isChecked = weicoEnable
            weico.setOnCheckedChangeListener(onCheckedChangeListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    /**
     * 用于判断模块是否加载成功，通过自己Hook自己
     */
    private fun isModuleLoaded(): Boolean = false

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MAIN_ACTIVITY = ".MainActivityAlias"
        private const val ICON_VISIBILITY_ERROR = "iconVisibilityError "
    }

}
