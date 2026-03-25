package deception;

import com.fasterxml.jackson.databind.ObjectMapper;
import deception.service.CardRegistryService;
import deception.service.GameService;
import deception.domain.LobbyPlayer;
import deception.mapper.GameStateMapper;
import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class GameTest {
    @Test
    public void testOutput() throws Exception {
        System.out.println("Starting TestGame");
        ObjectMapper mapper = new ObjectMapper();
        CardRegistryService cardRegistry = new CardRegistryService(mapper);
        cardRegistry.init();
        
        GameService gameService = new GameService(cardRegistry);
        List<LobbyPlayer> players = new ArrayList<>();
        players.add(new LobbyPlayer("p1", "A", true));
        players.add(new LobbyPlayer("p2", "B", true));
        players.add(new LobbyPlayer("p3", "C", true));
        players.add(new LobbyPlayer("p4", "D", true));
        players.add(new LobbyPlayer("p5", "E", true));
        players.add(new LobbyPlayer("p6", "F", true));
        
        gameService.setupNewGame(players);
        System.out.println("Game started successfully!");

        GameStateMapper mapperObj = new GameStateMapper();
        GameSession session = gameService.getCurrentGame();
        GameSessionDTO dto = mapperObj.toDTO(session, "p1");
        
        // Let's also serialize it to json to see if Jackson throws
        String json = mapper.writeValueAsString(dto);
        System.out.println("JSON length: " + json.length());
    }
}
