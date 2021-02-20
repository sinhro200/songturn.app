package com.sinhro.musicord.storage

import android.content.Context
import android.content.SharedPreferences

class Storage private constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private var globalStorage: Storage? = null
        private val TAG = "Shared preferences"
        private fun getSharedPrefs(context: Context): SharedPreferences? {
            return context.getSharedPreferences(
                APPLICATION_SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
        }

        @Throws(StorageException::class)
        fun initGlobal(context: Context) {
            globalStorage = Storage(
                getSharedPrefs(context) ?: throw StorageException()
            )
        }

        fun getGlobalInstance(): Storage? {
            return globalStorage
        }

        @Throws(StorageException::class)
        public fun getInstance(context: Context): Storage {
            val shPrefs = getSharedPrefs(context) ?: throw StorageException()
            return Storage(shPrefs)
        }
    }


    fun save(name: String, value: String?) {
        sharedPreferences.edit()
            .putString(name, value)
            .apply()
        android.util.Log.i(TAG, "saved : [$name] = [$value]")
    }

    fun get(name: String): String? = sharedPreferences.getString(name, "")

    fun saveInt(name: String, value: Int) {
        sharedPreferences.edit()
            .putInt(name, value)
            .apply()
        android.util.Log.i(TAG, "saved : [$name] = [$value]")
    }

    fun getInt(name: String) = sharedPreferences.getInt(name, 0)

    fun saveLong(name: String, value: Long) {
        sharedPreferences.edit()
            .putLong(name, value)
            .apply()
        android.util.Log.i(TAG, "saved : [$name] = [$value]")
    }

    fun getLong(name: String) = sharedPreferences.getLong(name, 0L)

    fun clearValues(values: List<String>) {
        val editor = sharedPreferences.edit()
        for (v in values) {
            editor.remove(v)
            android.util.Log.i(TAG, "removed : [$v] ")
        }
        editor.apply()
    }


    class StorageException : Exception(text) {
        companion object {
            private const val text = "Can not get shared preferences"
        }

        override fun toString(): String {
            return super.toString()
        }
    }
}