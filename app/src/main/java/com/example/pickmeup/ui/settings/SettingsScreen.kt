package com.example.pickmeup.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickmeup.R
import com.example.pickmeup.ui.components.ScreenHeader
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val IconBg = Color(0xFFEEEBFF)
private val ItemDivider = Color(0xFFE5E6EC)

private data class SettingsItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val onClick: () -> Unit,
)

/**
 * 설정 화면 (Figma node 37:9324 "2-2 설정화면").
 * 이용 안내 / 약관 및 개인정보 2개 그룹으로 구성됩니다. (Figma 에서 "픽업 대상자 설정" 그룹은 숨김 처리되어 제외)
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onLocationTermsClick: () -> Unit,
    onServiceTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 64.px, end = 64.px, top = 48.px),
        ) {
            ScreenHeader(title = stringResource(R.string.settings_title), onBack = onBack)

            // 설정 메뉴 (Figma 기준 전체 높이가 창보다 길어서 스크롤)
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 76.px, end = 76.px, top = 72.px, bottom = 64.px),
                verticalArrangement = Arrangement.spacedBy(48.px),
            ) {
                Text(
                    text = stringResource(R.string.settings_intro),
                    style = astaStyle(32.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 1.4f),
                )
                SettingsGroup(
                    label = R.string.settings_section_usage,
                    items = listOf(
                        SettingsItem(
                            R.drawable.ic_settings_book,
                            R.string.settings_guide_title,
                            R.string.settings_guide_desc,
                            onAppGuideClick,
                        ),
                    ),
                )
                SettingsGroup(
                    label = R.string.settings_section_terms,
                    items = listOf(
                        SettingsItem(
                            R.drawable.ic_settings_map_pin,
                            R.string.terms_location_title,
                            R.string.settings_location_terms_desc,
                            onLocationTermsClick,
                        ),
                        SettingsItem(
                            R.drawable.ic_settings_book,
                            R.string.terms_service_title,
                            R.string.settings_service_terms_desc,
                            onServiceTermsClick,
                        ),
                        SettingsItem(
                            R.drawable.ic_settings_shield,
                            R.string.terms_privacy_title,
                            R.string.settings_privacy_desc,
                            onPrivacyPolicyClick,
                        ),
                    ),
                )
            }
        }
    }
}

/** 그룹 이름 + 흰색 카드(항목 사이 구분선) */
@Composable
private fun SettingsGroup(@StringRes label: Int, items: List<SettingsItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
        Text(
            text = stringResource(label),
            style = astaStyle(26.pxSp, FontWeight.SemiBold, PickMeUpColors.TextStrong),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.px))
                .background(Color.White),
        ) {
            items.forEachIndexed { index, item ->
                SettingsRow(item)
                if (index < items.lastIndex) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 160.px, end = 40.px)
                            .height(1.px)
                            .background(ItemDivider),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(176.px)
            .clickable(role = Role.Button, onClick = item.onClick)
            .padding(horizontal = 40.px),
        horizontalArrangement = Arrangement.spacedBy(32.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(88.px)
                .clip(RoundedCornerShape(20.px))
                .background(IconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(44.px),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.px)) {
            Text(
                text = stringResource(item.title),
                style = astaStyle(40.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary, 1.25f),
            )
            Text(
                text = stringResource(item.description),
                style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 1.4f),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(48.px),
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        onBack = {},
        onAppGuideClick = {},
        onLocationTermsClick = {},
        onServiceTermsClick = {},
        onPrivacyPolicyClick = {},
    )
}
