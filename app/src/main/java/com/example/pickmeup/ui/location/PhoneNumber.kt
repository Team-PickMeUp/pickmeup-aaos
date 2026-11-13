package com.example.pickmeup.ui.location

/**
 * 국내 휴대전화 번호 입력 도우미.
 * Figma: "전화번호 입력란 · 숫자 키패드 / 자동 하이픈"
 */
object PhoneNumber {

    private const val MAX_DIGITS = 11

    // 010은 11자리만, 011/016/017/018/019는 10~11자리 허용
    private val MOBILE_REGEX = Regex("^(010\\d{8}|01[16789]\\d{7,8})$")

    /** 숫자만 남기고 최대 11자리로 자릅니다. */
    fun digitsOf(input: String): String = input.filter(Char::isDigit).take(MAX_DIGITS)

    /** 입력 중인 숫자에 자동으로 하이픈을 넣습니다. 예: 01012345678 → 010-1234-5678 */
    fun format(digits: String): String = when {
        digits.length <= 3 -> digits
        digits.length <= 6 -> "${digits.take(3)}-${digits.drop(3)}"
        digits.length <= 10 -> "${digits.take(3)}-${digits.substring(3, 6)}-${digits.drop(6)}"
        else -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.drop(7)}"
    }

    fun isValidMobile(digits: String): Boolean = MOBILE_REGEX.matches(digits)
}
