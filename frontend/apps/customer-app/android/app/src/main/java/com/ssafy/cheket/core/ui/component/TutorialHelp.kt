package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.White

enum class TutorialId {
    RESALE_LIST,
    RESALE_DETAIL,
    RESALE_CREATE,
    WALLET,
    WALLET_HISTORY,
    TX_HISTORY,
    TRANSFER,
    COLLECTION,
    COLLECTIBLE_TICKET_DETAIL,
    QR_CHECKIN,
}

private data class TutorialContent(
    val category: String,
    val title: String,
    val summary: String,
    val points: List<String>,
    val caution: String? = null,
)

@Composable
fun TutorialHelpButton(
    tutorialId: TutorialId,
    tint: Color = OnBackground,
) {
    var visible by remember { mutableStateOf(false) }

    IconButton(onClick = { visible = true }) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = "도움말",
            tint = tint,
        )
    }

    if (visible) {
        TutorialHelpDialog(
            content = tutorialContentOf(tutorialId),
            onDismiss = { visible = false },
        )
    }
}

@Composable
private fun TutorialHelpDialog(
    content: TutorialContent,
    onDismiss: () -> Unit,
) {
    val dialogBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE9F2EC),
            Color(0xFFE5EFF8),
            Color(0xFFE9E4FD),
        ),
    )
    val buttonBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFFDCEDE8),
            Color(0xFFDDE7F7),
            Color(0xFFE5DEFF),
        ),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .border(1.5.dp, dialogBorder, RoundedCornerShape(34.dp)),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp, vertical = 20.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "닫기",
                            tint = MutedForeground,
                        )
                    }
                }

                CategoryPill(content.category)
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFDFEFF), Color(0xFFF6F8FB)),
                            ),
                            shape = RoundedCornerShape(30.dp),
                        )
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = content.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                        Text(
                            text = content.summary,
                            fontSize = 15.sp,
                            lineHeight = 27.sp,
                            color = MutedForeground,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                content.points.forEachIndexed { index, point ->
                    TutorialPointCard(
                        index = index + 1,
                        text = point,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                content.caution?.let { caution ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFF3C8),
                                shape = RoundedCornerShape(24.dp),
                            )
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFE082), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF9B6B00),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Text(
                                text = caution,
                                fontSize = 14.sp,
                                lineHeight = 24.sp,
                                color = Color(0xFF5B4A16),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = White,
                    shadowElevation = 0.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, buttonBorder, RoundedCornerShape(24.dp))
                            .clickable(onClick = onDismiss)
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "확인했어요",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(category: String) {
    Row(
        modifier = Modifier
            .background(
                color = Color(0xFFF6F8FB),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = null,
            tint = MutedForeground,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = category,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MutedForeground,
        )
    }
}

@Composable
private fun TutorialPointCard(
    index: Int,
    text: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF6F8FB),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
            }
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 25.sp,
                color = OnBackground,
            )
        }
    }
}

private fun tutorialContentOf(id: TutorialId): TutorialContent {
    return when (id) {
        TutorialId.RESALE_LIST -> TutorialContent(
            category = "RESALE",
            title = "2차 거래소 안내",
            summary = "이 화면에서는 공연별로 등록된 재판매 티켓을 찾아보고 원하는 공연으로 이동할 수 있어요.",
            points = listOf(
                "검색과 지역 필터를 이용해 원하는 공연만 빠르게 좁혀볼 수 있어요.",
                "각 카드에서는 공연 일정, 장소, 현재 최저 재판매가를 바로 확인할 수 있어요.",
                "공연을 누르면 해당 공연에 등록된 재판매 티켓 목록으로 이동해요.",
            ),
            caution = "재판매가는 등록 상황에 따라 계속 달라질 수 있으니 결제 직전에 한 번 더 확인해 주세요.",
        )
        TutorialId.RESALE_DETAIL -> TutorialContent(
            category = "RESALE",
            title = "재판매 티켓 안내",
            summary = "이 화면에서는 현재 판매 중인 재판매 티켓을 비교하고, 원하는 좌석을 골라 구매할 수 있어요.",
            points = listOf(
                "좌석 정보와 가격을 함께 보고 나에게 맞는 티켓을 선택할 수 있어요.",
                "상세 화면에서는 정가 대비 할인 여부와 좌석 위치를 더 자세히 확인할 수 있어요.",
                "구매가 완료되면 티켓은 내 티켓으로 바로 이동해요.",
            ),
            caution = "재판매 티켓은 한 장씩 선점될 수 있어서 보고 있는 동안에도 상태가 바뀔 수 있어요.",
        )
        TutorialId.RESALE_CREATE -> TutorialContent(
            category = "RESALE",
            title = "재판매 등록 안내",
            summary = "보유 중인 티켓을 다른 사용자에게 판매하고 싶을 때 이 화면에서 재판매 가격을 정해 등록할 수 있어요.",
            points = listOf(
                "현재 티켓 정보와 원래 결제 금액을 확인한 뒤 판매 가격을 입력해 주세요.",
                "등록이 완료되면 티켓 상태가 판매 중으로 바뀌고 거래소에 노출돼요.",
                "판매 중인 티켓은 상세 화면에서 가격과 상태를 다시 확인할 수 있어요.",
            ),
            caution = "재판매 등록 후에는 실제 거래 진행 상황에 따라 티켓 사용 가능 여부가 달라질 수 있어요.",
        )
        TutorialId.WALLET -> TutorialContent(
            category = "WALLET",
            title = "SSF 지갑 안내",
            summary = "이 화면에서는 서비스 안에서 사용하는 SSF 잔액과 지갑 주소를 확인할 수 있어요.",
            points = listOf(
                "현재 보유 중인 SSF 잔액을 바로 확인할 수 있어요.",
                "테스트 충전 금액은 지갑 잔액에 즉시 반영돼요.",
                "더 자세한 사용 기록은 트랜젝션 내역 화면에서 볼 수 있어요.",
            ),
            caution = "지갑 주소는 자산 식별 정보이므로 복사 전후를 꼭 확인해 주세요.",
        )
        TutorialId.WALLET_HISTORY -> TutorialContent(
            category = "WALLET",
            title = "지갑 내역 안내",
            summary = "SSF의 충전과 사용 흐름을 날짜별로 확인할 수 있는 화면이에요.",
            points = listOf(
                "언제 어떤 이유로 SSF가 늘거나 줄었는지 한눈에 확인할 수 있어요.",
                "입금과 사용 내역이 날짜 기준으로 정리돼 흐름을 보기 쉬워요.",
                "거래 금액과 시간을 같이 보면서 최근 활동을 추적할 수 있어요.",
            ),
        )
        TutorialId.TX_HISTORY -> TutorialContent(
            category = "LEDGER",
            title = "트랜젝션 내역 안내",
            summary = "블록체인과 연결된 트랜젝션 상태를 확인하는 화면으로, 처리 결과와 확인 수를 함께 볼 수 있어요.",
            points = listOf(
                "구매, 재판매, 양도 같은 트랜젝션이 어떤 상태인지 확인할 수 있어요.",
                "트랜잭션 해시와 확인 수를 통해 처리 진행 상황을 볼 수 있어요.",
                "실패한 트랜젝션은 오류 메시지와 함께 원인을 다시 확인할 수 있어요.",
            ),
        )
        TutorialId.TRANSFER -> TutorialContent(
            category = "TRANSFER",
            title = "티켓 양도 안내",
            summary = "보유한 티켓을 다른 사용자에게 안전하게 전달할 때 사용하는 화면이에요.",
            points = listOf(
                "양도받는 사람의 전화번호를 정확하게 입력해야 정상적으로 전달돼요.",
                "양도가 완료되면 해당 티켓은 내 티켓 목록에서 빠져요.",
                "양도 실패 시에는 실패 사유를 확인하고 다시 시도할 수 있어요.",
            ),
            caution = "한 번 양도된 티켓은 되돌리기 어려우니 받는 사람 정보를 꼭 다시 확인해 주세요.",
        )
        TutorialId.COLLECTION -> TutorialContent(
            category = "COLLECTION",
            title = "컬렉션 안내",
            summary = "관람이 완료된 티켓은 컬렉션으로 보관되고, 나만의 아카이브처럼 모아볼 수 있어요.",
            points = listOf(
                "관람이 끝난 티켓만 컬렉션에 자동으로 쌓여요.",
                "티켓을 길게 누르거나 선택해 더 자세한 정보를 확인할 수 있어요.",
                "일반 티켓과는 다른 감상용 화면으로 추억을 모아볼 수 있어요.",
            ),
        )
        TutorialId.COLLECTIBLE_TICKET_DETAIL -> TutorialContent(
            category = "COLLECTION",
            title = "컬렉터블 티켓 안내",
            summary = "컬렉션에 저장된 티켓을 크게 보고, NFT처럼 감상할 수 있는 상세 화면이에요.",
            points = listOf(
                "카드를 탭하면 앞면과 뒷면이 전환되며 다른 정보를 볼 수 있어요.",
                "공연 정보와 좌석 정보, 티켓 번호를 한 화면에서 확인할 수 있어요.",
                "보관용 티켓이기 때문에 실제 입장용 티켓과는 역할이 달라요.",
            ),
        )
        TutorialId.QR_CHECKIN -> TutorialContent(
            category = "CHECK-IN",
            title = "QR 체크인 안내",
            summary = "공연장 입장 시 사용하는 QR을 확인하고 새로고침할 수 있는 화면이에요.",
            points = listOf(
                "QR은 일정 시간마다 새로 생성되므로 유효 시간을 함께 확인해 주세요.",
                "남은 시간이 0초가 되면 새 QR로 다시 갱신해 사용할 수 있어요.",
                "체크인이 완료되면 입장 처리 상태가 바로 바뀌어요.",
            ),
            caution = "입장 직전에는 화면 밝기와 QR 상태를 꼭 확인해 주세요.",
        )
    }
}
