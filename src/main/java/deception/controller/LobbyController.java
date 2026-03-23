package deception.controller;

import deception.domain.LobbyPlayer;
import deception.service.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lobby")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LobbyController {

    private final LobbyService lobbyService;

    @PostMapping("/join")
    public ResponseEntity<LobbyPlayer> joinLobby(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        LobbyPlayer player = lobbyService.joinLobby(username);
        return ResponseEntity.ok(player);
    }

    @PostMapping("/ready")
    public ResponseEntity<Void> setReady(@RequestBody Map<String, Object> body) {
        String playerId = (String) body.get("playerId");
        boolean isReady = (Boolean) body.get("isReady");
        lobbyService.setReady(playerId, isReady);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/state")
    public ResponseEntity<List<LobbyPlayer>> getLobbyState() {
        return ResponseEntity.ok(lobbyService.getLobbyState());
    }
}
