package deception.service;

import deception.domain.LobbyPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    private final List<LobbyPlayer> players = new ArrayList<>();
    private boolean gameStarted = false;

    public synchronized LobbyPlayer joinLobby(String username) {
        if (gameStarted) {
            throw new IllegalStateException("Game is already in progress!");
        }
        if (players.size() >= 6) {
            throw new IllegalStateException("Lobby is full!");
        }
        if (username == null || username.trim().isEmpty() || username.length() > 10) {
            throw new IllegalArgumentException("Username must be between 1 and 10 characters.");
        }

        String playerId = UUID.randomUUID().toString();
        LobbyPlayer newPlayer = new LobbyPlayer(playerId, username, false);
        players.add(newPlayer);

        broadcastLobbyState();
        return newPlayer;
    }

    public synchronized void setReady(String playerId, boolean isReady) {
        if (gameStarted) return;

        LobbyPlayer player = players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not in lobby"));

        player.setReady(isReady);
        broadcastLobbyState();
        checkAndStartGame();
    }

    private void checkAndStartGame() {
        if (players.size() == 6 && players.stream().allMatch(LobbyPlayer::isReady)) {
            log.info("All 6 players are ready. Starting the game!");
            gameStarted = true;
            gameService.setupNewGame(new ArrayList<>(players)); // pass LobbyPlayers

            Map<String, Object> message = new HashMap<>();
            message.put("type", "GAME_STARTED");
            message.put("players", players);

            messagingTemplate.convertAndSend("/topic/lobby", (Object) message);
        }
    }

    private void broadcastLobbyState() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOBBY_UPDATE");
        message.put("players", players);
        message.put("canStart", players.size() == 6 && players.stream().allMatch(LobbyPlayer::isReady));
        
        messagingTemplate.convertAndSend("/topic/lobby", (Object) message);
    }

    public synchronized List<LobbyPlayer> getLobbyState() {
        return new ArrayList<>(players);
    }
}
