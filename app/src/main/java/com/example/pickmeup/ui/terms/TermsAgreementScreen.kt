package com.example.pickmeup.ui.terms

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.example.pickmeup.R
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.AstaSans
import com.example.pickmeup.ui.theme.PickMeUpColors

/** 약관 조항: 제목 + 본문 */
data class TermsArticle(@StringRes val title: Int, @StringRes val body: Int)

// Figma 약관 상세 화면의 예시 조항 (위치기반서비스·개인정보 화면 모두 같은 문구가 들어 있음)
private val FigmaSampleArticles = listOf(
    TermsArticle(R.string.terms_sample_article1_title, R.string.terms_sample_article1_body),
    TermsArticle(R.string.terms_sample_article2_title, R.string.terms_sample_article2_body),
    TermsArticle(R.string.terms_sample_article3_title, R.string.terms_sample_article3_body),
)

/** 동의 항목. 모두 필수입니다. */
enum class TermsType(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val intro: Int,
    val articles: List<TermsArticle>,
) {
    Service(
        R.string.terms_service_title,
        R.string.terms_service_desc,
        R.string.terms_service_intro,
        listOf(TermsArticle(R.string.terms_service_placeholder_title, R.string.terms_service_placeholder_body)),
    ),
    Privacy(
        R.string.terms_privacy_title,
        R.string.terms_privacy_desc,
        R.string.terms_privacy_intro,
        FigmaSampleArticles,
    ),
    Location(
        R.string.terms_location_title,
        R.string.terms_location_desc,
        R.string.terms_location_intro,
        FigmaSampleArticles,
    ),
}

/**
 * 최초 접속 시 이용약관 동의 화면 (Figma node 149:17350 "픽미업 최초 실행 동의 팝업").
 *
 * 상태바·클러스터(driving_view)·하단 GNB는 Pleos 시스템 UI이므로 여기서 그리지 않고,
 * 앱 창 영역만 그립니다.
 */
@Composable
fun TermsAgreementRoute(
    onAgreed: () -> Unit,
    onDisagree: () -> Unit,
    onViewDetail: (TermsType) -> Unit,
    viewModel: TermsAgreementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TermsAgreementScreen(
        uiState = uiState,
        onCheckedChange = viewModel::onCheckedChange,
        onCheckAllChange = viewModel::onCheckAllChange,
        onAgreeAndStart = { if (viewModel.agree()) onAgreed() },
        onDisagree = onDisagree,
        onViewDetail = onViewDetail,
    )
}

@Composable
fun TermsAgreementScreen(
    uiState: TermsAgreementUiState,
    onCheckedChange: (TermsType, Boolean) -> Unit,
    onCheckAllChange: (Boolean) -> Unit,
    onAgreeAndStart: () -> Unit,
    onDisagree: () -> Unit,
    onViewDetail: (TermsType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allChecked = uiState.allChecked

    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 64.px, end = 64.px, top = 48.px, bottom = 64.px),
        ) {
            // 상단 헤더
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(96.px),
                horizontalArrangement = Arrangement.spacedBy(24.px),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Figma "뒤로 가기" 슬롯은 비어 있음(최초 실행이라 돌아갈 곳이 없음). 자리만 유지.
                Spacer(Modifier.size(width = 50.px, height = 80.px))
                Text(
                    text = stringResource(R.string.terms_screen_title),
                    style = astaStyle(52.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary),
                )
            }

            // 동의 팝업 영역
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 76.px, end = 76.px, top = 32.px),
                verticalArrangement = Arrangement.spacedBy(40.px),
            ) {
                // 팝업 헤더
                Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                    Text(
                        text = stringResource(R.string.terms_guide),
                        style = astaStyle(32.pxSp, FontWeight.Normal, PickMeUpColors.TextSecondary, 1.4f),
                    )
                    Text(
                        text = stringResource(R.string.terms_guide_sub),
                        style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextSecondary, 1.5f),
                    )
                }

                // 동의 항목 리스트
                Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                    TermsType.entries.forEach { type ->
                        TermsItemCard(
                            type = type,
                            checked = type in uiState.checked,
                            onCheckedChange = { onCheckedChange(type, it) },
                            onViewDetail = { onViewDetail(type) },
                        )
                    }
                }

                // 선택 영역
                Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = allChecked,
                                role = Role.Checkbox,
                                onValueChange = onCheckAllChange,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(16.px),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CheckMark(checked = allChecked)
                        Text(
                            text = stringResource(R.string.terms_agree_all),
                            style = astaStyle(28.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.terms_agree_all_optional),
                            style = astaStyle(22.pxSp, FontWeight.SemiBold, PickMeUpColors.TextSecondary),
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.px),
                    ) {
                        ActionButton(
                            text = stringResource(R.string.terms_agree_and_start),
                            enabled = allChecked,
                            primaryStyle = true,
                            onClick = onAgreeAndStart,
                            modifier = Modifier.weight(1f),
                        )
                        ActionButton(
                            text = stringResource(R.string.terms_disagree),
                            enabled = true,
                            primaryStyle = false,
                            onClick = onDisagree,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Text(
                        text = stringResource(R.string.terms_footnote),
                        style = astaStyle(22.pxSp, FontWeight.Normal, PickMeUpColors.TextSecondary, 1.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsItemCard(
    type: TermsType,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
) {
    val shape = RoundedCornerShape(24.px)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PickMeUpColors.Surface)
            .border(1.px, PickMeUpColors.Border, shape)
            .padding(24.px),
        verticalArrangement = Arrangement.spacedBy(16.px),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
            horizontalArrangement = Arrangement.spacedBy(16.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckMark(checked = checked)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.px)) {
                Text(
                    text = stringResource(type.title),
                    style = astaStyle(28.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary),
                )
                Text(
                    text = stringResource(type.description),
                    style = astaStyle(22.pxSp, FontWeight.Normal, PickMeUpColors.TextSecondary, 1.5f),
                )
            }
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(PickMeUpColors.Fill)
                    .padding(horizontal = 14.px, vertical = 8.px),
            ) {
                Text(
                    text = stringResource(R.string.terms_required),
                    style = astaStyle(20.pxSp, FontWeight.Bold, PickMeUpColors.TextDisabled),
                )
            }
        }

        Row(
            Modifier.clickable(role = Role.Button, onClick = onViewDetail),
            horizontalArrangement = Arrangement.spacedBy(8.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.terms_view_detail),
                style = astaStyle(22.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary),
            )
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.px),
            )
        }
    }
}

/**
 * 32px 원형 체크 표시.
 * Figma에는 미체크 상태만 있어서, 체크 상태 색상(진한 배경 + 흰 체크)은 임의로 정했습니다.
 */
@Composable
private fun CheckMark(checked: Boolean) {
    Box(
        Modifier
            .size(32.px)
            .clip(RoundedCornerShape(16.px))
            .background(if (checked) PickMeUpColors.TextPrimary else PickMeUpColors.Fill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = if (checked) Color.White else PickMeUpColors.TextDisabled,
            modifier = Modifier.size(18.px),
        )
    }
}

/**
 * 하단 88px 버튼.
 * - 활성 스타일(흰 배경/진한 글자) = Figma "동의하고 시작하기"
 * - 회색 스타일 = Figma "동의하지 않음"
 * 시작하기 버튼은 비활성일 때 회색 스타일로 표시합니다.
 */
@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    primaryStyle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.px)
    val whiteStyle = primaryStyle && enabled
    Box(
        modifier
            .height(88.px)
            .clip(shape)
            .background(if (whiteStyle) PickMeUpColors.Surface else PickMeUpColors.Fill)
            .border(
                1.px,
                if (whiteStyle) PickMeUpColors.BorderStrong else PickMeUpColors.Border,
                shape,
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(
                28.pxSp,
                FontWeight.Bold,
                if (whiteStyle) Color.Black else PickMeUpColors.TextDisabled,
            ),
        )
    }
}

internal fun astaStyle(
    size: TextUnit,
    weight: FontWeight,
    color: Color,
    lineHeightMultiplier: Float? = null,
) = TextStyle(
    fontFamily = AstaSans,
    fontWeight = weight,
    fontSize = size,
    color = color,
    lineHeight = if (lineHeightMultiplier != null) size * lineHeightMultiplier else TextUnit.Unspecified,
)

@Preview(widthDp = 1700, heightDp = 1280, showBackground = true)
@Composable
private fun TermsAgreementScreenPreview() {
    TermsAgreementScreen(
        uiState = TermsAgreementUiState(),
        onCheckedChange = { _, _ -> }, onCheckAllChange = {},
        onAgreeAndStart = {}, onDisagree = {}, onViewDetail = {},
    )
}
