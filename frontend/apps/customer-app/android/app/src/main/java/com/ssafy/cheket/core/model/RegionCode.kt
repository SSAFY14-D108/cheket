package com.ssafy.cheket.core.model

/**
 * 백엔드 Region 코드 (Int) ↔ 한글 지역명 매핑.
 *
 * - 쿼리 파라미터: Int 코드 사용 (11, 26, 28, ...)
 * - API 응답 region 필드: String 지역명 ("서울", "부산", ...)
 *
 * 코드 참고:
 * 11=서울, 28=인천, 26=부산, 29=광주, 27=대구, 30=대전, 31=울산,
 * 36=세종, 41=경기, 42=강원, 43=충북, 44=충남, 45=전북, 46=전남,
 * 47=경북, 48=경남, 50=제주
 */
object RegionCode {

    private val codeToName = mapOf(
        11 to "서울",
        28 to "인천",
        26 to "부산",
        29 to "광주",
        27 to "대구",
        30 to "대전",
        31 to "울산",
        36 to "세종",
        41 to "경기",
        42 to "강원",
        43 to "충북",
        44 to "충남",
        45 to "전북",
        46 to "전남",
        47 to "경북",
        48 to "경남",
        50 to "제주",
    )

    private val nameToCode: Map<String, Int> =
        codeToName.entries.associate { (code, name) -> name to code }

    /** Int 코드 → 한글 지역명 (없으면 코드 그대로 문자열) */
    fun toName(code: Int): String = codeToName[code] ?: "$code"

    /** 한글 지역명 → Int 코드 (없으면 null) */
    fun toCode(name: String): Int? = nameToCode[name]

    /** 모든 코드-이름 엔트리 (UI 필터용) */
    val entries: List<Pair<Int, String>> = codeToName.entries
        .sortedBy { it.key }
        .map { it.key to it.value }
}
