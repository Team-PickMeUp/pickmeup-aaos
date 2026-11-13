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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.terms.astaStyle
import com.example.pickmeup.ui.theme.PickMeUpColors

private const val DESIGN_WIDTH_PX = 1700f

private val TextSub = Color(0xFF646771)
private val Placeholder = Color(0xFF858894)
private val InputBorder = Color(0xFFDADCE3)
private val ButtonEnabled = Color(0xFF5A46FA)
private val ButtonDisabled = Color(0xFFB9B1FC)

/**
 * 위치 요청 화면 (Figma node 18:22356 "위치요청화면").
 *
 * - 이름: 공백이 아닌 값 필수
 * - 전화번호: 숫자 키패드, 자동 하이픈, 국내 휴대전화 형식일 때만 유효
 * - 둘 다 유효할 때만 "위치요청하기" 활성 (Figma 컴포넌트 State=Enabled/Disabled 설명 기준)
 *
 * Figma에서 숨김 처리된 "저장된 픽업 대상자" 목록과 "다음 픽업 대상자 저장" 체크박스,
 * 기획용 번호(1, 2)는 구현하지 않았습니다.
 */
@Composable
fun LocationRequestRoute(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onResponseReceived: () -> Unit,
    viewModel: LocationRequestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LocationRequestEvent.ResponseReceived -> onResponseReceived()
                is LocationRequestEvent.RequestFailed -> Unit // TODO: 실패 안내 UI (Figma 미정)
            }
        }
    }
    LocationRequestScreen(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onRequestLocation = viewModel::requestLocation,
        onBack = onBack,
        onSettingsClick = onSettingsClick,
    )
}

@Composable
fun LocationRequestScreen(
    uiState: LocationRequestUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRequestLocation: () -> Unit,
    onBack: () -> Unit,
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
        designWidthPx = DESIGN_WIDTH_PX,
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 80.px, end = 80.px, top = 48.px),
                verticalArrangement = Arrangement.spacedBy(48.px),
            ) {
                // 이전 화면
                Row(
                    Modifier
                        .height(60.px)
                        .clickable(role = Role.Button, onClick = onBack),
                    horizontalArrangement = Arrangement.spacedBy(16.px),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.px),
                    )
                    Text(
                        text = stringResource(R.string.location_request_title),
                        style = astaStyle(32.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary),
                    )
                }

                // 안내
                Column(verticalArrangement = Arrangement.spacedBy(20.px)) {
                    Text(
                        text = stringResource(R.string.main_greeting),
                        style = astaStyle(60.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 80f / 60f),
                    )
                    Text(
                        text = stringResource(R.string.location_request_guide),
                        style = astaStyle(30.pxSp, FontWeight.Normal, TextSub, 44f / 30f),
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
                    verticalArrangement = Arrangement.spacedBy(28.px),
                ) {
                    Text(
                        text = stringResource(R.string.location_request_new_target),
                        style = astaStyle(36.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary),
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
                        style = astaStyle(25.pxSp, FontWeight.Normal, TextSub),
                    )
                }
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

@Composable
private fun LabeledInput(
    label: String,
    placeholder: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    val inputStyle: TextStyle = astaStyle(30.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary)
    Column(verticalArrangement = Arrangement.spacedBy(14.px)) {
        Text(
            text = label,
            style = astaStyle(28.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary),
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
                        Text(text = placeholder, style = inputStyle.copy(color = Placeholder))
                    }
                    innerTextField()
                }
            },
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
        onNameChange = {}, onPhoneChange = {}, onRequestLocation = {},
        onBack = {}, onSettingsClick = {},
    )
}
