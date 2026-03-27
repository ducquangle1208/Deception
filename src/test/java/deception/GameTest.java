package deception;

import com.fasterxml.jackson.databind.ObjectMapper;
import deception.constant.GamePhase;
import deception.constant.RoleType;
import deception.domain.LobbyPlayer;
import deception.dto.GameSessionDTO;
import deception.gameplay.GameSession;
import deception.gameplay.SceneTileHint;
import deception.mapper.GameStateMapper;
import deception.model.PlayerInGame;
import deception.service.CardRegistryService;
import deception.service.GameService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

        String json = mapper.writeValueAsString(dto);
        System.out.println("JSON length: " + json.length());
    }

    @Test
    public void automaticallyAdvancesDiscussionAndStartsRoundTwo() {
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

        gameService.selectCrime(
                murderer.getPlayerId(),
                murderer.getClueCards().get(0).getId(),
                murderer.getMeansCards().get(0).getId());

        assertEquals(0, session.getCrimeSelectionEndTime());
        assertTrue(session.getHintPlacementEndTime() > System.currentTimeMillis());

        Map<String, String> hints = new HashMap<>();
        for (SceneTileHint hint : session.getBoardHints()) {
            hints.put(hint.getSceneCard().getId(), hint.getSceneCard().getOptions().get(0));
        }

        gameService.placeInitialHints(forensicScientist.getPlayerId(), hints);

        assertEquals(GamePhase.DISCUSSION_PRESENTATION, session.getCurrentPhase());
        assertTrue(session.isSimultaneousPresentationPhase());
        assertNull(session.getPresentingPlayerId());
        assertTrue(session.isTimerActive());

        session.setPresentationEndTime(System.currentTimeMillis() - 1000);
        gameService.advanceAutomaticFlow();

        List<String> expectedOrder = session.getPlayers().values().stream()
                .filter(player -> player.getRole() != RoleType.FORENSIC_SCIENTIST)
                .map(PlayerInGame::getPlayerId)
                .collect(Collectors.toList());

        assertFalse(session.isSimultaneousPresentationPhase());
        assertEquals(expectedOrder.get(0), session.getPresentingPlayerId());
        assertTrue(session.isTimerActive());
        assertTrue(session.getPlayers().get(expectedOrder.get(0)).isHasPresented());

        for (int i = 0; i < expectedOrder.size() - 1; i++) {
            session.setPresentationEndTime(System.currentTimeMillis() - 1000);
            gameService.advanceAutomaticFlow();
            assertEquals(expectedOrder.get(i + 1), session.getPresentingPlayerId());
        }

        session.setPresentationEndTime(System.currentTimeMillis() - 1000);
        gameService.advanceAutomaticFlow();

        assertEquals(2, session.getCurrentRound());
        assertEquals(GamePhase.FS_REPLACING_HINT, session.getCurrentPhase());
        assertNull(session.getPresentingPlayerId());
        assertFalse(session.isSimultaneousPresentationPhase());
        assertEquals(0, session.getPresentationEndTime());
    }

    @Test
    public void automaticallyAssignsCrimeSelectionWhenTimerExpires() {
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

        assertEquals(GamePhase.CRIME_SELECTION, session.getCurrentPhase());
        assertTrue(session.getCrimeSelectionEndTime() > System.currentTimeMillis());

        session.setCrimeSelectionEndTime(System.currentTimeMillis() - 1000);
        gameService.advanceAutomaticFlow();

        assertEquals(GamePhase.FS_PLACING_HINTS, session.getCurrentPhase());
        assertEquals(0, session.getCrimeSelectionEndTime());
        assertNotNull(session.getSolutionClue());
        assertNotNull(session.getSolutionMeans());
        assertEquals(murderer.getClueCards().get(0).getId(), session.getSolutionClue().getId());
        assertEquals(murderer.getMeansCards().get(0).getId(), session.getSolutionMeans().getId());
        assertTrue(session.getHintPlacementEndTime() > System.currentTimeMillis());
    }

    @Test
    public void automaticallyAssignsHintsWhenTimerExpires() {
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

        gameService.selectCrime(
                murderer.getPlayerId(),
                murderer.getClueCards().get(0).getId(),
                murderer.getMeansCards().get(0).getId());

        assertEquals(GamePhase.FS_PLACING_HINTS, session.getCurrentPhase());
        assertTrue(session.getHintPlacementEndTime() > System.currentTimeMillis());

        session.setHintPlacementEndTime(System.currentTimeMillis() - 1000);
        gameService.advanceAutomaticFlow();

        assertEquals(GamePhase.DISCUSSION_PRESENTATION, session.getCurrentPhase());
        assertEquals(0, session.getHintPlacementEndTime());
        assertTrue(session.isSimultaneousPresentationPhase());
        for (SceneTileHint hint : session.getBoardHints()) {
            assertEquals(hint.getSceneCard().getOptions().get(0), hint.getSelectedOption());
        }
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
