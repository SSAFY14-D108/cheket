package com.ssafy.cheket.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * CHEKET 앱 공통 날짜/시간 포맷터.
 *
 * ISO 형식(2026-03-15T14:30:00)을 한국어 친화적으로 변환.
 * "T" 제거, 요일 표시, 상대 표현 등을 통일합니다.
 */
object DateTimeUtils {

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val koLocale = Locale.KOREAN

    /**
     * "2026.03.15" 형식
     */
    fun formatDate(iso: String): String = try {
        val date = parseDate(iso)
        "${date.year}.${date.monthValue.pad()}.${date.dayOfMonth.pad()}"
    } catch (_: Exception) { iso.take(10).replace("-", ".") }

    /**
     * "2026.03.15 14:30" 형식
     */
    fun formatDateTime(iso: String): String = try {
        val dt = parseDateTime(iso)
        "${dt.year}.${dt.monthValue.pad()}.${dt.dayOfMonth.pad()} ${dt.hour.pad()}:${dt.minute.pad()}"
    } catch (_: Exception) { iso.replace("T", " ").take(16) }

    /**
     * "3월 15일 (토)" 형식
     */
    fun formatShowDate(iso: String): String = try {
        val date = parseDate(iso)
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, koLocale)
        "${date.monthValue}월 ${date.dayOfMonth}일 ($dayOfWeek)"
    } catch (_: Exception) { iso.take(10) }

    /**
     * "3월 15일 (토) 19:00" 형식
     */
    fun formatShowDateTime(iso: String): String = try {
        val dt = parseDateTime(iso)
        val dayOfWeek = dt.dayOfWeek.getDisplayName(TextStyle.SHORT, koLocale)
        "${dt.monthValue}월 ${dt.dayOfMonth}일 ($dayOfWeek) ${dt.hour.pad()}:${dt.minute.pad()}"
    } catch (_: Exception) { iso.replace("T", " ").take(16) }

    /**
     * "3/15 (토) 20:00 오픈" — 예매 오픈일 표시용
     */
    fun formatOpenDate(iso: String): String = try {
        val dt = parseDateTime(iso)
        val dayOfWeek = dt.dayOfWeek.getDisplayName(TextStyle.SHORT, koLocale)
        "${dt.monthValue}/${dt.dayOfMonth} ($dayOfWeek) ${dt.hour.pad()}:${dt.minute.pad()} 오픈"
    } catch (_: Exception) { iso.replace("T", " ").take(16) }

    /**
     * "공연 3/15 ~ 3/17" — 공연 기간 표시용
     */
    fun formatDateRange(startIso: String, endIso: String): String = try {
        val start = parseDate(startIso)
        val end = parseDate(endIso)
        if (start == end) {
            "공연 ${start.monthValue}/${start.dayOfMonth}"
        } else {
            "공연 ${start.monthValue}/${start.dayOfMonth} ~ ${end.monthValue}/${end.dayOfMonth}"
        }
    } catch (_: Exception) { "" }

    /**
     * "3/15" — 짧은 날짜 (공연 목록 카드용)
     */
    fun formatShortDate(iso: String): String = try {
        val date = parseDate(iso)
        "${date.monthValue}/${date.dayOfMonth}"
    } catch (_: Exception) { iso.take(10) }

    /**
     * "3/15 20:00" — 짧은 날짜+시간 (오픈/마감 뱃지용)
     */
    fun formatShortDateTime(iso: String): String = try {
        val dt = parseDateTime(iso)
        "${dt.monthValue}/${dt.dayOfMonth} ${dt.hour.pad()}:${dt.minute.pad()}"
    } catch (_: Exception) { iso.replace("T", " ").take(16) }

    /**
     * ISO "T" 제거 — fallback용
     */
    fun removeT(dateStr: String): String = dateStr.replace("T", " ")

    // ── Internal ──

    private fun parseDateTime(iso: String): LocalDateTime = try {
        LocalDateTime.parse(iso, isoFormatter)
    } catch (_: Exception) {
        LocalDateTime.parse(iso.take(19), isoFormatter)
    }

    private fun parseDate(iso: String): LocalDate = try {
        LocalDate.parse(iso.take(10), dateOnlyFormatter)
    } catch (_: Exception) {
        parseDateTime(iso).toLocalDate()
    }

    private fun Int.pad(): String = toString().padStart(2, '0')
}
