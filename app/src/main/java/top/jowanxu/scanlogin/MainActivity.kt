package top.jowanxu.scanlogin

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.packageManager.setComponentEnabledSetting(ComponentName(this.packageName, "${this.packageName}.MainActivityAlias"), COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP)
        if (isModuleLoaded()) {
            val text: TextView = findViewById(R.id.moduleStatus) as TextView
            text.text = getString(R.string.module_loaded_success)
        }
    }

    /**
     * 用于判断模块是否加载成功，通过自己Hook自己
     */
    private fun isModuleLoaded(): Boolean = false

}
