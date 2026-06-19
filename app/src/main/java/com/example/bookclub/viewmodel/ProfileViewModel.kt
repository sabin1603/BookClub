package com.example.bookclub.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.BookClubApplication
import com.example.bookclub.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserEntity? = null,
    val profileImageUri: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BookClubApplication
    private val userDao = app.database.userDao()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val userId = app.sessionManager.getUserId() ?: run {
            _uiState.value = ProfileUiState(
                errorMessage = "You must be logged in."
            )
            return
        }

        viewModelScope.launch {
            var user = userDao.findById(userId)

            val legacyProfileImage =
                app.sessionManager.getProfileImageUri(userId)

            if (
                user != null &&
                user.profileImageUri.isNullOrBlank() &&
                !legacyProfileImage.isNullOrBlank()
            ) {
                userDao.updateProfileImageUri(
                    userId = userId,
                    profileImageUri = legacyProfileImage
                )

                user = userDao.findById(userId)
            }

            _uiState.value = ProfileUiState(
                user = user,
                profileImageUri = user?.profileImageUri
                    ?: legacyProfileImage
            )
        }
    }

    fun updateProfilePicture(sourceUri: Uri) {
        val userId = app.sessionManager.getUserId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "You must be logged in."
            )
            return
        }

        viewModelScope.launch {
            try {
                val savedUri = copyImageToInternalStorage(
                    userId = userId,
                    sourceUri = sourceUri
                )

                if (savedUri == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not save profile picture.",
                        successMessage = null
                    )
                    return@launch
                }

                userDao.updateProfileImageUri(
                    userId = userId,
                    profileImageUri = savedUri
                )

                app.sessionManager.saveProfileImageUri(
                    userId = userId,
                    imageUri = savedUri
                )

                val updatedUser = userDao.findById(userId)

                _uiState.value = _uiState.value.copy(
                    user = updatedUser,
                    profileImageUri = savedUri,
                    successMessage = "Profile picture updated.",
                    errorMessage = null
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Could not update profile picture.",
                    successMessage = null
                )
            }
        }
    }

    fun updateUsername(newUsername: String) {
        val userId = app.sessionManager.getUserId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "You must be logged in."
            )
            return
        }

        val cleanUsername = newUsername.trim()

        if (cleanUsername.length < 3) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Username must have at least 3 characters.",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            val alreadyExists =
                userDao.countByUsernameExcept(cleanUsername, userId) > 0

            if (alreadyExists) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "This username is already taken.",
                    successMessage = null
                )
                return@launch
            }

            userDao.updateUsername(userId, cleanUsername)
            app.sessionManager.updateUsername(cleanUsername)

            val updatedUser = userDao.findById(userId)

            _uiState.value = _uiState.value.copy(
                user = updatedUser,
                successMessage = "Username updated.",
                errorMessage = null
            )
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val userId = app.sessionManager.getUserId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "You must be logged in."
            )
            return
        }

        if (currentPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Current password is required.",
                successMessage = null
            )
            return
        }

        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "New password must have at least 6 characters.",
                successMessage = null
            )
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "New passwords do not match.",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            val user = userDao.findById(userId)

            if (user == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "User not found.",
                    successMessage = null
                )
                return@launch
            }

            if (hashPassword(currentPassword) != user.passwordHash) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Current password is incorrect.",
                    successMessage = null
                )
                return@launch
            }

            userDao.updatePasswordHash(
                userId = userId,
                passwordHash = hashPassword(newPassword)
            )

            _uiState.value = _uiState.value.copy(
                successMessage = "Password changed.",
                errorMessage = null
            )
        }
    }

    fun logout() {
        app.sessionManager.logout()
    }

    private fun copyImageToInternalStorage(
        userId: Long,
        sourceUri: Uri
    ): String? {
        val directory = File(
            getApplication<Application>().filesDir,
            "profile_images"
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val destinationFile = File(
            directory,
            "profile_${userId}_${System.currentTimeMillis()}.jpg"
        )

        val inputStream = getApplication<Application>()
            .contentResolver
            .openInputStream(sourceUri)
            ?: return null

        inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        directory.listFiles()
            ?.filter { file ->
                file != destinationFile &&
                        (
                                file.name == "profile_$userId.jpg" ||
                                        file.name.startsWith("profile_${userId}_")
                                )
            }
            ?.forEach { oldFile ->
                oldFile.delete()
            }

        return Uri.fromFile(destinationFile).toString()
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}