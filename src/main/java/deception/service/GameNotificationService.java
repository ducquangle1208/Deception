package deception.service;

import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import deception.mapper.GameStateMapper;
import deception.model.PlayerInGame;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameNotificationService {

    // Công cụ có sẵn của Spring để gửi message qua WebSocket
    private final SimpMessagingTemplate messagingTemplate;
    private final GameStateMapper gameStateMapper;

    public GameNotificationService(SimpMessagingTemplate messagingTemplate, GameStateMapper gameStateMapper) {
        this.messagingTemplate = messagingTemplate;
        this.gameStateMapper = gameStateMapper;
    }

    // Hàm này sẽ được gọi mỗi khi có BẤT KỲ sự thay đổi nào trong game
    public void broadcastGameState(GameSession session) {
        if (session == null || session.getPlayers() == null) return;

        // Vòng lặp thần thánh: Gửi DTO đã được "che mờ" riêng cho từng người
        for (PlayerInGame player : session.getPlayers().values()) {
            String playerId = player.getPlayerId();

            // 1. Dùng Mapper bạn đã viết để lọc dữ liệu theo góc nhìn của người này
            GameSessionDTO personalDto = gameStateMapper.toDTO(session, playerId);

            // 2. Định nghĩa kênh riêng của người này.
            // Ví dụ: /topic/game/player/user_1
            String destinationChannel = "/topic/game/player/" + playerId;

            // 3. Bắn dữ liệu xuống Frontend!
            messagingTemplate.convertAndSend(destinationChannel, personalDto);
        }
    }
}