package com.example.pickmeup.ui.guide

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.example.pickmeup.R
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val Body = Color(0xFF4B4E5C)
private val ExampleBg = Color(0xFFF3F2FE)
private val ExampleLabel = Color(0xFF7B78E8)
private val CardBorder = Color(0xFFDADCE3)
private val Placeholder = Color(0xFF858894)
private val ButtonDisabled = Color(0xFFB9B1FC)
private val SmsBubble = Color(0xFFE9E8F4)
private val SmsText = Color(0xFF2D2F3C)
private val Divider = Color(0x1A131417)

private val DOT_INDICATORS = listOf(
    R.drawable.ic_carousel_dots_1,
    R.drawable.ic_carousel_dots_2,
    R.drawable.ic_carousel_dots_3,
)

/**
 * 앱 사용 가이드 (Figma "앱 사용 가이드 화면 (1~3단계)", node 47:10419 / 64:11617 / 64:12716).
 * 좌우로 넘기는 3장짜리 캐러셀이고, 왼쪽 그림 영역은 실제 화면을 흉내 낸 예시라 눌리지 않습니다.
 */
@Composable
fun AppGuideScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { DOT_INDICATORS.size }

    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 80.px, vertical = 48.px),
                verticalArrangement = Arrangement.spacedBy(32.px),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 헤더
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onBack),
                    horizontalArrangement = Arrangement.spacedBy(16.px),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_chevron),
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(30.72.px),
                    )
                    Text(
                        text = stringResource(R.string.guide_title),
                        style = astaStyle(44.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 44f),
                    )
                }

                // 캐러셀 슬라이드
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    pageSpacing = 80.px,
                ) { page ->
                    when (page) {
                        0 -> Step1Slide()
                        1 -> Step2Slide()
                        else -> Step3Slide()
                    }
                }

                // 캐러셀 인디케이터
                val pageDescription = stringResource(
                    R.string.guide_page_description,
                    pagerState.currentPage + 1,
                    pagerState.pageCount,
                )
                Icon(
                    painter = painterResource(DOT_INDICATORS[pagerState.currentPage]),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(width = 68.px, height = 16.px)
                        .semantics { contentDescription = pageDescription },
                )
            }

            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 23.px),
            )
        }
    }
}

// ───────────────────────── 슬라이드 ─────────────────────────

/** 이미지 영역(640px) + 간격 40px + 안내 텍스트 영역 */
@Composable
private fun SlideLayout(
    image: @Composable ColumnScope.() -> Unit,
    text: @Composable ColumnScope.() -> Unit,
    textTopPadding: Int? = null,
    textGap: Int = 28,
) {
    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(40.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .width(640.px)
                .fillMaxHeight()
                // 3단계 카드처럼 영역보다 큰 그림도 잘리지 않고 가운데 기준으로 넘치게
                .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
            verticalArrangement = Arrangement.spacedBy(16.px, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = image,
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(if (textTopPadding != null) Modifier.padding(top = textTopPadding.px) else Modifier),
            verticalArrangement = if (textTopPadding != null) {
                Arrangement.spacedBy(textGap.px)
            } else {
                Arrangement.spacedBy(textGap.px, Alignment.CenterVertically)
            },
            content = text,
        )
    }
}

@Composable
private fun Step1Slide() {
    SlideLayout(
        image = {
            // Figma "가이드 이미지 컨테이너"는 비어 있는 자리(94.4px)라 공간만 유지
            Spacer(Modifier.height(94.395.px))
            MockNewTargetCard()
        },
        text = {
            StepBadge(R.string.guide_step1_badge, R.string.guide_step1_badge_label)
            Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                NumberedHeader(1, R.string.guide_step1_head1)
                Column(
                    Modifier.padding(start = 48.px),
                    verticalArrangement = Arrangement.spacedBy(12.px),
                ) {
                    Text(
                        text = stringResource(R.string.guide_step1_body1),
                        style = astaStyle(30.pxSp, FontWeight.Normal, Body, 46f / 30f),
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.px))
                            .background(ExampleBg)
                            .padding(horizontal = 20.px, vertical = 16.px),
                        verticalArrangement = Arrangement.spacedBy(4.px),
                    ) {
                        Text(
                            text = stringResource(R.string.guide_step1_example_label),
                            style = astaStyle(22.pxSp, FontWeight.Medium, ExampleLabel, 33f / 22f),
                        )
                        Text(
                            text = stringResource(R.string.guide_step1_example),
                            style = astaStyle(28.pxSp, FontWeight.Normal, Body, 44f / 28f),
                        )
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.px)
                    .background(Divider),
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                NumberedHeader(2, R.string.guide_step1_head2)
                Text(
                    text = stringResource(R.string.guide_step1_body2),
                    style = astaStyle(28.pxSp, FontWeight.Normal, Body, 44f / 28f),
                    modifier = Modifier.padding(start = 48.px),
                )
            }
        },
    )
}

@Composable
private fun Step2Slide() {
    SlideLayout(
        image = {
            // 문자메시지 예시 말풍선
            Box(
                Modifier
                    .width(640.px)
                    .dropShadow(
                        RoundedCornerShape(16.px),
                        Shadow(radius = 12.px, offset = DpOffset(0.px, 6.px), color = Color(0x14000000)),
                    )
                    .clip(RoundedCornerShape(16.px))
                    .background(Color.White)
                    .padding(24.px),
            ) {
                Text(
                    text = stringResource(R.string.guide_step2_sms),
                    style = astaStyle(22.pxSp, FontWeight.Normal, SmsText, 34f / 22f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.px, topEnd = 16.px, bottomEnd = 16.px, bottomStart = 4.px))
                        .background(SmsBubble)
                        .padding(horizontal = 20.px, vertical = 16.px),
                )
            }
        },
        // Figma 2단계는 텍스트 영역이 가운데 정렬이 아니라 위에서 366px
        textTopPadding = 366,
        textGap = 29,
        text = {
            StepBadge(R.string.guide_step2_badge, R.string.guide_step2_badge_label)
            NumberedHeader(2, R.string.guide_step2_head)
        },
    )
}

@Composable
private fun Step3Slide() {
    SlideLayout(
        image = {
            Spacer(Modifier.height(94.395.px))
            MockAllowedCard()
        },
        text = {
            StepBadge(R.string.guide_step3_badge, R.string.guide_step3_badge_label)
            Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                NumberedHeader(3, R.string.guide_step3_head)
                Text(
                    text = stringResource(R.string.guide_step3_body),
                    style = astaStyle(30.pxSp, FontWeight.Normal, Body, 46f / 30f),
                    modifier = Modifier.padding(start = 48.px),
                )
            }
        },
    )
}

// ───────────────────────── 공통 요소 ─────────────────────────

/** "1단계 위치요청하기" 보라색 알약 뱃지 */
@Composable
private fun StepBadge(@StringRes step: Int, @StringRes label: Int) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(Purple)
            .padding(horizontal = 20.px, vertical = 8.px),
        horizontalArrangement = Arrangement.spacedBy(8.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(step), style = astaStyle(24.pxSp, FontWeight.Bold, Color.White, 36f / 24f))
        Text(stringResource(label), style = astaStyle(24.pxSp, FontWeight.Medium, Color.White, 36f / 24f))
    }
}

/** 36px 보라 원 번호 + 굵은 제목 */
@Composable
private fun NumberedHeader(number: Int, @StringRes text: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.px)
                .clip(CircleShape)
                .background(Purple),
            contentAlignment = Alignment.Center,
        ) {
            Text(number.toString(), style = astaStyle(20.pxSp, FontWeight.Bold, Color.White, 30f / 20f))
        }
        Text(
            text = stringResource(text),
            style = astaStyle(30.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 42f / 30f),
            modifier = Modifier.weight(1f),
        )
    }
}

/** 1단계 예시 그림: 위치 요청 화면의 "새 픽업 대상자" 카드 (입력 전 상태, 눌리지 않음) */
@Composable
private fun MockNewTargetCard() {
    val cardShape = RoundedCornerShape(24.px)
    Column(
        Modifier
            .fillMaxWidth()
            .height(680.px)
            .clip(cardShape)
            .background(Color.White)
            .border(1.px, CardBorder, cardShape)
            .padding(36.px),
        verticalArrangement = Arrangement.spacedBy(28.px),
    ) {
        Text(
            text = stringResource(R.string.location_request_new_target),
            style = astaStyle(36.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary),
        )
        Column(Modifier.height(360.px), verticalArrangement = Arrangement.spacedBy(28.px)) {
            MockInput(R.string.location_request_name_label, R.string.location_request_name_hint)
            MockInput(R.string.location_request_phone_label, R.string.location_request_phone_hint)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(100.px)
                .clip(RoundedCornerShape(16.px))
                .background(ButtonDisabled),
            horizontalArrangement = Arrangement.spacedBy(14.px, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_pin),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 30.px, height = 38.px),
            )
            Text(
                text = stringResource(R.string.location_request_button),
                style = astaStyle(34.pxSp, FontWeight.Bold, Color.White),
            )
        }
    }
}

@Composable
private fun MockInput(@StringRes label: Int, @StringRes hint: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(14.px)) {
        Text(stringResource(label), style = astaStyle(28.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary))
        val shape = RoundedCornerShape(16.px)
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.px)
                .clip(shape)
                .background(Color.White)
                .border(2.px, CardBorder, shape)
                .padding(horizontal = 24.px),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(stringResource(hint), style = astaStyle(30.pxSp, FontWeight.Normal, Placeholder))
        }
    }
}

/** 3단계 예시 그림: "픽업 대상자가 위치 공유를 허용했어요" 카드 (눌리지 않음) */
@Composable
private fun MockAllowedCard() {
    val cardShape = RoundedCornerShape(24.px)
    Column(
        Modifier
            .fillMaxWidth()
            .height(950.px)
            .clip(cardShape)
            .background(Color.White)
            .border(1.px, CardBorder, cardShape)
            .padding(36.px),
        verticalArrangement = Arrangement.spacedBy(40.px),
    ) {
        Text(
            text = stringResource(R.string.guide_step3_card_title),
            style = astaStyle(36.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 44f / 36f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        // 본문 654px: 주소는 top 207.8px, 질문은 top 582px (Figma 2-3-2 기준)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Text(
                text = stringResource(R.string.guide_step3_card_location),
                style = astaStyle(48.pxSp, FontWeight.ExtraBold, PickMeUpColors.TextPrimary, 64f / 48f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 207.8.px),
            )
            Text(
                text = stringResource(R.string.guide_step3_card_question),
                style = astaStyle(30.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary, 38f / 30f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 582.px),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.px)) {
            MockPurpleButton(R.string.guide_step3_waypoint, Modifier.weight(1f))
            MockPurpleButton(R.string.guide_step3_destination, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MockPurpleButton(@StringRes text: Int, modifier: Modifier) {
    Box(
        modifier
            .height(100.px)
            .clip(RoundedCornerShape(32.px))
            .background(Purple),
        contentAlignment = Alignment.Center,
    ) {
        // Figma: 글자 폭(228px)이 패딩(24px) 안쪽 폭(216px)보다 넓어서 패딩 위로 넘쳐 보이는 구조.
        // 패딩을 두면 글자가 잘리므로 버튼 전체 폭 기준으로 가운데 정렬만 합니다.
        Text(
            text = stringResource(text),
            style = astaStyle(32.pxSp, FontWeight.Bold, Color.White, 40f / 32f),
            softWrap = false,
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun AppGuideScreenPreview() {
    AppGuideScreen(onBack = {}, onSettingsClick = {})
}
