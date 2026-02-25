package deception.controller;

import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import deception.mapper.GameStateMapper;
import deception.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/test")
public class GameTestController {

    private final GameStateMapper gameStateMapper;
    private final GameService gameService;

    // Inject GameStateMapper vào thông qua Constructor
    public GameTestController(GameStateMapper gameStateMapper, GameService gameService) {
        this.gameStateMapper = gameStateMapper;
        this.gameService = gameService;
    }

    @PostMapping("/create-real-game")
    public ResponseEntity<GameSessionDTO> createRealGame() {
        List<String> realPlayerIds = Arrays.asList("user_1", "user_2", "user_3", "user_4", "user_5", "user_6");

        // Gọi Service tạo game (không cần roomId)
        gameService.setupNewGame(realPlayerIds);

        // Lấy state trả về dưới góc nhìn của user_1
        GameSessionDTO responseDTO = gameStateMapper.toDTO(gameService.getCurrentGame(), "user_1");

        return ResponseEntity.ok(responseDTO);
    }

    // API lấy state hiện tại của game (Để các client gọi)
    @GetMapping("/current-state/{requesterId}")
    public ResponseEntity<GameSessionDTO> getCurrentState(@PathVariable String requesterId) {
        try {
            GameSession currentSession = gameService.getCurrentGame();
            GameSessionDTO responseDTO = gameStateMapper.toDTO(currentSession, requesterId);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Trả về lỗi nếu game chưa start
        }
    }
}