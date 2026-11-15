package com.example.pickmeup.ui.target

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickmeup.R
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.components.DimOverlay
import com.example.pickmeup.ui.components.GuideButton
import com.example.pickmeup.ui.components.HEADER_ICONS_END_PX
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.components.ScreenHeader
import com.example.pickmeup.ui.components.consumeClicks
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.isCompactLayout
import com.example.pickmeup.ui.design.popupWidth
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.location.LocationShareEvent
import com.example.pickmeup.ui.location.LocationShareRequestOverlay
import com.example.pickmeup.ui.location.LocationShareRequestState
import com.example.pickmeup.ui.location.PhoneNumber
import com.example.pickmeup.ui.location.WaitingTitle
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val RowBorder = Color(0xFFE2DFF0)
private val AvatarCircle = Color(0xFFEFEDFF)
private val DeleteRed = Color(0xFFFF4C28)
private val EmptyBg = Color(0xFFF0EFF8)
private val Dim = Color(0xFF16121F).copy(alpha = 0.45f)
private val CancelBg = Color(0xFFF1EFF9)
private val EditBg = Color(0xFFF0EDFF)
private val EditBorder = Color(0xFFD5CEFF)
private val EditText = Color(0xFF5A46FA)
private val Purple = Color(0xFF5A46FA)

@Composable
fun PickupTargetManageRoute(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onResponseReceived: () -> Unit,
    viewModel: PickupTargetManageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareState by viewModel.shareState.collectAsStateWithLifecycle()
    // 삭제 / 추가·수정 / 확인 팝업이 열려 있으면 뒤로 가기로 팝업만 닫기
    // (대기·결과 팝업은 LocationShareRequestOverlay 가 처리)
    BackHandler(enabled = uiState.pendingDelete != null, onBack = viewModel::onDeleteDismiss)
    BackHandler(enabled = uiState.editing != null, onBack = viewModel::onEditDismiss)
    BackHandler(enabled = uiState.pendingConfirm != null, onBack = viewModel::onConfirmDismiss)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LocationShareEvent.Allowed -> onResponseReceived()
                is LocationShareEvent.RequestFailed -> Unit // TODO: 실패 안내 UI (Figma 미정)
            }
        }
    }
    PickupTargetManageScreen(
        uiState = uiState,
        shareState = shareState,
        onBack = onBack,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onTargetClick = viewModel::onTargetClick,
        onAddClick = viewModel::onAddClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onEditClick = viewModel::onEditClick,
        onEditNameChange = viewModel::onEditNameChange,
        onEditPhoneChange = viewModel::onEditPhoneChange,
        onEditDismiss = viewModel::onEditDismiss,
        onEditSave = viewModel::onEditSave,
        onConfirmDismiss = viewModel::onConfirmDismiss,
        onConfirmRequest = viewModel::onConfirmRequest,
        onCancelRequest = viewModel::cancelRequest,
        onDismissShareResult = viewModel::dismissShareResult,
    )
}

/**
 * 저장된 픽업 대상자 목록 (Figma 388:25033 "2-1-1", 삭제 확인 팝업 391:26088 "2-1-2", 수정 팝업 507:27177 "2-1-3").
 * - 대상자 줄 선택 → 확인 팝업 → 이 화면에서 바로 위치 요청 + 대기 팝업 (Figma 2-1-5).
 *   픽업 불러오기는 최근 3명만 보여서 나머지는 여기서 선택
 * - "+ 추가" → 입력 팝업 (Figma 2-1-1-1) → 저장하면 목록에 추가하고 바로 확인 팝업 (Figma 2-1-1-2)
 * - 수정 → 이름·전화번호 수정 팝업 → 저장하면 목록에 바로 반영 (순서는 그대로)
 * - 삭제 → 확인 팝업 → 삭제하면 목록이 자동으로 당겨짐
 * - 저장된 대상자가 없으면 빈 목록 안내
 */
@Composable
fun PickupTargetManageScreen(
    uiState: PickupTargetManageUiState,
    shareState: LocationShareRequestState,
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTargetClick: (PickupTarget) -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (PickupTarget) -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onEditClick: (PickupTarget) -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditPhoneChange: (String) -> Unit,
    onEditDismiss: () -> Unit,
    onEditSave: () -> Unit,
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
        Box(Modifier.fillMaxSize()) {
            // Figma 헤더: 뒤로 가기 (64, 56) 80px + 제목 (168, 70) — Pleos 설정 헤더 규격
            ScreenHeader(
                title = stringResource(R.string.pickup_targets_manage_header),
                onBack = onBack,
                modifier = Modifier.padding(start = 64.px, end = HEADER_ICONS_END_PX.px, top = 48.px),
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

            val compact = isCompactLayout
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = 64.px, end = 64.px, top = 183.px),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.pickup_targets_manage_title),
                    style = astaStyle(56.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 72f / 56f),
                    textAlign = TextAlign.Center,
                )
                // Figma: 제목 아래 22px 에 보조 문구(26px), 그 아래 22px 에 "+ 추가" (목록 오른쪽 끝 = 삭제 버튼 오른쪽 끝)
                Spacer(Modifier.height(22.px))
                Text(
                    text = stringResource(R.string.pickup_targets_manage_subtitle),
                    style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.px))
                Box(
                    Modifier
                        .widthIn(max = 1330.px)
                        .fillMaxWidth()
                        .padding(end = if (compact) 34.px else 54.px),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    PopupButton(
                        text = stringResource(R.string.pickup_targets_manage_add),
                        background = Purple,
                        textColor = Color.White,
                        fontSize = 28,
                        lineHeight = 40,
                        onClick = onAddClick,
                        modifier = Modifier.size(width = if (compact) 150.px else 190.px, height = 69.px),
                        cornerRadius = 16,
                    )
                }
                Spacer(Modifier.height(if (compact) 32.px else 47.px))
                if (uiState.targets.isEmpty()) {
                    EmptyNotice()
                } else {
                    val listState = rememberLazyListState()
                    // 새 대상자는 목록 맨 위에 추가됨 → 스크롤돼 있어도 보이도록 맨 위로 (삭제·수정 때는 그대로)
                    val knownIds = remember { mutableSetOf<String>() }
                    val topId = uiState.targets.first().id
                    LaunchedEffect(topId) {
                        if (knownIds.isNotEmpty() && topId !in knownIds) listState.animateScrollToItem(0)
                        knownIds += uiState.targets.map { it.id }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .widthIn(max = 1330.px)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(23.px),
                        contentPadding = PaddingValues(bottom = 48.px),
                    ) {
                        items(uiState.targets, key = { it.id }) { target ->
                            TargetRow(
                                target = target,
                                onClick = { onTargetClick(target) },
                                onEditClick = { onEditClick(target) },
                                onDeleteClick = { onDeleteClick(target) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }

            uiState.pendingDelete?.let { target ->
                DeleteConfirmDialog(
                    target = target,
                    enabled = !uiState.isDeleting,
                    onDismiss = onDeleteDismiss,
                    onConfirm = onDeleteConfirm,
                )
            }
            uiState.editing?.let { editing ->
                ProfileEditDialog(
                    state = editing,
                    onNameChange = onEditNameChange,
                    onPhoneChange = onEditPhoneChange,
                    onDismiss = onEditDismiss,
                    onSave = onEditSave,
                )
            }
            uiState.pendingConfirm?.let { target ->
                RequestConfirmDialog(target = target, onDismiss = onConfirmDismiss, onConfirm = onConfirmRequest)
            }
            LocationShareRequestOverlay(
                state = shareState,
                onCancelRequest = onCancelRequest,
                onDismissResult = onDismissShareResult,
                waitingTitle = WaitingTitle.Long,
            )
        }
    }
}

/**
 * 대상자 한 줄 (Figma "전체 프로필 / 이름", 1330×137px).
 * 좁은 창(화면 3분할)에서는 이름 칸이 남도록 수정·삭제 버튼을 190 → 120px 로 줄임
 */
@Composable
private fun TargetRow(
    target: PickupTarget,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.px)
    val compact = isCompactLayout
    val buttonWidth = if (compact) 120.px else 190.px
    Row(
        modifier
            .fillMaxWidth()
            .height(137.px)
            .clip(shape)
            .background(Color.White)
            .border(2.px, RowBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 34.px, end = if (compact) 34.px else 54.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(90.88.px)
                .clip(CircleShape)
                .background(AvatarCircle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_user_round),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(45.44.px),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 27.px),
            verticalArrangement = Arrangement.spacedBy(17.px),
        ) {
            Text(
                text = target.name,
                style = astaStyle(32.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 40f / 32f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = PhoneNumber.format(target.phoneDigits),
                style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 28f / 24f),
            )
        }
        // 수정 (Figma 508:28217): 연보라 배경 + 1px 테두리, 삭제 버튼과 24px 간격
        PopupButton(
            text = stringResource(R.string.pickup_targets_manage_edit),
            background = EditBg,
            textColor = EditText,
            fontSize = 28,
            lineHeight = 36,
            onClick = onEditClick,
            modifier = Modifier
                .size(width = buttonWidth, height = 69.px)
                .border(1.px, EditBorder, RoundedCornerShape(16.px)),
            cornerRadius = 16,
        )
        Spacer(Modifier.width(if (compact) 16.px else 24.px))
        PopupButton(
            text = stringResource(R.string.pickup_targets_manage_delete),
            background = DeleteRed,
            textColor = Color.White,
            fontSize = 28,
            lineHeight = 36,
            onClick = onDeleteClick,
            modifier = Modifier.size(width = buttonWidth, height = 69.px),
            cornerRadius = 16,
        )
    }
}

/** 빈 목록 안내 (Figma 398:27120, 1330×458px) */
@Composable
private fun EmptyNotice() {
    Column(
        Modifier
            .widthIn(max = 1330.px)
            .fillMaxWidth()
            .height(458.px)
            .clip(RoundedCornerShape(24.px))
            .background(EmptyBg)
            .padding(top = 174.px),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.pickup_targets_manage_empty_title),
            style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52.26f / 40f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.px))
        Text(
            text = stringResource(R.string.pickup_targets_manage_empty_desc),
            style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 28f / 24f),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 삭제 확인 팝업 (Figma 391:26920, 806×469px).
 * 앱 화면 전체를 어둡게 덮고, 바깥을 누르면 닫힘
 */
@Composable
private fun DeleteConfirmDialog(
    target: PickupTarget,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DimOverlay(dimColor = Dim, onOutsideClick = onDismiss) {
        Column(
            Modifier
                .popupWidth(806)
                .height(469.px)
                .clip(RoundedCornerShape(34.px))
                .background(Color.White)
                .consumeClicks()
                .padding(start = 40.px, end = 40.px, top = 50.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.pickup_targets_delete_title),
                style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 40f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(27.px))
            Text(
                text = target.name,
                style = astaStyle(32.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 40f / 32f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.px))
            Text(
                text = PhoneNumber.format(target.phoneDigits),
                style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 28f / 24f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(35.px))
            Text(
                text = stringResource(R.string.pickup_targets_delete_desc),
                style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 34f / 26f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(41.px))
            // 버튼 320×75px 두 개 (좁은 창에서는 팝업 폭에 맞춰 줄어듦)
            Row(
                Modifier
                    .widthIn(max = 691.px)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactLayout) 32.px else 51.px),
            ) {
                PopupButton(
                    text = stringResource(R.string.pickup_targets_delete_cancel),
                    background = CancelBg,
                    textColor = PickMeUpColors.TextPrimary,
                    onClick = onDismiss,
                    enabled = enabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(75.px),
                )
                PopupButton(
                    text = stringResource(R.string.pickup_targets_delete_confirm),
                    background = DeleteRed,
                    textColor = Color.White,
                    onClick = onConfirm,
                    enabled = enabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(75.px),
                )
            }
        }
    }
}

/** 둥근 사각 버튼 (수정 / 삭제 / 돌아가기 / 삭제하기). 기본값은 팝업 버튼 (30px 글자, 17px 모서리) */
@Composable
private fun PopupButton(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: Int = 30,
    lineHeight: Int = 38,
    cornerRadius: Int = 17,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(cornerRadius.px))
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(fontSize.pxSp, FontWeight.Bold, textColor, lineHeight.toFloat() / fontSize),
        )
    }
}

private val previewTargets = listOf(
    PickupTarget("1", "김민수", "01012345678", 3),
    PickupTarget("2", "이서연", "01098765432", 2),
    PickupTarget("3", "박지훈", "01055556666", 1),
)

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetManagePreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(targets = previewTargets),
        shareState = LocationShareRequestState.Idle,
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(name = "3분할", widthDp = 840, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetManageCompactPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(targets = previewTargets),
        shareState = LocationShareRequestState.Idle,
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetManageEmptyPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(),
        shareState = LocationShareRequestState.Idle,
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetDeleteDialogPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(targets = previewTargets, pendingDelete = previewTargets[0]),
        shareState = LocationShareRequestState.Idle,
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetEditDialogPreview() {
    val target = previewTargets[0]
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(
            targets = previewTargets,
            editing = ProfileEditState(target, target.name, target.phoneDigits),
        ),
        shareState = LocationShareRequestState.Idle,
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(name = "2-1-1-1 추가", widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetAddDialogPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(
            targets = previewTargets,
            editing = ProfileEditState(target = null, name = "", phoneDigits = ""),
        ),
        shareState = LocationShareRequestState.Idle,
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}

@Preview(name = "2-1-5 대기", widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetWaitingPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(targets = previewTargets),
        shareState = LocationShareRequestState.Waiting(remainingSeconds = 300),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetClick = {}, onAddClick = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
        onConfirmDismiss = {}, onConfirmRequest = {}, onCancelRequest = {}, onDismissShareResult = {},
    )
}
