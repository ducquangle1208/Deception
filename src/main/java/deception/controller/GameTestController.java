package deception.controller;

import deception.constant.GamePhase;
import deception.constant.RoleType;
import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import deception.mapper.GameStateMapper;
import deception.model.PlayerInGame;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/test")
public class GameTestController {

    private final GameStateMapper gameStateMapper;

    // Inject GameStateMapper vào thông qua Constructor
    public GameTestController(GameStateMapper gameStateMapper) {
        this.gameStateMapper = gameStateMapper;
    }

    @GetMapping("/room-state/{requesterId}")
    public ResponseEntity<GameSessionDTO> getMockRoomState(@PathVariable String requesterId) {
        // 1. Tạo một GameSession giả lập (Mock)
        GameSession mockSession = createMockSession();

        try {
            // 2. Lọc dữ liệu qua DTO Mapper dựa trên góc nhìn của requesterId
            GameSessionDTO responseDTO = gameStateMapper.toDTO(mockSession, requesterId);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            // Nếu truyền ID bậy bạ không có trong phòng
            return ResponseEntity.badRequest().build();
        }
    }

    // Hàm tạo dữ liệu giả (Mock)
    private GameSession createMockSession() {
        GameSession session = new GameSession();
        session.setRoomId("ROOM_6969");
        session.setCurrentPhase(GamePhase.DISCUSSION_PRESENTATION);
        session.setCurrentRound(1);
        session.setBoardHints(new ArrayList<>()); // Bỏ qua mảng Hint cho gọn

        // Set đáp án của vụ án
        ClueCard solutionClue = new ClueCard();
        solutionClue.setId("CLUE_99");
        solutionClue.setName("Vết máu");
        session.setSolutionClue(solutionClue);

        MeansCard solutionMeans = new MeansCard();
        solutionMeans.setId("MEANS_99");
        solutionMeans.setName("Búa");
        session.setSolutionMeans(solutionMeans);

        // Tạo 6 người chơi với 6 Role cố định
        Map<String, PlayerInGame> players = new HashMap<>();
        players.put("p1", createMockPlayer("p1", "Alice (Bác Sĩ)", RoleType.FORENSIC_SCIENTIST));
        players.put("p2", createMockPlayer("p2", "Bob (Sát Nhân)", RoleType.MURDERER));
        players.put("p3", createMockPlayer("p3", "Charlie (Tòng Phạm)", RoleType.ACCOMPLICE));
        players.put("p4", createMockPlayer("p4", "David (Nhân Chứng)", RoleType.WITNESS));
        players.put("p5", createMockPlayer("p5", "Eve (Thám Tử 1)", RoleType.INVESTIGATOR));
        players.put("p6", createMockPlayer("p6", "Frank (Thám Tử 2)", RoleType.INVESTIGATOR));

        session.setPlayers(players);
        return session;
    }

    private PlayerInGame createMockPlayer(String id, String name, RoleType role) {
        PlayerInGame p = new PlayerInGame();
        p.setPlayerId(id);
        p.setPlayerName(name);
        p.setRole(role);
        p.setClueCards(new ArrayList<>()); // Bỏ qua bài trên tay cho gọn
        p.setMeansCards(new ArrayList<>());
        p.setHasBadge(true);
        p.setHasPresented(false);
        return p;
    }
}