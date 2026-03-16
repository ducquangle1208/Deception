package deception.controller;

import deception.dto.*;
import deception.gameplay.GameSession;
import deception.mapper.GameStateMapper;
import deception.service.GameNotificationService;
import deception.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameStateMapper gameStateMapper;
    private final GameService gameService;
    private final GameNotificationService notificationService;

    public GameController(GameStateMapper gameStateMapper, GameService gameService, GameNotificationService notificationService) {
        this.gameStateMapper = gameStateMapper;
        this.gameService = gameService;
        this.notificationService = notificationService;
    }

    @PostMapping("/create-real-game")
    public ResponseEntity<GameSessionDTO> createRealGame() {
        List<String> realPlayerIds = Arrays.asList("user_1", "user_2", "user_3", "user_4", "user_5", "user_6");

        gameService.setupNewGame(realPlayerIds);

        GameSession currentSession = gameService.getCurrentGame();
        notificationService.broadcastGameState(currentSession);

        GameSessionDTO responseDTO = gameStateMapper.toDTO(currentSession, "user_1");
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/current-state/{requesterId}")
    public ResponseEntity<GameSessionDTO> getCurrentState(@PathVariable String requesterId) {
        try {
            GameSession currentSession = gameService.getCurrentGame();
            GameSessionDTO responseDTO = gameStateMapper.toDTO(currentSession, requesterId);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/action/select-crime")
    public ResponseEntity<?> selectCrime(@RequestBody CrimeSelectionRequest request) {
        try {
            gameService.selectCrime(request.getPlayerId(), request.getClueId(), request.getMeansId());
            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);
            return ResponseEntity.ok("Tội ác đã được ghi nhận!");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/action/place-hints")
    public ResponseEntity<?> placeInitialHints(@RequestBody FsPlaceHintRequest request) {
        try {
            gameService.placeInitialHints(request.getPlayerId(), request.getHints());
            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);
            return ResponseEntity.ok("Hints đã được đặt!");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/action/solve-attempt")
    public ResponseEntity<?> attemptToSolve(@RequestBody SolveAttemptRequest request) {
        try {
            gameService.attemptToSolve(request.getPlayerId(), request.getTargetPlayerId(), request.getClueId(), request.getMeansId());

            GameSession currentSession = gameService.getCurrentGame();

            notificationService.broadcastGameState(currentSession);

            return ResponseEntity.ok("Phá án đã được ghi nhận!");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/action/start-presentation/{playerId}")
    public ResponseEntity<?> startPresentation(@PathVariable String playerId) {
        try {
            gameService.startPresentation(playerId);
            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);
            return ResponseEntity.ok("Người chơi " + playerId + "trình bày!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/action/end-presentation/{playerId}")
    public ResponseEntity<?> endPresentationEarly(@PathVariable String playerId) {
        try {
            gameService.endPresentationEarly(playerId);

            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);

            return ResponseEntity.ok("Đã kết thúc lượt trình bày sớm!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PatchMapping("/action/replace-tile")
    public ResponseEntity<?> replaceTile(@RequestBody ReplaceTileRequest request) {
        try {
            gameService.replaceSceneTile(request.getPlayerId(), request.getOldCardId(), request.getNewCardOption());

            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);

            return ResponseEntity.ok("Đã thay thẻ thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/action/next-round/{playerId}")
    public ResponseEntity<?> triggerNextRound(@PathVariable String playerId) {
        try {
            gameService.startNextRound(playerId);
            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);
            return ResponseEntity.ok("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/action/witness-reversal")
    public ResponseEntity<?> attemptWitnessReversal(@RequestBody WitnessReversalRequest request) {
        try {
            gameService.attemptWitnessReversal(request.getPlayerId(), request.getSuspectId());
            GameSession currentSession = gameService.getCurrentGame();
            notificationService.broadcastGameState(currentSession);

            GameSessionDTO updatedSession = gameStateMapper.toDTO(gameService.getCurrentGame(), request.getPlayerId());
            return ResponseEntity.ok(updatedSession);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}