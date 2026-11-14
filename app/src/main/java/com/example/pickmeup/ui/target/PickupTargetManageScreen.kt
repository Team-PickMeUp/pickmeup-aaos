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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.components.ScreenHeader
import com.example.pickmeup.ui.components.consumeClicks
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.location.PhoneNumber
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

@Composable
fun PickupTargetManageRoute(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTargetSelected: (PickupTarget) -> Unit,
    viewModel: PickupTargetManageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 삭제 / 수정 팝업이 열려 있으면 뒤로 가기로 팝업만 닫기
    BackHandler(enabled = uiState.pendingDelete != null, onBack = viewModel::onDeleteDismiss)
    BackHandler(enabled = uiState.editing != null, onBack = viewModel::onEditDismiss)
    PickupTargetManageScreen(
        uiState = uiState,
        onBack = onBack,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onTargetSelected = onTargetSelected,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onEditClick = viewModel::onEditClick,
        onEditNameChange = viewModel::onEditNameChange,
        onEditPhoneChange = viewModel::onEditPhoneChange,
        onEditDismiss = viewModel::onEditDismiss,
        onEditSave = viewModel::onEditSave,
    )
}

/**
 * 저장된 픽업 대상자 목록 (Figma 388:25033 "2-1-1", 삭제 확인 팝업 391:26088 "2-1-2", 수정 팝업 507:27177 "2-1-3").
 * - 대상자 줄 선택 → 위치 요청 (픽업 불러오기는 최근 3명만 보여서 나머지는 여기서 선택)
 * - 수정 → 이름·전화번호 수정 팝업 → 저장하면 목록에 바로 반영 (순서는 그대로)
 * - 삭제 → 확인 팝업 → 삭제하면 목록이 자동으로 당겨짐
 * - 저장된 대상자가 없으면 빈 목록 안내
 */
@Composable
fun PickupTargetManageScreen(
    uiState: PickupTargetManageUiState,
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTargetSelected: (PickupTarget) -> Unit,
    onDeleteClick: (PickupTarget) -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onEditClick: (PickupTarget) -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditPhoneChange: (String) -> Unit,
    onEditDismiss: () -> Unit,
    onEditSave: () -> Unit,
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
                modifier = Modifier.padding(start = 64.px, end = 64.px, top = 48.px),
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
                    .fillMaxSize()
                    .padding(top = 183.px),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.pickup_targets_manage_title),
                    style = astaStyle(56.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 72f / 56f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(134.px))
                if (uiState.targets.isEmpty()) {
                    EmptyNotice()
                } else {
                    LazyColumn(
                        Modifier.width(1330.px),
                        verticalArrangement = Arrangement.spacedBy(23.px),
                        contentPadding = PaddingValues(bottom = 48.px),
                    ) {
                        items(uiState.targets, key = { it.id }) { target ->
                            TargetRow(
                                target = target,
                                onClick = { onTargetSelected(target) },
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
        }
    }
}

/** 대상자 한 줄 (Figma "전체 프로필 / 이름", 1330×137px) */
@Composable
private fun TargetRow(
    target: PickupTarget,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.px)
    Row(
        modifier
            .fillMaxWidth()
            .height(137.px)
            .clip(shape)
            .background(Color.White)
            .border(2.px, RowBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 34.px, end = 54.px),
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
                .size(width = 190.px, height = 69.px)
                .border(1.px, EditBorder, RoundedCornerShape(16.px)),
            cornerRadius = 16,
        )
        Spacer(Modifier.width(24.px))
        PopupButton(
            text = stringResource(R.string.pickup_targets_manage_delete),
            background = DeleteRed,
            textColor = Color.White,
            fontSize = 28,
            lineHeight = 36,
            onClick = onDeleteClick,
            modifier = Modifier.size(width = 190.px, height = 69.px),
            cornerRadius = 16,
        )
    }
}

/** 빈 목록 안내 (Figma 398:27120, 1330×458px) */
@Composable
private fun EmptyNotice() {
    Column(
        Modifier
            .size(width = 1330.px, height = 458.px)
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
                .size(width = 806.px, height = 469.px)
                .clip(RoundedCornerShape(34.px))
                .background(Color.White)
                .consumeClicks()
                .padding(top = 50.px),
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
            Row(horizontalArrangement = Arrangement.spacedBy(51.px)) {
                PopupButton(
                    text = stringResource(R.string.pickup_targets_delete_cancel),
                    background = CancelBg,
                    textColor = PickMeUpColors.TextPrimary,
                    onClick = onDismiss,
                    enabled = enabled,
                    modifier = Modifier.size(width = 320.px, height = 75.px),
                )
                PopupButton(
                    text = stringResource(R.string.pickup_targets_delete_confirm),
                    background = DeleteRed,
                    textColor = Color.White,
                    onClick = onConfirm,
                    enabled = enabled,
                    modifier = Modifier.size(width = 320.px, height = 75.px),
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
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetSelected = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetManageEmptyPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetSelected = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupTargetDeleteDialogPreview() {
    PickupTargetManageScreen(
        uiState = PickupTargetManageUiState(targets = previewTargets, pendingDelete = previewTargets[0]),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetSelected = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
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
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onTargetSelected = {},
        onDeleteClick = {}, onDeleteDismiss = {}, onDeleteConfirm = {},
        onEditClick = {}, onEditNameChange = {}, onEditPhoneChange = {}, onEditDismiss = {}, onEditSave = {},
    )
}
