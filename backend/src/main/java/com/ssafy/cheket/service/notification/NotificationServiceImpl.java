package com.ssafy.cheket.service.notification;

import com.ssafy.cheket.dto.notification.request.CreateNotificationRequest;
import com.ssafy.cheket.entity.notification.Notification;
import com.ssafy.cheket.entity.notification.NotificationMessage;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.NotificationType;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.notification.NotificationMessageRepository;
import com.ssafy.cheket.repository.notification.NotificationRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.util.notification.NotificationTemplateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMessageRepository notificationMessageRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;

    private final PushNotificationService pushNotificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Override
    public void createNotification(CreateNotificationRequest request) {
        createNotification(request, Map.of());
    }

    @Override
    public void createNotification(CreateNotificationRequest request, Map<String, String> data) {
        User user = userRepository.findByIdAndDeletedAtIsNull(request.getUserId())
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        NotificationMessage template = notificationMessageRepository.findByType(request.getType())
            .orElseThrow(() -> new NotFoundException("알림 메시지 템플릿을 찾을 수 없습니다."));

        String finalMessage = NotificationTemplateUtil.replaceVariables(template.getMessage(),
            getVariablesOrEmpty(request.getVariables()));

        Long showId = null;
        if (data != null && data.containsKey("showId")) {
            showId = Long.valueOf(data.get("showId"));
        }

        Notification notification = Notification.builder().title(request.getTitle()).message(finalMessage)
            .userId(request.getUserId()).notificationMessageId(template.getId()).type(request.getType()).showId(showId)
            .build();

        Notification savedNotification = notificationRepository.save(notification);

        if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
            Map<String, String> payload = new HashMap<>();
            payload.put("type", request.getType().name());
            payload.put("notificationId", String.valueOf(savedNotification.getId()));

            if (data != null) {
                payload.putAll(data);
            }

            pushNotificationService.sendPush(user.getFcmToken(), request.getTitle(), finalMessage, payload);
        }
    }

    @Override
    public void sendShowStart(Long userId, LocalDateTime sessionDateTime, String showTitle) {
        createNotification(CreateNotificationRequest.builder().userId(userId).type(NotificationType.SHOW_START)
            .title("공연 시작 알림")
            .variables(Map.of("sessionDateTime", sessionDateTime.format(FORMATTER), "showTitle", showTitle)).build());
    }

    @Override
    public void sendResale(Long userId, String showTitle) {
        createNotification(CreateNotificationRequest.builder().userId(userId).type(NotificationType.RESALE)
            .title("2차 티켓 판매 완료").variables(Map.of("showTitle", showTitle)).build());
    }

    @Override
    public void sendSettlement(Long userId, LocalDateTime sessionDateTime, String showTitle,
        BigDecimal settlementAmount) {
        createNotification(CreateNotificationRequest.builder().userId(userId).type(NotificationType.SETTLEMENT)
            .title("정산 완료 알림").variables(Map.of("sessionDateTime", sessionDateTime.format(FORMATTER), "showTitle",
                showTitle, "settlementAmount", settlementAmount.stripTrailingZeros().toPlainString()))
            .build());
    }

    @Override
    public void sendRequestCreate(Long userId, Long showId) {
        createNotification(CreateNotificationRequest.builder().userId(userId).type(NotificationType.RQ_CREATE)
            .title("공연 등록 요청").variables(Map.of()).build(), Map.of("showId", String.valueOf(showId)));
    }

    @Override
    public void sendRequestUpdate(Long userId, Long showId) {
        createNotification(CreateNotificationRequest.builder().userId(userId).type(NotificationType.RQ_UPDATE)
            .title("공연 수정 요청").variables(Map.of()).build(), Map.of("showId", String.valueOf(showId)));
    }

    @Override
    public void sendTomorrowShowStartNotifications() {
        LocalDate targetDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        sendShowStartNotificationsByDate(targetDate);
    }

    @Override
    public void sendTodayShowStartNotifications() {
        LocalDate targetDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        sendShowStartNotificationsByDate(targetDate);
    }

    private void sendShowStartNotificationsByDate(LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        List<Session> sessions = sessionRepository.findBySessionDateGreaterThanEqualAndSessionDateLessThan(start, end);

        for (Session session : sessions) {
            Show show = showRepository.findById(session.getShowId())
                .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다."));

            List<Long> userIds = ticketRepository.findDistinctUserIdsBySessionId(session.getId());

            for (Long userId : userIds) {
                sendShowStart(userId, session.getSessionDate(), show.getTitle());
            }
        }
    }

    private Map<String, String> getVariablesOrEmpty(Map<String, String> variables) {
        return variables == null ? Map.of() : variables;
    }
}
