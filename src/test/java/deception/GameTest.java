package deception;

import com.fasterxml.jackson.databind.ObjectMapper;
import deception.constant.GamePhase;
import deception.constant.RoleType;
import deception.gameplay.SceneTileHint;
import deception.service.CardRegistryService;
import deception.service.GameService;
import deception.domain.LobbyPlayer;
import deception.mapper.GameStateMapper;
import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import deception.model.PlayerInGame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTest {
    @Test
    public void testOutput() throws Exception {
        System.out.println("Starting TestGame");
        ObjectMapper mapper = new ObjectMapper();
        CardRegistryService cardRegistry = new CardRegistryService(mapper);
        cardRegistry.init();
        
        GameService gameService = new GameService(cardRegistry);
        List<LobbyPlayer> players = buildPlayers();
        
        gameService.setupNewGame(players);
        System.out.println("Game started successfully!");

        GameStateMapper mapperObj = new GameStateMapper();
        GameSession session = gameService.getCurrentGame();
        GameSessionDTO dto = mapperObj.toDTO(session, "p1");
        
        // Let's also serialize it to JSON to see if Jackson throws
        String json = mapper.writeValueAsString(dto);
        System.out.println("JSON length: " + json.length());
    }

    @Test
    public void startsWithSharedPresentationThenAllowsIndividualPresentations() {
        ObjectMapper mapper = new ObjectMapper();
        CardRegistryService cardRegistry = new CardRegistryService(mapper);
        cardRegistry.init();

        GameService gameService = new GameService(cardRegistry);
        gameService.setupNewGame(buildPlayers());

        GameSession session = gameService.getCurrentGame();
        PlayerInGame murderer = session.getPlayers().values().stream()
                .filter(player -> player.getRole() == RoleType.MURDERER)
                .findFirst()
                .orElseThrow();
        PlayerInGame forensicScientist = session.getPlayers().values().stream()
                .filter(player -> player.getRole() == RoleType.FORENSIC_SCIENTIST)
                .findFirst()
                .orElseThrow();
        PlayerInGame investigator = session.getPlayers().values().stream()
                .filter(player -> player.getRole() == RoleType.INVESTIGATOR)
                .findFirst()
                .orElseThrow();

        gameService.selectCrime(
                murderer.getPlayerId(),
                murderer.getClueCards().get(0).getId(),
                murderer.getMeansCards().get(0).getId());

        Map<String, String> hints = new HashMap<>();
        for (SceneTileHint hint : session.getBoardHints()) {
            hints.put(hint.getSceneCard().getId(), hint.getSceneCard().getOptions().get(0));
        }

        gameService.placeInitialHints(forensicScientist.getPlayerId(), hints);

        assertEquals(GamePhase.DISCUSSION_PRESENTATION, session.getCurrentPhase());
        assertTrue(session.isSimultaneousPresentationPhase());
        assertNull(session.getPresentingPlayerId());
        assertTrue(session.isTimerActive());

        IllegalStateException duringSharedWindow = assertThrows(
                IllegalStateException.class,
                () -> gameService.startPresentation(investigator.getPlayerId()));
        assertTrue(duringSharedWindow.getMessage().contains("45 giây trình bày tự do"));

        session.setPresentationEndTime(System.currentTimeMillis() - 1000);

        gameService.startPresentation(investigator.getPlayerId());

        assertFalse(session.isSimultaneousPresentationPhase());
        assertEquals(investigator.getPlayerId(), session.getPresentingPlayerId());
        assertTrue(session.isTimerActive());
        assertTrue(session.getPlayers().get(investigator.getPlayerId()).isHasPresented());
    }

    private List<LobbyPlayer> buildPlayers() {
        List<LobbyPlayer> players = new ArrayList<>();
        players.add(new LobbyPlayer("p1", "A", true));
        players.add(new LobbyPlayer("p2", "B", true));
        players.add(new LobbyPlayer("p3", "C", true));
        players.add(new LobbyPlayer("p4", "D", true));
        players.add(new LobbyPlayer("p5", "E", true));
        players.add(new LobbyPlayer("p6", "F", true));
        return players;
    }
}
