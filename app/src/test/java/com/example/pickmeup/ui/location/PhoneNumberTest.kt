package com.example.pickmeup.ui.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberTest {

    @Test
    fun digitsOf_stripsNonDigitsAndLimitsTo11() {
        assertEquals("01012345678", PhoneNumber.digitsOf("010-1234-5678 99"))
    }

    @Test
    fun format_insertsHyphensProgressively() {
        assertEquals("010", PhoneNumber.format("010"))
        assertEquals("010-12", PhoneNumber.format("01012"))
        assertEquals("010-123-4", PhoneNumber.format("0101234"))
        assertEquals("011-123-4567", PhoneNumber.format("0111234567"))
        assertEquals("010-1234-5678", PhoneNumber.format("01012345678"))
    }

    @Test
    fun isValidMobile() {
        assertTrue(PhoneNumber.isValidMobile("01012345678"))
        assertTrue(PhoneNumber.isValidMobile("0111234567"))
        assertTrue(PhoneNumber.isValidMobile("01912345678"))
        assertFalse(PhoneNumber.isValidMobile("0101234567"))   // 010은 11자리
        assertFalse(PhoneNumber.isValidMobile("01212345678"))  // 없는 식별번호
        assertFalse(PhoneNumber.isValidMobile("0212345678"))   // 유선
        assertFalse(PhoneNumber.isValidMobile(""))
    }
}
