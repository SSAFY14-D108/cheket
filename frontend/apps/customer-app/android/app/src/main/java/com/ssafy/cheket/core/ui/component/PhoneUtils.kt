package com.ssafy.cheket.core.ui.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 전화번호를 010-1234-5678 형식으로 API 전송용 변환.
 * 숫자만 추출 후 하이픈 삽입.
 */
fun formatPhoneForApi(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return when {
        digits.length == 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
        digits.length == 10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
        else -> digits
    }
}

/**
 * 전화번호 입력 시 자동으로 010-0000-0000 형식으로 표시하는 VisualTransformation.
 * 실제 값은 숫자만 저장되고, 화면에만 하이픈이 보임.
 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 3 || i == 7) append('-')
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 8) return offset - 1
                if (offset <= 13) return offset - 2
                return digits.length
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
