package com.example.bookclub.session

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "bookclub_session",
        Context.MODE_PRIVATE
    )

    private val profilePreferences = context.getSharedPreferences(
        "bookclub_profiles",
        Context.MODE_PRIVATE
    )

    fun saveSession(userId: Long, username: String) {
        preferences.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun updateUsername(username: String) {
        preferences.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }

    fun getUserId(): Long? {
        val value = preferences.getLong(KEY_USER_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getUsername(): String? {
        return preferences.getString(KEY_USERNAME, null)
    }

    fun saveProfileImageUri(userId: Long, imageUri: String) {
        profilePreferences.edit()
            .putString(profileImageKey(userId), imageUri)
            .apply()
    }

    fun getProfileImageUri(userId: Long): String? {
        return profilePreferences.getString(profileImageKey(userId), null)
    }

    fun logout() {
        preferences.edit().clear().apply()
    }

    private fun profileImageKey(userId: Long): String {
        return "$KEY_PROFILE_IMAGE_PREFIX$userId"
    }

    companion object {
        private const val KEY_USER_ID = "logged_user_id"
        private const val KEY_USERNAME = "logged_username"
        private const val KEY_PROFILE_IMAGE_PREFIX = "profile_image_uri_"
    }
}