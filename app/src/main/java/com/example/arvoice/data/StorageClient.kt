package com.example.arvoice.data

import android.content.Context
import android.content.SharedPreferences
import com.example.arvoice.domain.AugmentedRoom
import com.google.gson.Gson

class StorageClient(context: Context): IStorageClient {

    private val AR_SHARED_PREFS = "AR_SHARED_PREFS_KEY"
    private val sharedPrefs: SharedPreferences

    init {
        sharedPrefs =
            context.getSharedPreferences(AR_SHARED_PREFS, Context.MODE_PRIVATE)
    }

    override fun storeAugmentedRoom(room: AugmentedRoom): Int {
        var shortCode = room.id
        if (shortCode == -1) {
            shortCode = getAvailableRoomCode()
            room.id = shortCode
        }
        val prefsEditor = sharedPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(room)
        prefsEditor.putString(KEY_PREFIX + shortCode, json)
        prefsEditor.commit()
        return shortCode
    }

    override fun getAugmentedRoom(shortCode: Int): AugmentedRoom? {
        val gson = Gson()
        val json = sharedPrefs.getString(KEY_PREFIX + shortCode, null)
        return try {
            gson.fromJson(json, AugmentedRoom::class.java)
        } catch (e: IllegalStateException) {
            null
        }
    }

    /** Gets a new short code that can be used to store the anchor ID.  */
    private fun getAvailableRoomCode(): Int {
        val shortCode = sharedPrefs.getInt(NEXT_SHORT_CODE, INITIAL_SHORT_CODE)

        // Increment and update the value in prefs
        sharedPrefs.edit().putInt(NEXT_SHORT_CODE, shortCode + 1).apply()
        return shortCode
    }

    companion object {
        private const val NEXT_SHORT_CODE = "next_short_code"
        private const val KEY_PREFIX = "room:"
        private const val INITIAL_SHORT_CODE = 200
    }

}