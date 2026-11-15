package com.example.pickmeup.ui.target

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.example.pickmeup.R
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.components.DimOverlay
import com.example.pickmeup.ui.components.consumeClicks
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.isCompactLayout
import com.example.pickmeup.ui.design.popupWidth
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.location.PhoneNumber
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val ConfirmCardBg = Color(0xFFF7F7FC)
private val ConfirmCardBorder = Color(0xFFDFDEEA)
private val ConfirmCancelBg = Color(0xFFF0F0F5)

/**
 * 선택한 대상자에게 위치를 요청할지 확인 (Figma 527:29080 "선택 프로필 픽업 확인 팝업", 880×520px).
 * 픽업 불러오기(2-1-4)와 전체 프로필 보기(2-1-1 줄 선택, 2-1-1-2 새 대상자 추가 후)에서 같이 씁니다.
 * 바깥을 누르면 닫힘
 */
@Composable
internal fun RequestConfirmDialog(target: PickupTarget, onDismiss: () -> Unit, onConfirm: () -> Unit) {
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

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun RequestConfirmDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        RequestConfirmDialog(PickupTarget("1", "김민수", "01012345678", 1), onDismiss = {}, onConfirm = {})
    }
}
