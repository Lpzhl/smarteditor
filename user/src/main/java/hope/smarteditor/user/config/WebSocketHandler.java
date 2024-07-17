package hope.smarteditor.user.config;


import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebSocketHandler extends BinaryWebSocketHandler {

    @Autowired
    private UserService userService;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String room = getRoom(session);
        String username = getUsernameFromSession(session);
        if (username != null) {
            User user = userService.findByUsername(username);
            session.getAttributes().put("user", user);
        }
        roomSessions.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    private String getUsernameFromSession(WebSocketSession session) {
        Map<String, String> params = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().toSingleValueMap();
        return params.get("username");
    }


    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String room = getRoom(session);
        byte[] payload = message.getPayload().array();
        for (WebSocketSession s : roomSessions.getOrDefault(room, new CopyOnWriteArrayList<>())) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                synchronized (s) {
                    try {
                        s.sendMessage(new BinaryMessage(payload));
                    } catch (IllegalStateException e) {
                        System.err.println("Failed to send message: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String room = getRoom(session);
        roomSessions.getOrDefault(room, new CopyOnWriteArrayList<>()).remove(session);
    }

    private String getRoom(WebSocketSession session) {
        return session.getUri().getPath().split("/")[2];
    }
}
