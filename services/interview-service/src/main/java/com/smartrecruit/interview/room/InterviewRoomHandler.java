package com.smartrecruit.interview.room;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Signalling for in-platform video interviews. Each interview has a room with at most one recruiter and one
 * candidate. The service never sees audio or video: it only relays the WebRTC offer/answer, network candidates,
 * chat lines and camera/microphone state between the two browsers, which then talk directly to each other.
 * <p>
 * Messages from a browser must be JSON objects; they are forwarded to the other participant as
 * {@code {"from":"RECRUITER","data":{...}}}. Room events sent by the service have a {@code "type"} field:
 * {@code welcome}, {@code peer-joined}, {@code peer-left}, {@code replaced}, {@code pong}.
 */
@Component
public class InterviewRoomHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(InterviewRoomHandler.class);
    private static final String SAFE = "safeSession";
    private static final int MAX_MESSAGE = 64 * 1024;

    private final Map<UUID, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // send() may be called from several threads at once (both peers talking): serialise writes per session
        WebSocketSession safe = new ConcurrentWebSocketSessionDecorator(session, 10_000, 512 * 1024);
        session.getAttributes().put(SAFE, safe);
        UUID interviewId = (UUID) session.getAttributes().get(InterviewRoomHandshakeInterceptor.INTERVIEW);
        String role = (String) session.getAttributes().get(InterviewRoomHandshakeInterceptor.ROLE);
        String name = (String) session.getAttributes().get(InterviewRoomHandshakeInterceptor.NAME);

        Map<String, WebSocketSession> room = rooms.computeIfAbsent(interviewId, id -> new ConcurrentHashMap<>());
        WebSocketSession previous = room.put(role, safe);
        if (previous != null && previous.isOpen()) {
            send(previous, "{\"type\":\"replaced\"}");
            previous.close(new CloseStatus(4001, "Joined from another window"));
        }
        WebSocketSession peer = room.get(other(role));
        boolean peerHere = peer != null && peer.isOpen();
        String peerName = peerHere ? (String) peer.getAttributes().get(InterviewRoomHandshakeInterceptor.NAME) : "";
        send(safe, "{\"type\":\"welcome\",\"role\":\"" + role + "\",\"peerPresent\":" + peerHere
                + ",\"peerName\":" + json(peerName) + "}");
        if (peerHere) send(peer, "{\"type\":\"peer-joined\",\"role\":\"" + role + "\",\"name\":" + json(name) + "}");
        log.info("Interview room {}: {} joined", interviewId, role);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload().trim();
        if (payload.length() > MAX_MESSAGE || !payload.startsWith("{") || !payload.endsWith("}")) return;
        WebSocketSession self = safe(session);
        if (payload.contains("\"type\":\"ping\"")) {
            send(self, "{\"type\":\"pong\"}");
            return;
        }
        UUID interviewId = (UUID) session.getAttributes().get(InterviewRoomHandshakeInterceptor.INTERVIEW);
        String role = (String) session.getAttributes().get(InterviewRoomHandshakeInterceptor.ROLE);
        Map<String, WebSocketSession> room = rooms.get(interviewId);
        WebSocketSession peer = room == null ? null : room.get(other(role));
        if (peer != null && peer.isOpen()) send(peer, "{\"from\":\"" + role + "\",\"data\":" + payload + "}");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID interviewId = (UUID) session.getAttributes().get(InterviewRoomHandshakeInterceptor.INTERVIEW);
        String role = (String) session.getAttributes().get(InterviewRoomHandshakeInterceptor.ROLE);
        Map<String, WebSocketSession> room = rooms.get(interviewId);
        if (room == null) return;
        if (room.remove(role, safe(session))) {
            WebSocketSession peer = room.get(other(role));
            if (peer != null && peer.isOpen()) send(peer, "{\"type\":\"peer-left\",\"role\":\"" + role + "\"}");
            log.info("Interview room {}: {} left", interviewId, role);
        }
        if (room.isEmpty()) rooms.remove(interviewId, room);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        log.debug("Interview room transport error: {}", exception.toString());
        if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
    }

    private static WebSocketSession safe(WebSocketSession session) {
        Object stored = session.getAttributes().get(SAFE);
        return stored instanceof WebSocketSession decorated ? decorated : session;
    }

    private static String other(String role) {
        return "RECRUITER".equals(role) ? "CANDIDATE" : "RECRUITER";
    }

    private static void send(WebSocketSession session, String json) {
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException | IllegalStateException failed) {
            log.debug("Could not reach a room participant: {}", failed.toString());
        }
    }

    /** Minimal JSON string encoding for names. */
    static String json(String value) {
        if (value == null) return "\"\"";
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
