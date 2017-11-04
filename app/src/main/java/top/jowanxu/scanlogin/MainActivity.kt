package top.jowanxu.scanlogin

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import android.os.Bundle
import android.os.Environment
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : Activity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var thread: MyThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(ICON_VISIBILITY_KEY, false)) {
            tryHook(TAG, ICON_VISIBILITY_ERROR, {
                this.packageManager.setComponentEnabledSetting(ComponentName(this.packageName,
                        this.packageName + MAIN_ACTIVITY), COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP)
                sharedPreferences.edit().putBoolean(ICON_VISIBILITY_KEY, true).apply()
            }, {
                sharedPreferences.edit().putBoolean(ICON_VISIBILITY_KEY, false).apply()
            })

        }
        if (isModuleLoaded()) {
            initFile()
            val text: TextView = findViewById(R.id.moduleStatus) as TextView
            text.text = getString(R.string.module_loaded_success)
            val weChat: Switch = findViewById(R.id.weCht) as Switch
            weChat.isChecked = sharedPreferences.getBoolean(KEY_WECHAT, true)
            weChat.setOnCheckedChangeListener(this)
            val timQQ: Switch = findViewById(R.id.timQQ) as Switch
            timQQ.isChecked = sharedPreferences.getBoolean(KEY_TIM_QQ, true)
            timQQ.setOnCheckedChangeListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止线程
        thread.stopThread()
        finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.weCht -> {
                sharedPreferences.edit().putBoolean(KEY_WECHAT, isChecked).apply()
                writeFile(weChatFilePath, isChecked.toString())
                Toast.makeText(this, getString(R.string.restart_wechat), Toast.LENGTH_SHORT).show()
            }
            R.id.timQQ -> {
                sharedPreferences.edit().putBoolean(KEY_TIM_QQ, isChecked).apply()
                writeFile(timQQFilePath, isChecked.toString())
                Toast.makeText(this, getString(R.string.restart_qq_tim), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 初始化文件操作
     */
    private fun initFile() {
        thread = MyThread(TAG, WRITE_FILE_ERROR)
        thread.start()
        if (!File(weChatFilePath).exists()) {
            writeFile(weChatFilePath, true.toString())
        }
        if (!File(timQQFilePath).exists()) {
            writeFile(timQQFilePath, true.toString())
        }
    }

    /**
     * 用于判断模块是否加载成功，通过自己Hook自己
     */
    private fun isModuleLoaded(): Boolean = false

    /**
     *  写入文件
     */
    private fun writeFile(filePath: String, content: String) {
        thread.resumeThread(filePath, content)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val weChatFilePath = Environment.getExternalStorageDirectory().path + "/scanLoginWeChat.xml"
        private val timQQFilePath = Environment.getExternalStorageDirectory().path + "/scanLoginTIMQQ.xml"
        private const val SHARED_NAME = "scanLogin"
        private const val MAIN_ACTIVITY = ".MainActivityAlias"
        private const val ICON_VISIBILITY_KEY = "iconVisibility"
        private const val ICON_VISIBILITY_ERROR = "iconVisibilityError "
        private const val KEY_WECHAT = "weChat"
        private const val KEY_TIM_QQ = "timQQ"
        private const val WRITE_FILE_ERROR = "writeFileError "
    }

}
