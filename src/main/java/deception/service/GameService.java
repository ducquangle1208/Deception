package deception.service;

import deception.constant.GamePhase;
import deception.constant.RoleType;
import deception.constant.SceneType;
import deception.gameplay.GameSession;
import deception.gameplay.SceneTileHint;
import deception.model.PlayerInGame;
import deception.model.abstract_model.Card;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import deception.model.cards.SceneCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class GameService {

    private final CardRegistryService cardRegistry;

    private GameSession currentGameSession = null;

    public GameService(CardRegistryService cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    public void setupNewGame(List<String> playerIds) {
        if (playerIds.size() != 6) {
            throw new IllegalArgumentException("Trò chơi yêu cầu chính xác 6 người chơi!");
        }

        GameSession session = new GameSession();
        session.setCurrentPhase(GamePhase.CRIME_SELECTION);
        session.setCurrentRound(1);

        List<RoleType> roles = Arrays.asList(
                RoleType.FORENSIC_SCIENTIST, RoleType.MURDERER, RoleType.ACCOMPLICE,
                RoleType.WITNESS, RoleType.INVESTIGATOR, RoleType.INVESTIGATOR
        );
        Collections.shuffle(roles);

        List<ClueCard> deckClues = cardRegistry.getAllClueCards();
        List<MeansCard> deckMeans = cardRegistry.getAllMeansCards();
        Collections.shuffle(deckClues);
        Collections.shuffle(deckMeans);

        Map<String, PlayerInGame> playersMap = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            PlayerInGame player = new PlayerInGame();
            player.setPlayerId(playerIds.get(i));
            player.setPlayerName("Player " + (i + 1));
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
            throw new IllegalStateException("Chưa có ván game nào được tạo!");
        }
        return this.currentGameSession;
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
            throw new IllegalStateException("Hành động bị từ chối: Hiện không phải là giai đoạn chọn Hung khí và Vật chứng!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Người chơi không tồn tại trong phòng!");
        }
        if (player.getRole() != RoleType.MURDERER) {
            throw new IllegalArgumentException("Gian lận: Chỉ Kẻ Sát Nhân mới được phép chọn Đáp án!");
        }

        ClueCard selectedClue = player.getClueCards().stream()
                .filter(c -> c.getId().equals(clueId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Manh mối (Clue) không hợp lệ hoặc không thuộc về bạn!"));

        MeansCard selectedMeans = player.getMeansCards().stream()
                .filter(m -> m.getId().equals(meansId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Hung khí (Means) không hợp lệ hoặc không thuộc về bạn!"));

        session.setSolutionClue(selectedClue);
        session.setSolutionMeans(selectedMeans);

        session.setCurrentPhase(GamePhase.FS_PLACING_HINTS);
    }

    public synchronized void placeInitialHints(String playerId, Map<String, String> hints) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.FS_PLACING_HINTS) {
            throw new IllegalStateException("Hành động bị từ chối: Hiện không phải là giai đoạn Bác sĩ pháp y đặt Hint!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Gian lận: Chỉ Bác sĩ pháp y mới được quyền đặt Hint!");
        }

        if (hints == null || hints.size() != 6) {
            throw new IllegalArgumentException("Bác sĩ pháp y bắt buộc phải đặt chính xác 6 viên đạn lên 6 thẻ hiện trường!");
        }

        for (SceneTileHint boardHint : session.getBoardHints()) {
            String cardId = boardHint.getSceneCard().getId();

            if (!hints.containsKey(cardId)) {
                throw new IllegalArgumentException("Thiếu Hint cho thẻ hiện trường: " + boardHint.getSceneCard().getName());
            }

            String selectedOption = hints.get(cardId);

            if (!boardHint.getSceneCard().getOptions().contains(selectedOption)) {
                throw new IllegalArgumentException("Lựa chọn '" + selectedOption + "' không tồn tại trên thẻ " + boardHint.getSceneCard().getName());
            }

            boardHint.setSelectedOption(selectedOption);
        }

        session.setCurrentPhase(GamePhase.DISCUSSION_PRESENTATION);
    }

    /**
     * Hàm này đại diện cho phase có người muốn phá án.
     *
     * @param playerId Id của người đang muốn phá án
     * @param targetPlayerId Id của người được cho là kẻ ám sát
     * @param clueId Hiện vật của người được cho là kẻ ám sát
     * @param meansId Vũ khí của nguười được cho là kẻ ám sát
     */
    public synchronized void attemptToSolve(String playerId, String targetPlayerId, String clueId, String meansId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.DISCUSSION_PRESENTATION) {
            throw new IllegalStateException("Hành động bị từ chối: Chỉ được phép phá án trong giai đoạn Thảo luận!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Người chơi không tồn tại!");
        }

        if (player.getRole() == RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Gian lận: Bác sĩ Pháp Y không được phép phá án!");
        }

        if (!player.isHasBadge()) {
            throw new IllegalStateException("Bạn đã sử dụng hết Huy hiệu phá án!");
        }

        player.setHasBadge(false);

        boolean isCorrectTarget = session.getPlayers().get(targetPlayerId).getRole() == RoleType.MURDERER;
        boolean isCorrectClue = session.getSolutionClue().getId().equals(clueId);
        boolean isCorrectMeans = session.getSolutionMeans().getId().equals(meansId);

        if (isCorrectTarget && isCorrectClue && isCorrectMeans) {
            //chuyển phase
            session.setCurrentPhase(GamePhase.WITNESS_REVERSAL);
        } else {
            System.out.println("Bạn đã sai!");
            /*
            * Game goes on
            */
        }
    }

    public synchronized void startPresentation(String playerId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.DISCUSSION_PRESENTATION) {
            throw new IllegalStateException("Chỉ được trình bày trong phase Thảo luận!");
        }

        if (session.getPlayers().get(playerId).getRole() == RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalStateException("Bác sĩ pháp y không được trình bày");
        }

        if (session.isTimerActive()) {
            long remainingSeconds = (session.getPresentationEndTime() - System.currentTimeMillis()) / 1000;
            throw new IllegalStateException("Hành động bị từ chối: Người chơi "
                    + session.getPresentingPlayerId() + " đang trình bày. Vui lòng đợi "
                    + remainingSeconds + " giây nữa!");
        }

        // Set timer 45 sec và người gần nhất trình bày
        session.setPresentingPlayerId(playerId);
        session.setPresentationEndTime(System.currentTimeMillis() + 45000);

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player.isHasPresented()) {
            throw new IllegalStateException("Bạn đã trình bày trong vòng này rồi!");
        }

        player.setHasPresented(true);


    }

    /**
     * Player can end their presentation sooner than 45 sec by calling this method.
     *
     * @param playerId who currently is presenting
     */
    public synchronized void endPresentationEarly(String playerId) {
        GameSession session = getCurrentGame();

        if (session.isTimerActive() && playerId.equals(session.getPresentingPlayerId())) {
            session.setPresentationEndTime(0);
            session.setPresentingPlayerId(null);
        } else {
            throw new IllegalStateException("Bạn không có quyền kết thúc lượt của người khác!");
        }
    }

    public synchronized void startNextRound(String playerId) {
        GameSession session = getCurrentGame();

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Chỉ Bác sĩ Pháp Y mới có quyền kết thúc vòng thảo luận!");
        }

        if (session.getCurrentRound() >= 3) {
            session.setWinningSide(RoleType.MURDERER);
            session.setCurrentPhase(GamePhase.GAME_OVER);
            return;
        }

        session.setCurrentRound(session.getCurrentRound() + 1);

        for (PlayerInGame p : session.getPlayers().values()) {
            p.setHasPresented(false);
        }

        session.setCurrentPhase(GamePhase.FS_REPLACING_HINT);
    }

    public synchronized void replaceSceneTile(String oldCardId, String newCardOption) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.FS_REPLACING_HINT) {
            throw new IllegalStateException("Không phải lúc để thay thẻ!");
        }

        SceneTileHint toRemove = session.getBoardHints().stream()
                .filter(h -> h.getSceneCard().getId().equals(oldCardId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thẻ cũ!"));

        if (toRemove.getSceneCard().getSceneType() != SceneType.RANDOM_SCENE) {
            throw new IllegalArgumentException("Chỉ được thay thế các thẻ hiện trường ngẫu nhiên!");
        }

        session.getBoardHints().remove(toRemove);

        SceneCard newCard = cardRegistry.getRandomSceneCardsByType(SceneType.RANDOM_SCENE, 1).get(0);
        SceneTileHint newHint = new SceneTileHint(newCard);
        newHint.setSelectedOption(newCardOption);

        session.getBoardHints().add(newHint);

        session.setCurrentPhase(GamePhase.DISCUSSION_PRESENTATION);
    }

    public synchronized void attemptWitnessReversal(String playerId, String suspectId) {
        GameSession session = getCurrentGame();

        if (session.getCurrentPhase() != GamePhase.WITNESS_REVERSAL) {
            throw new IllegalStateException("Hành động bị từ chối: Hiện không phải là giai đoạn Lật Kèo!");
        }

        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.MURDERER) {
            throw new IllegalArgumentException("Gian lận: Chỉ Kẻ Sát Nhân mới được quyền chỉ điểm Nhân Chứng!");
        }

        PlayerInGame suspect = session.getPlayers().get(suspectId);
        if (suspect == null) {
            throw new IllegalArgumentException("Người bị chỉ điểm không tồn tại!");
        }
        if (suspect.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("Sát nhân không thể tự chỉ điểm chính mình!");
        }

        if (suspect.getRole() == RoleType.WITNESS) {
            session.setWinningSide(RoleType.MURDERER);
        } else {
            session.setWinningSide(RoleType.INVESTIGATOR);
        }

        session.setCurrentPhase(GamePhase.GAME_OVER);
    }
}