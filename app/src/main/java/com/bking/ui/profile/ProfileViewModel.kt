package com.bking.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bking.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val registration = MutableStateFlow(RegistrationState())

    val uiState: StateFlow<ProfileUiState> = profileRepository.profile
        .combine(registration) { profile, registration ->
            Log.d(
                "BkingStartup",
                "profile loaded registered=${profile.isRegistered} email=${profile.email.isNotBlank()}"
            )
            ProfileUiState(
                email = profile.email,
                displayName = profile.displayName,
                isRegistered = profile.isRegistered,
                isProfileLoading = false,
                registration = registration
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ProfileUiState(isProfileLoading = true)
        )

    fun updateEmail(value: String) {
        registration.update { it.copy(emailInput = value, errorMessage = null) }
    }

    fun updateDisplayName(value: String) {
        registration.update { it.copy(displayNameInput = value, errorMessage = null) }
    }

    fun register() {
        val current = registration.value
        if (!EmailValidator.isValid(current.emailInput)) {
            registration.update { it.copy(errorMessage = "请输入有效邮箱。") }
            return
        }

        viewModelScope.launch {
            registration.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                profileRepository.register(
                    email = current.emailInput,
                    displayName = current.displayNameInput
                )
            }.onSuccess {
                registration.value = RegistrationState()
            }.onFailure { error ->
                registration.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "注册失败。"
                    )
                }
            }
        }
    }
}

data class ProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val isRegistered: Boolean = false,
    val isProfileLoading: Boolean = false,
    val registration: RegistrationState = RegistrationState()
)

data class RegistrationState(
    val emailInput: String = "",
    val displayNameInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class StartupDestination {
    SPLASH,
    REGISTRATION,
    MAIN_APP
}

val ProfileUiState.startupDestination: StartupDestination
    get() = when {
        isProfileLoading -> StartupDestination.SPLASH
        isRegistered -> StartupDestination.MAIN_APP
        else -> StartupDestination.REGISTRATION
    }
