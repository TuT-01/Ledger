package com.bking.ui.profile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidatorTest {
    @Test
    fun `accepts common email address`() {
        assertTrue(EmailValidator.isValid("me@example.com"))
    }

    @Test
    fun `rejects invalid email address`() {
        assertFalse(EmailValidator.isValid("not-an-email"))
    }
}

