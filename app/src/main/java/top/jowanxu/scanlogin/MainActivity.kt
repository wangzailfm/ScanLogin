package top.jowanxu.scanlogin

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(packageName + SHARED_NAME, Context.MODE_PRIVATE)
        tryHook(TAG, ICON_VISIBILITY_ERROR) {
            this.packageManager.setComponentEnabledSetting(ComponentName(this.packageName,
                    this.packageName + MAIN_ACTIVITY),
                    if (sharedPreferences.getBoolean(ICON_VISIBILITY_KEY, true))
                        COMPONENT_ENABLED_STATE_DISABLED
                    else
                        COMPONENT_ENABLED_STATE_DISABLED,
                    DONT_KILL_APP)
        }
        if (isModuleLoaded()) {
            val text: TextView = findViewById(R.id.moduleStatus) as TextView
            text.text = getString(R.string.module_loaded_success)
            val icon: Switch = findViewById(R.id.iconVisibility) as Switch
            icon.isChecked = sharedPreferences.getBoolean(Constant.ICON_ENABLE, true)
            icon.setOnCheckedChangeListener(this)
            val weChat: Switch = findViewById(R.id.weChat) as Switch
            weChat.isChecked = sharedPreferences.getBoolean(Constant.WECHAT_ENABLE, true)
            weChat.setOnCheckedChangeListener(this)
            val timQQ: Switch = findViewById(R.id.timQQ) as Switch
            timQQ.isChecked = sharedPreferences.getBoolean(Constant.TIM_QQ_ENABLE, true)
            timQQ.setOnCheckedChangeListener(this)
            val weico: Switch = findViewById(R.id.weico) as Switch
            weico.isChecked = sharedPreferences.getBoolean(Constant.WEICO_ENABLE, true)
            weico.setOnCheckedChangeListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.iconVisibility -> {
                sharedPreferences.edit().putBoolean(Constant.ICON_ENABLE, isChecked).apply()
                tryHook(TAG, ICON_VISIBILITY_ERROR) {
                    this.packageManager.setComponentEnabledSetting(ComponentName(this.packageName,
                            this.packageName + MAIN_ACTIVITY),
                            if (isChecked) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_DISABLED,
                            DONT_KILL_APP)
                }
            }
            R.id.weChat -> {
                sharedPreferences.edit().putBoolean(Constant.WECHAT_ENABLE, isChecked).apply()
                Toast.makeText(this, getString(R.string.restart_wechat), Toast.LENGTH_SHORT).show()
            }
            R.id.timQQ -> {
                sharedPreferences.edit().putBoolean(Constant.TIM_QQ_ENABLE, isChecked).apply()
                Toast.makeText(this, getString(R.string.restart_qq_tim), Toast.LENGTH_SHORT).show()
            }
            R.id.weico -> {
                sharedPreferences.edit().putBoolean(Constant.WEICO_ENABLE, isChecked).apply()
                Toast.makeText(this, getString(R.string.restart_weico), Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * 用于判断模块是否加载成功，通过自己Hook自己
     */
    private fun isModuleLoaded(): Boolean = false

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MAIN_ACTIVITY = ".MainActivityAlias"
        private const val ICON_VISIBILITY_KEY = "iconVisibility"
        private const val ICON_VISIBILITY_ERROR = "iconVisibilityError "
        private const val SHARED_NAME = "_preferences"
    }

}
