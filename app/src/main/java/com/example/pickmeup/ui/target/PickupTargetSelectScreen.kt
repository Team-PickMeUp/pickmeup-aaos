package com.example.pickmeup.ui.target

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickmeup.R
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.components.GuideButton
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.location.PhoneNumber
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val TextStrong = Color(0xD6131417)   // rgba(19,20,23,0.84)
private val TextMuted = Color(0xA3131417)    // rgba(19,20,23,0.64)
private val CardBorder = Color(0xFFE3E1ED)
private val AvatarBg = Color(0xFFF9F8FF)
private val AvatarCircle = Color(0xFFEFEDFF)
private val ManualCardBg = Color(0xFFF7F6FC)
private val ManualCardBorder = Color(0xFFDCD7EF)
private val PlusCircle = Color(0xFFEAE6FF)
private val ViewAllBg = Color(0xFFEEEAFE)
private val ViewAllBorder = Color(0xFFD6CFFF)

@Composable
fun PickupTargetSelectRoute(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTargetSelected: (PickupTarget) -> Unit,
    onManualInputClick: () -> Unit,
    onViewAllClick: () -> Unit,
    viewModel: PickupTargetSelectViewModel = hiltViewModel(),
) {
    val targets by viewModel.recentTargets.collectAsStateWithLifecycle()
    PickupTargetSelectScreen(
        targets = targets,
        onBack = onBack,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onTargetSelected = onTargetSelected,
        onManualInputClick = onManualInputClick,
        onViewAllClick = onViewAllClick,
    )
}

/**
 * 픽업 불러오기 화면 (Figma 372:22957 "2-1 프로필 불러오는 화면").
 * 저장된 대상자 카드(최대 3개) + 직접 입력 카드를 가운데 정렬로 보여줍니다.
 * 저장된 대상자가 없으면 직접 입력 카드만 보입니다.
 */
@Composable
fun PickupTargetSelectScreen(
    targets: List<PickupTarget>,
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTargetSelected: (PickupTarget) -> Unit,
    onManualInputClick: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Box(Modifier.fillMaxSize()) {
            BackTitle(
                title = stringResource(R.string.pickup_targets_title),
                onClick = onBack,
                modifier = Modifier.padding(start = 87.px, top = 49.px),
            )
            GuideButton(
                onClick = onAppGuideClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 101.px),
            )
            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 23.px),
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 252.px),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.pickup_targets_heading),
                    style = astaStyle(60.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 80f / 60f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(11.px))
                Text(
                    text = stringResource(R.string.pickup_targets_guide),
                    style = astaStyle(26.pxSp, FontWeight.Normal, TextStrong, 34f / 26f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(77.px))
                Row(horizontalArrangement = Arrangement.spacedBy(32.px)) {
                    targets.forEach { target ->
                        TargetCard(target = target, onClick = { onTargetSelected(target) })
                    }
                    ManualInputCard(onClick = onManualInputClick)
                }
                Spacer(Modifier.height(61.px))
                ViewAllButton(onClick = onViewAllClick)
            }
        }
    }
}

/** "←  픽업 불러오기" (Figma 뒤로가기 버튼: 40px Bold) */
@Composable
private fun BackTitle(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = "←  $title",
        style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 1f),
        modifier = modifier
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.common_back),
                onClick = onClick,
            )
            .padding(start = 8.px, end = 8.px, top = 9.px, bottom = 13.px),
    )
}

/** 315×399px 카드 틀: 아래쪽에 이름(32px) / 보조 문구(20px) */
@Composable
private fun CardFrame(
    title: String,
    subtitle: String,
    background: Color,
    border: Color,
    elevated: Boolean,
    onClick: () -> Unit,
    top: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(28.px)
    Column(
        Modifier
            .size(width = 315.px, height = 399.px)
            .then(
                if (elevated) {
                    Modifier.dropShadow(shape, Shadow(radius = 20.px, offset = DpOffset(0.px, 9.px), color = Color(0x12000000)))
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(background)
            .border(2.px, border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 29.px, end = 29.px, top = 28.px),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        top()
        Spacer(Modifier.height(23.px))
        Text(
            text = title,
            style = astaStyle(32.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 40f / 32f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(19.px))
        Text(
            text = subtitle,
            style = astaStyle(20.pxSp, FontWeight.Normal, TextMuted, 24f / 20f),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** 저장된 대상자 카드 (Figma "픽업 대상자 프로필 / 이름") */
@Composable
private fun TargetCard(target: PickupTarget, onClick: () -> Unit) {
    CardFrame(
        title = target.name,
        subtitle = PhoneNumber.format(target.phoneDigits),
        background = Color.White,
        border = CardBorder,
        elevated = true,
        onClick = onClick,
    ) {
        Box(
            Modifier
                .size(width = 257.px, height = 234.px)
                .clip(RoundedCornerShape(23.px))
                .background(AvatarBg),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(163.2.px)
                    .clip(CircleShape)
                    .background(AvatarCircle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_user_round),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(81.6.px),
                )
            }
        }
    }
}

/** 직접 입력 카드 (Figma "새 대상자 직접 입력"): 149px 원 + 63×9px 막대 두 개로 만든 + */
@Composable
private fun ManualInputCard(onClick: () -> Unit) {
    CardFrame(
        title = stringResource(R.string.pickup_targets_manual_title),
        subtitle = stringResource(R.string.pickup_targets_manual_desc),
        background = ManualCardBg,
        border = ManualCardBorder,
        elevated = false,
        onClick = onClick,
    ) {
        Box(Modifier.size(width = 257.px, height = 234.px), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(149.px)
                    .clip(CircleShape)
                    .background(PlusCircle),
                contentAlignment = Alignment.Center,
            ) {
                val bar = RoundedCornerShape(4.px)
                Box(Modifier.size(width = 63.px, height = 9.px).clip(bar).background(Purple))
                Box(Modifier.size(width = 9.px, height = 63.px).clip(bar).background(Purple))
            }
        }
    }
}

/** 전체 프로필 보기 · 관리 버튼 (Figma 388:25891, 560×82px) */
@Composable
private fun ViewAllButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(19.px)
    Box(
        Modifier
            .size(width = 560.px, height = 82.px)
            .clip(shape)
            .background(ViewAllBg)
            .border(2.px, ViewAllBorder, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.pickup_targets_view_all),
            style = astaStyle(32.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 40f / 32f),
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetSelectPreview() {
    PickupTargetSelectScreen(
        targets = listOf(
            PickupTarget("1", "김민수", "01012345678", 3),
            PickupTarget("2", "이서연", "01012345678", 2),
            PickupTarget("3", "박지훈", "01012345678", 1),
        ),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {},
        onTargetSelected = {}, onManualInputClick = {}, onViewAllClick = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetSelectEmptyPreview() {
    PickupTargetSelectScreen(
        targets = emptyList(),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {},
        onTargetSelected = {}, onManualInputClick = {}, onViewAllClick = {},
    )
}
