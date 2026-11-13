package com.example.pickmeup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pickmeup.data.terms.TermsConsentRepository
import com.example.pickmeup.navigation.PickMeUpNavHost
import com.example.pickmeup.ui.theme.PickMeUpTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var consentRepository: TermsConsentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startWithTerms = !consentRepository.hasAgreedToCurrentTerms()
        setContent {
            PickMeUpTheme {
                PickMeUpNavHost(
                    startWithTerms = startWithTerms,
                    // 약관에 동의하지 않으면 앱을 시작하지 않고 종료 → 런처로 돌아감
                    onExit = { finish() },
                )
            }
        }
    }
}
