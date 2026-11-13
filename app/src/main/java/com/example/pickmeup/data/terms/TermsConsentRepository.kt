package com.example.pickmeup.data.terms

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 최초 실행 약관 동의 여부 저장소 */
interface TermsConsentRepository {
    fun hasAgreedToCurrentTerms(): Boolean
    fun saveAgreement(nowMillis: Long = System.currentTimeMillis())
}

/**
 * 약관 동의 여부를 기기(SharedPreferences)에 저장합니다.
 *
 * 약관 내용이 바뀌면 [CURRENT_TERMS_VERSION]을 올리세요.
 * 저장된 버전이 현재 버전보다 낮으면 동의 화면이 다시 표시됩니다.
 * 백업 대상에서 제외되어 있습니다 (res/xml/backup_rules.xml, data_extraction_rules.xml).
 */
@Singleton
class LocalTermsConsentRepository @Inject constructor(
    @ApplicationContext context: Context,
) : TermsConsentRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun hasAgreedToCurrentTerms(): Boolean =
        prefs.getInt(KEY_AGREED_VERSION, 0) >= CURRENT_TERMS_VERSION

    override fun saveAgreement(nowMillis: Long) {
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
