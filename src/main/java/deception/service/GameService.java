package deception.service;

import deception.constant.GamePhase;
import deception.constant.RoleType;
import deception.constant.SceneType;
import deception.domain.LobbyPlayer;
import deception.gameplay.GameSession;
import deception.gameplay.SceneTileHint;
import deception.model.PlayerInGame;
import deception.model.abstract_model.Card;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import deception.model.cards.SceneCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GameService {

    private static final long CRIME_SELECTION_DURATION_MS = 20_000L;
    private static final long HINT_PLACEMENT_DURATION_MS = 30_000L;
    private static final long PRESENTATION_DURATION_MS = 45_000L;

    private final CardRegistryService cardRegistry;

    private GameSession currentGameSession = null;

    public GameService(CardRegistryService cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    public void setupNewGame(List<LobbyPlayer> lobbyPlayers) {
        if (lobbyPlayers.size() != 6) {
            throw new IllegalArgumentException("Tro choi yeu cau chinh xac 6 nguoi choi!");
        }

        GameSession session = new GameSession();
        session.setCurrentPhase(GamePhase.CRIME_SELECTION);
        session.setCrimeSelectionEndTime(System.currentTimeMillis() + CRIME_SELECTION_DURATION_MS);
        session.setCurrentRound(1);

        List<RoleType> roles = Arrays.asList(
                RoleType.FORENSIC_SCIENTIST, RoleType.MURDERER, RoleType.ACCOMPLICE,
                RoleType.WITNESS, RoleType.INVESTIGATOR, RoleType.INVESTIGATOR);
        Collections.shuffle(roles);

        List<ClueCard> deckClues = cardRegistry.getAllClueCards();
        List<MeansCard> deckMeans = cardRegistry.getAllMeansCards();
        Collections.shuffle(deckClues);
        Collections.shuffle(deckMeans);

        Map<String, PlayerInGame> playersMap = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            LobbyPlayer lobbyPlayer = lobbyPlayers.get(i);
            PlayerInGame player = new PlayerInGame();
            player.setPlayerId(lobbyPlayer.getPlayerId());
            player.setPlayerName(lobbyPlayer.getUsername());
            player.setRole(roles.get(i));
            player.setHasPresented(false);

            if (player.getRole() == RoleType.FORENSIC_SCIENTIST) {
                player.setClueCards(new ArrayList<>());
                player.setMeansCards(new ArrayList<>());
                player.setHasBadge(false);
            } else {
                player.setClueCards(drawCards(deckClues));
                player.setMeansCards(drawCards(deckMeans));
                player.setHasBadge(true);
            }
            if (player.getRole() == RoleType.MURDERER) {
                session.setMurdererId(player.getPlayerId());
            }
            playersMap.put(player.getPlayerId(), player);
        }
        session.setPlayers(playersMap);

        List<SceneTileHint> boardHints = new ArrayList<>();
        boardHints.add(new SceneTileHint(cardRegistry.getRandomSceneCardByType(SceneType.CAUSE_OF_DEATH)));
        boardHints.add(new SceneTileHint(cardRegistry.getRandomSceneCardByType(SceneType.LOCATION_OF_CRIME)));
        List<SceneCard> randomScenes = cardRegistry.getRandomSceneCardsByType(SceneType.RANDOM_SCENE, 4);
        for (SceneCard card : randomScenes) {
            boardHints.add(new SceneTileHint(card));
        }
        session.setBoardHints(boardHints);

        this.currentGameSession = session;
    }

    public GameSession getCurrentGame() {
        if (this.currentGameSession == null) {
            throw new IllegalStateException("Chua co van game nao duoc tao!");
        }
        advanceAutomaticFlow();
        return this.currentGameSession;
    }

    public synchronized GameSession advanceAutomaticFlow() {
        if (this.currentGameSession == null) {
            return null;
        }

        boolean changed = applyAutomaticFlowTransitions(this.currentGameSession);
        return changed ? this.currentGameSession : null;
    }

    private <T extends Card> List<T> drawCards(List<T> deck) {
        List<T> drawn = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            drawn.add(deck.remove(0));
        }
        return drawn;
    }

    public synchronized void selectCrime(String playerId, String clueId, String meansId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.CRIME_SELECTION) {
            throw new IllegalStateException(
                    "Hanh dong bi tu choi: Hien khong phai la giai doan chon Hung khi va Vat chung!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Nguoi choi khong ton tai trong phong!");
        }
        if (player.getRole() != RoleType.MURDERER) {
            throw new IllegalArgumentException("Gian lan: Chi Ke Sat Nhan moi duoc phep chon dap an!");
        }

        ClueCard selectedClue = player.getClueCards().stream()
                .filter(c -> c.getId().equals(clueId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Manh moi (Clue) khong hop le hoac khong thuoc ve ban!"));

        MeansCard selectedMeans = player.getMeansCards().stream()
                .filter(m -> m.getId().equals(meansId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Hung khi (Means) khong hop le hoac khong thuoc ve ban!"));

        session.setSolutionClue(selectedClue);
        session.setSolutionMeans(selectedMeans);
        session.setCrimeSelectionEndTime(0);

        startHintPlacementPhase(session);
    }

    public synchronized void placeInitialHints(String playerId, Map<String, String> hints) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.FS_PLACING_HINTS) {
            throw new IllegalStateException(
                    "Hanh dong bi tu choi: Hien khong phai la giai doan Bac si phap y dat Hint!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Gian lan: Chi Bac si phap y moi duoc quyen dat Hint!");
        }

        if (hints == null || hints.size() != 6) {
            throw new IllegalArgumentException(
                    "Bac si phap y bat buoc phai dat chinh xac 6 vien dan len 6 the hien truong!");
        }

        for (SceneTileHint boardHint : session.getBoardHints()) {
            String cardId = boardHint.getSceneCard().getId();

            if (!hints.containsKey(cardId)) {
                throw new IllegalArgumentException(
                        "Thieu Hint cho the hien truong: " + boardHint.getSceneCard().getName());
            }

            String selectedOption = hints.get(cardId);

            if (!boardHint.getSceneCard().getOptions().contains(selectedOption)) {
                throw new IllegalArgumentException("Lua chon '" + selectedOption + "' khong ton tai tren the "
                        + boardHint.getSceneCard().getName());
            }

            boardHint.setSelectedOption(selectedOption);
        }

        session.setHintPlacementEndTime(0);
        startSimultaneousPresentationPhase(session);
    }

    public synchronized void attemptToSolve(String playerId, String targetPlayerId, String clueId, String meansId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.DISCUSSION_PRESENTATION) {
            throw new IllegalStateException("Hanh dong bi tu choi: Chi duoc phep pha an trong giai doan thao luan!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Nguoi choi khong ton tai!");
        }

        if (player.getRole() == RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Gian lan: Bac si Phap Y khong duoc phep pha an!");
        }

        if (!player.isHasBadge()) {
            throw new IllegalStateException("Ban da su dung het Huy hieu pha an!");
        }

        player.setHasBadge(false);

        boolean isCorrectTarget = session.getPlayers().get(targetPlayerId).getRole() == RoleType.MURDERER;
        boolean isCorrectClue = session.getSolutionClue().getId().equals(clueId);
        boolean isCorrectMeans = session.getSolutionMeans().getId().equals(meansId);

        if (isCorrectTarget && isCorrectClue && isCorrectMeans) {
            resetPresentationState(session);
            session.setCurrentPhase(GamePhase.WITNESS_REVERSAL);
        } else {
            log.info("Người chơi đoán sai.");
        }
    }

    public synchronized void startPresentation(String playerId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.DISCUSSION_PRESENTATION) {
            throw new IllegalStateException("Chi duoc trinh bay trong phase Thao luan!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Nguoi choi khong ton tai!");
        }

        if (player.getRole() == RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalStateException("Bac si phap y khong duoc trinh bay");
        }

        if (!playerId.equals(session.getPresentingPlayerId()) || !session.isTimerActive()) {
            throw new IllegalStateException("Luot trinh bay duoc sap xep tu dong. Hay doi he thong chuyen luot.");
        }
    }

    public synchronized void endPresentationEarly(String playerId) {
        throw new IllegalStateException("Luot trinh bay duoc tien hanh tu dong, khong the ket thuc som.");
    }

    public synchronized void startNextRound(String playerId) {
        GameSession session = getCurrentGame();

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Chi Bac si Phap Y moi co quyen ket thuc vong thao luan!");
        }

        advanceToNextRound(session);
    }

    public synchronized void replaceSceneTile(String playerId, String oldCardId, String newCardOption) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.FS_REPLACING_HINT) {
            throw new IllegalStateException("Khong phai luc de thay the!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Chi Bac si Phap Y moi co quyen thay the the hien truong!");
        }

        SceneTileHint toRemove = session.getBoardHints().stream()
                .filter(h -> h.getSceneCard().getId().equals(oldCardId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay the cu!"));

        if (toRemove.getSceneCard().getSceneType() != SceneType.RANDOM_SCENE) {
            throw new IllegalArgumentException("Chi duoc thay the cac the hien truong ngau nhien!");
        }

        session.getBoardHints().remove(toRemove);

        SceneCard newCard = cardRegistry.getRandomSceneCardsByType(SceneType.RANDOM_SCENE, 1).get(0);
        SceneTileHint newHint = new SceneTileHint(newCard);
        newHint.setSelectedOption(newCardOption);

        session.getBoardHints().add(newHint);

        startSimultaneousPresentationPhase(session);
    }

    public synchronized void attemptWitnessReversal(String playerId, String suspectId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.WITNESS_REVERSAL) {
            throw new IllegalStateException("Hanh dong bi tu choi: Hien khong phai la giai doan Lat Keo!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.MURDERER) {
            throw new IllegalArgumentException("Gian lan: Chi Ke Sat Nhan moi duoc quyen chi diem Nhan Chung!");
        }

        PlayerInGame suspect = session.getPlayers().get(suspectId);
        if (suspect == null) {
            throw new IllegalArgumentException("Nguoi bi chi diem khong ton tai!");
        }
        if (suspect.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("Sat nhan khong the tu chi diem chinh minh!");
        }

        if (suspect.getRole() == RoleType.WITNESS) {
            session.setWinningSide(RoleType.MURDERER);
        } else {
            session.setWinningSide(RoleType.INVESTIGATOR);
        }

        resetPresentationState(session);
        session.setCurrentPhase(GamePhase.GAME_OVER);
    }

    private void startSimultaneousPresentationPhase(GameSession session) {
        session.setCurrentPhase(GamePhase.DISCUSSION_PRESENTATION);
        resetPresentationState(session);
        session.setSimultaneousPresentationPhase(true);
        session.setPresentationEndTime(System.currentTimeMillis() + PRESENTATION_DURATION_MS);
    }

    private boolean applyAutomaticFlowTransitions(GameSession session) {
        if (session.getCurrentPhase() == GamePhase.CRIME_SELECTION) {
            return maybeAutoAssignCrimeSelection(session);
        }

        if (session.getCurrentPhase() == GamePhase.FS_PLACING_HINTS) {
            return maybeAutoAssignHints(session);
        }

        if (session.getCurrentPhase() != GamePhase.DISCUSSION_PRESENTATION || session.isTimerActive()) {
            return false;
        }

        if (session.isSimultaneousPresentationPhase()) {
            session.setSimultaneousPresentationPhase(false);
            session.setPresentationEndTime(0);
            return startNextIndividualPresentation(session);
        }

        if (session.getPresentingPlayerId() != null) {
            session.setPresentingPlayerId(null);
            session.setPresentationEndTime(0);
            return startNextIndividualPresentation(session);
        }

        return false;
    }

    private boolean maybeAutoAssignCrimeSelection(GameSession session) {
        if (System.currentTimeMillis() < session.getCrimeSelectionEndTime()) {
            return false;
        }

        PlayerInGame murderer = session.getPlayers().get(session.getMurdererId());
        if (murderer == null || murderer.getClueCards().isEmpty() || murderer.getMeansCards().isEmpty()) {
            throw new IllegalStateException("Khong the tu dong chon toi ac cho sat nhan.");
        }

        session.setSolutionClue(murderer.getClueCards().get(0));
        session.setSolutionMeans(murderer.getMeansCards().get(0));
        session.setCrimeSelectionEndTime(0);
        startHintPlacementPhase(session);
        return true;
    }

    private boolean maybeAutoAssignHints(GameSession session) {
        if (System.currentTimeMillis() < session.getHintPlacementEndTime()) {
            return false;
        }

        for (SceneTileHint boardHint : session.getBoardHints()) {
            if (boardHint.getSceneCard().getOptions().isEmpty()) {
                throw new IllegalStateException("Khong the tu dong dat hint cho the hien truong.");
            }
            boardHint.setSelectedOption(boardHint.getSceneCard().getOptions().get(0));
        }

        session.setHintPlacementEndTime(0);
        startSimultaneousPresentationPhase(session);
        return true;
    }

    private boolean startNextIndividualPresentation(GameSession session) {
        for (PlayerInGame participant : session.getPlayers().values()) {
            if (participant.getRole() == RoleType.FORENSIC_SCIENTIST || participant.isHasPresented()) {
                continue;
            }

            participant.setHasPresented(true);
            session.setPresentingPlayerId(participant.getPlayerId());
            session.setPresentationEndTime(System.currentTimeMillis() + PRESENTATION_DURATION_MS);
            return true;
        }

        advanceToNextRound(session);
        return true;
    }

    private void advanceToNextRound(GameSession session) {
        if (session.getCurrentRound() >= 3) {
            resetPresentationState(session);
            session.setWinningSide(RoleType.MURDERER);
            session.setCurrentPhase(GamePhase.GAME_OVER);
            return;
        }

        session.setCurrentRound(session.getCurrentRound() + 1);
        resetPresentationState(session);
        session.setCurrentPhase(GamePhase.FS_REPLACING_HINT);
    }

    private void startHintPlacementPhase(GameSession session) {
        session.setCurrentPhase(GamePhase.FS_PLACING_HINTS);
        session.setHintPlacementEndTime(System.currentTimeMillis() + HINT_PLACEMENT_DURATION_MS);
    }

    private void resetPresentationState(GameSession session) {
        session.setPresentingPlayerId(null);
        session.setPresentationEndTime(0);
        session.setSimultaneousPresentationPhase(false);

        for (PlayerInGame participant : session.getPlayers().values()) {
            if (participant.getRole() != RoleType.FORENSIC_SCIENTIST) {
                participant.setHasPresented(false);
            }
        }
    }
}
