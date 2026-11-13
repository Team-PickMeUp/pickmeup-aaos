package com.example.pickmeup.data

import android.content.Context
import androidx.core.content.edit

/**
 * 최초 실행 약관 동의 여부를 기기에 저장합니다.
 *
 * 약관 내용이 바뀌면 [CURRENT_TERMS_VERSION]을 올리세요.
 * 저장된 버전이 현재 버전보다 낮으면 동의 화면이 다시 표시됩니다.
 */
class TermsConsentRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasAgreedToCurrentTerms(): Boolean =
        prefs.getInt(KEY_AGREED_VERSION, 0) >= CURRENT_TERMS_VERSION

    fun saveAgreement(nowMillis: Long = System.currentTimeMillis()) {
        prefs.edit {
            putInt(KEY_AGREED_VERSION, CURRENT_TERMS_VERSION)
            putLong(KEY_AGREED_AT, nowMillis)
        }
    }

    companion object {
        const val CURRENT_TERMS_VERSION = 1
        private const val PREFS_NAME = "terms_consent"
        private const val KEY_AGREED_VERSION = "agreed_version"
        private const val KEY_AGREED_AT = "agreed_at"
    }
}
