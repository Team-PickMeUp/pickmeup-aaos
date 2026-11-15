package com.example.pickmeup.ui.target

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.example.pickmeup.R
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.components.DimOverlay
import com.example.pickmeup.ui.components.consumeClicks
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.popupWidth
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.location.PhoneNumber
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
/** 저장 불가(입력값이 올바르지 않음) — 위치요청하기 버튼 Disabled 색과 같음 */
private val PurpleDisabled = Color(0xFFB9B1FC)
private val CancelBg = Color(0xFFF0F0F5)
private val FieldBg = Color(0xFFFAFAFC)
private val FieldBorder = Color(0xFFD9DBE3)

/**
 * 픽업 대상자 프로필 수정 팝업 (Figma 507:28042 "프로필 정보 수정 팝업", 960×800px).
 * - 저장된 이름·전화번호가 채워진 채로 열림
 * - 이름이 비었거나 전화번호가 휴대전화 형식이 아니면 "저장하기" 비활성
 * - 입력 중인 값을 잃지 않도록 바깥을 눌러도 닫히지 않음 ("취소" 또는 뒤로 가기로 닫기)
 */
@Composable
internal fun ProfileEditDialog(
    state: ProfileEditState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    DimOverlay {
        val shape = RoundedCornerShape(32.px)
        Column(
            Modifier
                .popupWidth(960)
                .height(800.px)
                .dropShadow(shape, Shadow(radius = 32.px, offset = DpOffset(0.px, 12.px), color = Color(0x1F000000)))
                .clip(shape)
                .background(Color.White)
                .consumeClicks()
                .padding(horizontal = 64.px),
            verticalArrangement = Arrangement.spacedBy(64.px, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 팝업 헤더
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.px),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.profile_edit_title),
                    style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 40f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.profile_edit_desc),
                    style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                    textAlign = TextAlign.Center,
                )
            }
            EditField(
                label = stringResource(R.string.location_request_name_label),
                value = state.name,
                onValueChange = onNameChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            )
            EditField(
                label = stringResource(R.string.location_request_phone_label),
                value = PhoneNumber.format(state.phoneDigits),
                onValueChange = onPhoneChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    },
                ),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.px)) {
                EditButton(
                    text = stringResource(R.string.profile_edit_cancel),
                    background = CancelBg,
                    textColor = PickMeUpColors.TextPrimary,
                    enabled = !state.isSaving,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                EditButton(
                    text = stringResource(R.string.profile_edit_save),
                    background = if (state.canSave) Purple else PurpleDisabled,
                    textColor = Color.White,
                    enabled = state.canSave,
                    onClick = {
                        keyboard?.hide()
                        onSave()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 라벨(26px) + 100px 입력란 */
@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.px)) {
        Text(
            text = label,
            style = astaStyle(26.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 34f / 26f),
        )
        val shape = RoundedCornerShape(16.px)
        BasicTextField(
            // 커서는 항상 끝 (전화번호는 하이픈이 자동으로 들어가서 위치를 유지하기 어려움)
            value = TextFieldValue(value, TextRange(value.length)),
            onValueChange = { onValueChange(it.text) },
            singleLine = true,
            textStyle = astaStyle(30.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary, 38f / 30f),
            cursorBrush = SolidColor(Purple),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.px)
                        .clip(shape)
                        .background(FieldBg)
                        .border(1.5.px, FieldBorder, shape)
                        .padding(horizontal = 28.px),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    innerTextField()
                }
            },
        )
    }
}

/** 하단 버튼 (Figma 404×100px, 팝업 폭을 반씩 나눠 씀) */
@Composable
private fun EditButton(
    text: String,
    background: Color,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(100.px)
            .clip(RoundedCornerShape(32.px))
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(32.pxSp, FontWeight.Bold, textColor, 40f / 32f),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun ProfileEditDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        ProfileEditDialog(
            state = ProfileEditState(
                target = PickupTarget("1", "김민수", "01012345678", 1),
                name = "김민수",
                phoneDigits = "01012345678",
            ),
            onNameChange = {}, onPhoneChange = {}, onDismiss = {}, onSave = {},
        )
    }
}
