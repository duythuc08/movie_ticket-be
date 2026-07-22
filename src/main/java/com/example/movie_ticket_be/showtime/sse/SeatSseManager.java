package com.example.movie_ticket_be.showtime.sse;

import com.example.movie_ticket_be.showtime.dto.response.SeatShowTimeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSseManager {

    // showTimeId -> danh sách emitter đang kết nối
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitter subscribe(Long showTimeId) {
        // Timeout 30 phút — đủ cho 1 suất chọn ghế
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitters.computeIfAbsent(showTimeId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(showTimeId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Gửi heartbeat ngay khi kết nối để xác nhận SSE hoạt động
        try {
            emitter.send(SseEmitter.event().name("connected").data((Object) "ok"));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    public void broadcast(Long showTimeId, List<SeatShowTimeResponse> seats) {
        List<SseEmitter> list = emitters.get(showTimeId);
        if (list == null || list.isEmpty()) return;

        String payload;
        try {
            payload = objectMapper.writeValueAsString(seats);
        } catch (Exception e) {
            log.error("SSE serialize error for showTime {}", showTimeId, e);
            return;
        }

        List<SseEmitter> dead = new java.util.ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("seat-update").data((Object) payload));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        list.removeAll(dead);
    }

    private void removeEmitter(Long showTimeId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(showTimeId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(showTimeId);
        }
    }
}
