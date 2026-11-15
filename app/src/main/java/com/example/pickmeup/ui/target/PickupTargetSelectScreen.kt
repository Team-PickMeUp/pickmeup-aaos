package com.example.pickmeup.ui.target

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.example.pickmeup.ui.components.DimOverlay
import com.example.pickmeup.ui.components.GuideButton
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.components.consumeClicks
import com.example.pickmeup.ui.design.LocalPxScale
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.isCompactLayout
import com.example.pickmeup.ui.design.popupWidth
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.location.LocationShareEvent
import com.example.pickmeup.ui.location.LocationShareRequestOverlay
import com.example.pickmeup.ui.location.LocationShareRequestState
import com.example.pickmeup.ui.location.PhoneNumber
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val CardBorder = Color(0xFFE3E1ED)
private val AvatarBg = Color(0xFFF9F8FF)
private val AvatarCircle = Color(0xFFEFEDFF)
private val ManualCardBg = Color(0xFFF7F6FC)
private val ManualCardBorder = Color(0xFFDCD7EF)
private val PlusCircle = Color(0xFFEAE6FF)
private val ViewAllBg = Color(0xFFEEEAFE)
private val ViewAllBorder = Color(0xFFD6CFFF)
private val ConfirmCardBg = Color(0xFFF7F7FC)
private val ConfirmCardBorder = Color(0xFFDFDEEA)
private val ConfirmCancelBg = Color(0xFFF0F0F5)

@Composable
fun PickupTargetSelectRoute(
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onManualInputClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onResponseReceived: () -> Unit,
    viewModel: PickupTargetSelectViewModel = hiltViewModel(),
) {
    val targets by viewModel.recentTargets.collectAsStateWithLifecycle()
    val pendingConfirm by viewModel.pendingConfirm.collectAsStateWithLifecycle()
    val shareState by viewModel.shareState.collectAsStateWithLifecycle()
    // 확인 팝업이 열려 있으면 뒤로 가기로 팝업만 닫기 (대기·결과 팝업은 LocationShareRequestOverlay 가 처리)
    BackHandler(enabled = pendingConfirm != null, onBack = viewModel::onConfirmDismiss)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LocationShareEvent.Allowed -> onResponseReceived()
                is LocationShareEvent.RequestFailed -> Unit // TODO: 실패 안내 UI (Figma 미정)
            }
        }
    }
    PickupTargetSelectScreen(
        targets = targets,
        pendingConfirm = pendingConfirm,
        shareState = shareState,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onTargetClick = viewModel::onTargetClick,
        onManualInputClick = onManualInputClick,
        onViewAllClick = onViewAllClick,
        onConfirmDismiss = viewModel::onConfirmDismiss,
        onConfirmRequest = viewModel::onConfirmRequest,
        onCancelRequest = viewModel::cancelRequest,
        onDismissShareResult = viewModel::dismissShareResult,
    )
}

/**
 * 픽업 불러오기 화면 — 앱 첫 화면 (Figma 372:22957 "2-1 프로필 불러오는 화면").
 * 저장된 대상자 카드(최대 3개) + "새 픽업 대상자 입력" 카드를 가운데 정렬로 보여줍니다.
 * 저장된 대상자가 없으면 입력 카드만 보입니다. 첫 화면이라 뒤로 가기 버튼이 없습니다.
 * 좁은 창(화면 3분할, 840px)에서는 카드를 0.85배로 줄여 한 줄에 2개씩 놓습니다. 그래도 넘치면 세로 스크롤.
 *
 * - 프로필 선택 → 확인 팝업 (Figma 2-1-4) → "위치 요청하기" → 대기 팝업 (Figma 2-1-4-1)
 * - 허용되면 경유지 / 목적지 선택 화면으로, 자동 취소·거부면 팝업 확인 후 이 화면에 머묾
 */
@Composable
fun PickupTargetSelectScreen(
    targets: List<PickupTarget>,
    pendingConfirm: PickupTarget?,
    shareState: LocationShareRequestState,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTargetClick: (PickupTarget) -> Unit,
    onManualInputClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onConfirmDismiss: () -> Unit,
    onConfirmRequest: () -> Unit,
    onCancelRequest: () -> Unit,
    onDismissShareResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        val compact = isCompactLayout
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 64.px)
                    .padding(top = if (compact) 112.px else 252.px, bottom = 64.px),
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
                    style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(if (compact) 48.px else 77.px))
                // 좁은 창: 카드 4장(2×2)이 한 화면에 들어가도록 카드 영역만 0.85배
                val cardScale = LocalPxScale.current * if (compact) COMPACT_CARD_SCALE else 1f
                CompositionLocalProvider(LocalPxScale provides cardScale) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(32.px, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(32.px),
                        maxItemsInEachRow = if (compact) 2 else Int.MAX_VALUE,
                    ) {
                        targets.forEach { target ->
                            TargetCard(target = target, onClick = { onTargetClick(target) })
                        }
                        ManualInputCard(onClick = onManualInputClick)
                    }
                }
                Spacer(Modifier.height(if (compact) 48.px else 61.px))
                ViewAllButton(onClick = onViewAllClick)
            }

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

            pendingConfirm?.let { target ->
                RequestConfirmDialog(target = target, onDismiss = onConfirmDismiss, onConfirm = onConfirmRequest)
            }
            LocationShareRequestOverlay(
                state = shareState,
                onCancelRequest = onCancelRequest,
                onDismissResult = onDismissShareResult,
            )
        }
    }
}

private const val COMPACT_CARD_SCALE = 0.85f

/**
 * 선택한 대상자에게 위치를 요청할지 확인 (Figma 527:29080 "선택 프로필 픽업 확인 팝업", 880×520px).
 * 바깥을 누르면 닫힘
 */
@Composable
private fun RequestConfirmDialog(target: PickupTarget, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    DimOverlay(onOutsideClick = onDismiss) {
        val shape = RoundedCornerShape(34.px)
        Column(
            Modifier
                .popupWidth(880)
                .height(520.px)
                .dropShadow(shape, Shadow(radius = 28.px, offset = DpOffset(0.px, 10.px), color = Color(0x24000000)))
                .clip(shape)
                .background(Color.White)
                .consumeClicks()
                .padding(start = 48.px, end = 48.px, top = 78.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(56.px), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.pickup_targets_confirm_title),
                    style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 40f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(56.px))
            val cardShape = RoundedCornerShape(20.px)
            Column(
                Modifier
                    .widthIn(max = 744.px)
                    .fillMaxWidth()
                    .height(150.px)
                    .clip(cardShape)
                    .background(ConfirmCardBg)
                    .border(1.5.px, ConfirmCardBorder, cardShape)
                    .padding(horizontal = 52.px),
                verticalArrangement = Arrangement.spacedBy(11.px, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = target.name,
                    style = astaStyle(36.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 44f / 36f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = PhoneNumber.format(target.phoneDigits),
                    style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 34f / 26f),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(34.px))
            // 버튼 342×100px 두 개 (좁은 창에서는 팝업 폭에 맞춰 줄어듦)
            Row(
                Modifier
                    .widthIn(max = 744.px)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactLayout) 32.px else 60.px),
            ) {
                ConfirmButton(
                    text = stringResource(R.string.pickup_targets_confirm_cancel),
                    background = ConfirmCancelBg,
                    textColor = PickMeUpColors.TextPrimary,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                ConfirmButton(
                    text = stringResource(R.string.pickup_targets_confirm_request),
                    background = Purple,
                    textColor = Color.White,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 확인 팝업 버튼 (100px 높이) */
@Composable
private fun ConfirmButton(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(100.px)
            .clip(RoundedCornerShape(32.px))
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(32.pxSp, FontWeight.Bold, textColor, 40f / 32f),
            textAlign = TextAlign.Center,
        )
    }
}

/** 315×399px 카드 틀: 아래쪽에 이름(32px) / 보조 문구(20px, 없으면 생략) */
@Composable
private fun CardFrame(
    title: String,
    subtitle: String?,
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
        if (subtitle != null) {
            Spacer(Modifier.height(19.px))
            Text(
                text = subtitle,
                style = astaStyle(20.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 24f / 20f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
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

/** "새 픽업 대상자 입력" 카드 (Figma 382:24021): 149px 원 + 63×9px 막대 두 개로 만든 + */
@Composable
private fun ManualInputCard(onClick: () -> Unit) {
    CardFrame(
        title = stringResource(R.string.pickup_targets_manual_title),
        subtitle = null,
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

private val previewTargets = listOf(
    PickupTarget("1", "김민수", "01012345678", 3),
    PickupTarget("2", "이서연", "01012345678", 2),
    PickupTarget("3", "박지훈", "01012345678", 1),
)

@Composable
private fun SelectPreview(
    targets: List<PickupTarget> = previewTargets,
    pendingConfirm: PickupTarget? = null,
    shareState: LocationShareRequestState = LocationShareRequestState.Idle,
) {
    PickupTargetSelectScreen(
        targets = targets, pendingConfirm = pendingConfirm, shareState = shareState,
        onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {},
        onManualInputClick = {}, onViewAllClick = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetSelectPreview() = SelectPreview()

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetSelectEmptyPreview() = SelectPreview(targets = emptyList())

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetConfirmPreview() = SelectPreview(pendingConfirm = previewTargets[0])

@Preview(name = "3분할 (Connect-S-17)", widthDp = 840, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetSelectCompactPreview() = SelectPreview()

@Preview(name = "3분할 (Connect-L-12.9)", widthDp = 628, heightDp = 855, showBackground = true)
@Composable
private fun PickupTargetSelectSmallPreview() = SelectPreview()

@Preview(name = "3분할 팝업", widthDp = 840, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetConfirmCompactPreview() = SelectPreview(pendingConfirm = previewTargets[0])

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetWaitingPreview() =
    SelectPreview(shareState = LocationShareRequestState.Waiting(remainingSeconds = 300))
