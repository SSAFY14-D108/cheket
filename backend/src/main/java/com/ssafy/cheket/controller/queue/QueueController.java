package com.ssafy.cheket.controller.queue;

import com.ssafy.cheket.service.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows/{showId}/sessions/{sessionId}/queue")
public class QueueController {
    private final QueueService queueService;

}
