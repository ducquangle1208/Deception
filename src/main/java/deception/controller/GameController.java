package deception.controller;

import deception.dto.CrimeSelectionRequest;
import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import deception.mapper.GameStateMapper;
import deception.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameStateMapper gameStateMapper;
    private final GameService gameService;

    // Inject GameStateMapper vào thông qua Constructor
    public GameController(GameStateMapper gameStateMapper, GameService gameService) {
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

    @PostMapping("/action/select-crime")
    public ResponseEntity<?> selectCrime(@RequestBody CrimeSelectionRequest request) {
        try {
            // Gọi logic xử lý trong Service
            gameService.selectCrime(request.getPlayerId(), request.getClueId(), request.getMeansId());

            // Nếu thành công, trả về trạng thái Game hiện tại dưới góc nhìn của chính Kẻ Sát Nhân đó
            GameSessionDTO updatedSession = gameStateMapper.toDTO(gameService.getCurrentGame(), request.getPlayerId());
            return ResponseEntity.ok(updatedSession);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Trả về lỗi 400 Bad Request kèm thông báo (Ví dụ: "Gian lận: Chỉ Kẻ Sát Nhân mới được...")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}