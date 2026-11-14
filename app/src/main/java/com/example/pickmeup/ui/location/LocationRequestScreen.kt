package com.example.pickmeup.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickmeup.R
import com.example.pickmeup.ui.components.GuideButton
import com.example.pickmeup.ui.components.ScreenHeader
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val InputBorder = Color(0xFFDADCE3)
private val ButtonEnabled = Color(0xFF5A46FA)
private val ButtonDisabled = Color(0xFFB9B1FC)

@Composable
fun LocationRequestRoute(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onResponseReceived: () -> Unit,
    viewModel: LocationRequestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LocationShareEvent.Allowed -> onResponseReceived()
                is LocationShareEvent.RequestFailed -> Unit // TODO: 실패 안내 UI (Figma 미정)
            }
        }
    }
    LocationRequestScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onSaveForNextChange = viewModel::onSaveForNextChange,
        onRequestLocation = viewModel::requestLocation,
        onCancelRequest = viewModel::cancelRequest,
        onDismissShareResult = viewModel::dismissShareResult,
        onBack = onBack,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
    )
}

/**
 * 위치 요청 화면 — "새 픽업 대상자 입력" (Figma 270:16683 "4", 18:22356 "3 위치요청화면").
 * 픽업 불러오기 화면의 "새 픽업 대상자 입력" 카드에서 들어옵니다.
 *
 * - 이름: 공백이 아닌 값 필수
 * - 전화번호: 숫자 키패드, 자동 하이픈, 국내 휴대전화 형식일 때만 유효
 * - 둘 다 유효할 때만 "위치요청하기" 활성 (Figma 컴포넌트 State=Enabled/Disabled 설명 기준)
 * - "다음 픽업을 위해 대상자 저장" 체크 시 요청 성공 후 대상자 저장 → 픽업 불러오기에 표시
 * - 요청 후 대상자 응답까지 대기 팝업 + 5분 카운트다운 (Figma 4), 요청 취소 시 이 화면에 머묾
 * - 5분 무응답이면 자동 취소 팝업 (Figma 4-2), 거부하면 거부 팝업 (Figma 4-3) → 확인 시 이 화면에 머묾
 *
 * Figma에서 숨김 처리된 "저장된 픽업 대상자" 목록과 기획용 번호는 구현하지 않았습니다.
 */
@Composable
fun LocationRequestScreen(
    uiState: LocationRequestUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSaveForNextChange: (Boolean) -> Unit,
    onRequestLocation: () -> Unit,
    onCancelRequest: () -> Unit,
    onDismissShareResult: () -> Unit,
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = uiState.name
    val phoneDigits = uiState.phoneDigits

    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 80.px, end = 80.px, top = 48.px),
                verticalArrangement = Arrangement.spacedBy(48.px),
            ) {
                // 헤더: Figma 는 x 64 (본문은 x 80) → 16px 왼쪽으로. 96px 높이라 안내 문구가 y 192 에 옴
                ScreenHeader(
                    title = stringResource(R.string.location_request_title),
                    onBack = onBack,
                    modifier = Modifier.offset(x = (-16).px),
                )

                // 안내
                Column(verticalArrangement = Arrangement.spacedBy(20.px)) {
                    Text(
                        text = stringResource(R.string.location_request_heading),
                        style = astaStyle(60.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 80f / 60f),
                    )
                    Text(
                        text = stringResource(R.string.location_request_guide),
                        style = astaStyle(30.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 38f / 30f),
                    )
                }

                // 새 픽업 대상자 카드
                val cardShape = RoundedCornerShape(24.px)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(680.px)
                        .clip(cardShape)
                        .background(Color.White)
                        .border(1.px, InputBorder, cardShape)
                        .padding(36.px),
                    verticalArrangement = Arrangement.spacedBy(22.px),
                ) {
                    Text(
                        text = stringResource(R.string.location_request_new_target),
                        style = astaStyle(36.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 44f / 36f),
                    )
                    Column(
                        Modifier.height(360.px),
                        verticalArrangement = Arrangement.spacedBy(28.px),
                    ) {
                        LabeledInput(
                            label = stringResource(R.string.location_request_name_label),
                            placeholder = stringResource(R.string.location_request_name_hint),
                            value = TextFieldValue(name, TextRange(name.length)),
                            onValueChange = { onNameChange(it.text) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                        )
                        val formatted = PhoneNumber.format(phoneDigits)
                        LabeledInput(
                            label = stringResource(R.string.location_request_phone_label),
                            placeholder = stringResource(R.string.location_request_phone_hint),
                            value = TextFieldValue(formatted, TextRange(formatted.length)),
                            onValueChange = { onPhoneChange(it.text) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboard?.hide()
                                    focusManager.clearFocus()
                                },
                            ),
                        )
                    }
                    SaveForNextCheckbox(
                        checked = uiState.saveForNext,
                        onCheckedChange = onSaveForNextChange,
                    )
                    RequestLocationButton(
                        enabled = uiState.canRequest,
                        onClick = {
                            keyboard?.hide()
                            onRequestLocation()
                        },
                    )
                }

                // 위치 공유 안내
                Row(
                    Modifier.padding(bottom = 48.px),
                    horizontalArrangement = Arrangement.spacedBy(14.px),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shield_check),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.px),
                    )
                    Text(
                        text = stringResource(R.string.location_request_privacy_notice),
                        style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                    )
                }
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

            LocationShareRequestOverlay(
                state = uiState.shareState,
                onCancelRequest = onCancelRequest,
                onDismissResult = onDismissShareResult,
            )
        }
    }
}

@Composable
private fun LabeledInput(
    label: String,
    placeholder: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    val inputStyle: TextStyle = astaStyle(30.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary, 38f / 30f)
    Column(verticalArrangement = Arrangement.spacedBy(14.px)) {
        Text(
            text = label,
            style = astaStyle(28.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary, 36f / 28f),
        )
        val shape = RoundedCornerShape(16.px)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = inputStyle,
            cursorBrush = SolidColor(ButtonEnabled),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(96.px)
                        .clip(shape)
                        .background(Color.White)
                        .border(2.px, InputBorder, shape)
                        .padding(horizontal = 24.px),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.text.isEmpty()) {
                        Text(text = placeholder, style = inputStyle.copy(color = PickMeUpColors.TextMuted))
                    }
                    innerTextField()
                }
            },
        )
    }
}

/**
 * "다음 픽업을 위해 대상자 저장" (Figma 18:22388): 32px 체크박스 + 26px 문구, 줄 전체 탭 가능.
 * Figma 에는 체크 안 된 상태만 있어서, 체크 상태는 버튼 색(보라) + 흰 체크로 표시
 */
@Composable
private fun SaveForNextCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(8.px)
    Row(
        Modifier
            .fillMaxWidth()
            .height(36.px)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(14.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.px)
                .clip(shape)
                .background(if (checked) ButtonEnabled else Color.White)
                .then(if (checked) Modifier else Modifier.border(1.5.px, PickMeUpColors.TextMuted, shape)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.px),
                )
            }
        }
        Text(
            text = stringResource(R.string.location_request_save_for_next),
            style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
        )
    }
}

/** Figma 컴포넌트 "위치요청하기 버튼" (State=Enabled #5A46FA / Disabled #B9B1FC, 탭 불가) */
@Composable
private fun RequestLocationButton(enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.px)
    Row(
        Modifier
            .fillMaxWidth()
            .height(100.px)
            .clip(shape)
            .background(if (enabled) ButtonEnabled else ButtonDisabled)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
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

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun LocationRequestScreenPreview() {
    LocationRequestScreen(
        uiState = LocationRequestUiState(name = "홍길동", phoneDigits = "01012345678"),
        onNameChange = {}, onPhoneChange = {}, onSaveForNextChange = {},
        onRequestLocation = {}, onCancelRequest = {}, onDismissShareResult = {},
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun LocationRequestWaitingPreview() {
    LocationRequestScreen(
        uiState = LocationRequestUiState(
            name = "홍길동",
            phoneDigits = "01012345678",
            shareState = LocationShareRequestState.Waiting(remainingSeconds = 287),
        ),
        onNameChange = {}, onPhoneChange = {}, onSaveForNextChange = {},
        onRequestLocation = {}, onCancelRequest = {}, onDismissShareResult = {},
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {},
    )
}
