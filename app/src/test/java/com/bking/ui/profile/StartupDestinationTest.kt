package com.bking.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class StartupDestinationTest {
    @Test
    fun `shows splash while saved profile is loading`() {
        val state = ProfileUiState(isProfileLoading = true, isRegistered = false)

        assertEquals(StartupDestination.SPLASH, state.startupDestination)
    }

    @Test
    fun `shows main app when saved profile is registered`() {
        val state = ProfileUiState(isProfileLoading = false, isRegistered = true)

        assertEquals(StartupDestination.MAIN_APP, state.startupDestination)
    }

    @Test
    fun `shows registration only after loading an unregistered profile`() {
        val state = ProfileUiState(isProfileLoading = false, isRegistered = false)

        assertEquals(StartupDestination.REGISTRATION, state.startupDestination)
    }
}
