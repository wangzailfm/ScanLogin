package top.jowanxu.scanlogin.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.SharedPreferences
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.preference.PreferenceManager
import top.jowanxu.scanlogin.Constant

class SettingsProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    private lateinit var mPreferences: SharedPreferences

    init {
        uriMatcher.addURI(Constant.AUTHORITY, Constant.BOOLEAN_PATH, Constant.BOOLEAN_CODE)
    }

    override fun onCreate(): Boolean {
        // 初始化
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?,
                       selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        if (selection == null) return null

        when(uriMatcher.match(uri)) {
            Constant.BOOLEAN_CODE -> return matrixCursor(mPreferences.getBoolean(selection, true))
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException("No external updates")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("No external updates")
    }

    private inline fun <reified T> matrixCursor(value: T): Cursor {

        val cursor = MatrixCursor(arrayOf("value"), 1)

        // 添加数据
        cursor.addRow(arrayOf(value))

        return cursor
    }
}