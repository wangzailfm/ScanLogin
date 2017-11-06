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
        sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(ICON_VISIBILITY_KEY, false)) {
            tryHookException(TAG, ICON_VISIBILITY_ERROR, {
                this.packageManager.setComponentEnabledSetting(ComponentName(this.packageName,
                        this.packageName + MAIN_ACTIVITY), COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP)
                sharedPreferences.edit().putBoolean(ICON_VISIBILITY_KEY, true).apply()
            }, {
                sharedPreferences.edit().putBoolean(ICON_VISIBILITY_KEY, false).apply()
            })

        }
        if (isModuleLoaded()) {
            val text: TextView = findViewById(R.id.moduleStatus) as TextView
            text.text = getString(R.string.module_loaded_success)
            val weChat: Switch = findViewById(R.id.weCht) as Switch
            weChat.isChecked = sharedPreferences.getBoolean(KEY_WECHAT, true)
            weChat.setOnCheckedChangeListener(this)
            val timQQ: Switch = findViewById(R.id.timQQ) as Switch
            timQQ.isChecked = sharedPreferences.getBoolean(KEY_TIM_QQ, true)
            timQQ.setOnCheckedChangeListener(this)
            val weico: Switch = findViewById(R.id.weico) as Switch
            weico.isChecked = sharedPreferences.getBoolean(KEY_WEICO, true)
            weico.setOnCheckedChangeListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.weCht -> {
                sharedPreferences.edit().putBoolean(KEY_WECHAT, isChecked).apply()
                Toast.makeText(this, getString(R.string.restart_wechat), Toast.LENGTH_SHORT).show()
            }
            R.id.timQQ -> {
                sharedPreferences.edit().putBoolean(KEY_TIM_QQ, isChecked).apply()
                Toast.makeText(this, getString(R.string.restart_qq_tim), Toast.LENGTH_SHORT).show()
            }
            R.id.weico -> {
                sharedPreferences.edit().putBoolean(KEY_WEICO, isChecked).apply()
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
        private const val SHARED_NAME = "scanLogin"
        private const val MAIN_ACTIVITY = ".MainActivityAlias"
        private const val ICON_VISIBILITY_KEY = "iconVisibility"
        private const val ICON_VISIBILITY_ERROR = "iconVisibilityError "
        private const val KEY_WECHAT = "weChat"
        private const val KEY_TIM_QQ = "timQQ"
        private const val KEY_WEICO = "weico"
        private const val WRITE_FILE_ERROR = "writeFileError "
    }

}
