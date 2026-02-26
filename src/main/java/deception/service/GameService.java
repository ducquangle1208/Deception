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
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    private final CardRegistryService cardRegistry;

    // BIẾN DUY NHẤT LƯU TRẠNG THÁI GAME CHO TOÀN BỘ APP
    private GameSession currentGameSession = null;

    public GameService(CardRegistryService cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    // Không cần truyền tham số roomId nữa
    public void setupNewGame(List<String> playerIds) {
        if (playerIds.size() != 6) {
            throw new IllegalArgumentException("Trò chơi yêu cầu chính xác 6 người chơi!");
        }

        GameSession session = new GameSession();
        session.setCurrentPhase(GamePhase.CRIME_SELECTION);
        session.setCurrentRound(1);

        // 1. CHIA VAI TRÒ
        List<RoleType> roles = Arrays.asList(
                RoleType.FORENSIC_SCIENTIST, RoleType.MURDERER, RoleType.ACCOMPLICE,
                RoleType.WITNESS, RoleType.INVESTIGATOR, RoleType.INVESTIGATOR
        );
        Collections.shuffle(roles);

        // 2. XÁO BÀI
        List<ClueCard> deckClues = cardRegistry.getAllClueCards();
        List<MeansCard> deckMeans = cardRegistry.getAllMeansCards();
        Collections.shuffle(deckClues);
        Collections.shuffle(deckMeans);

        // 3. KHỞI TẠO NGƯỜI CHƠI
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

        // 4. KHỞI TẠO BẢN ĐỒ HIỆN TRƯỜNG
        List<SceneTileHint> boardHints = new ArrayList<>();
        boardHints.add(new SceneTileHint(cardRegistry.getRandomSceneCardByType(SceneType.CAUSE_OF_DEATH)));
        boardHints.add(new SceneTileHint(cardRegistry.getRandomSceneCardByType(SceneType.LOCATION_OF_CRIME)));
        List<SceneCard> randomScenes = cardRegistry.getRandomSceneCardsByType(SceneType.RANDOM_SCENE, 4);
        for (SceneCard card : randomScenes) {
            boardHints.add(new SceneTileHint(card));
        }
        session.setBoardHints(boardHints);

        // 5. GHI ĐÈ VÀO BIẾN TOÀN CỤC
        this.currentGameSession = session;
    }

    // Hàm lấy game hiện tại
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

    // Thêm hàm này vào trong GameService
    public synchronized void selectCrime(String playerId, String clueId, String meansId) {
        GameSession session = getCurrentGame();

        // 1. Kiểm tra xem có đúng là đang ở Phase Chọn đáp án không
        if (session.getCurrentPhase() != GamePhase.CRIME_SELECTION) {
            throw new IllegalStateException("Hành động bị từ chối: Hiện không phải là giai đoạn chọn Hung khí và Vật chứng!");
        }

        // 2. Tìm người chơi gửi request và kiểm tra Role
        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Người chơi không tồn tại trong phòng!");
        }
        if (player.getRole() != RoleType.MURDERER) {
            throw new IllegalArgumentException("Gian lận: Chỉ Kẻ Sát Nhân mới được phép chọn Đáp án!");
        }

        // 3. XÁC THỰC BÀI: Đảm bảo 2 lá bài được chọn THỰC SỰ nằm trên tay của Kẻ Sát Nhân
        ClueCard selectedClue = player.getClueCards().stream()
                .filter(c -> c.getId().equals(clueId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Manh mối (Clue) không hợp lệ hoặc không thuộc về bạn!"));

        MeansCard selectedMeans = player.getMeansCards().stream()
                .filter(m -> m.getId().equals(meansId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Hung khí (Means) không hợp lệ hoặc không thuộc về bạn!"));

        // 4. Lưu Đáp án vào Session
        session.setSolutionClue(selectedClue);
        session.setSolutionMeans(selectedMeans);

        // 5. Chuyển Phase sang cho Bác sĩ pháp y làm việc
        // Theo luật chuẩn, sau khi chọn xong, Bác sĩ pháp y sẽ bắt đầu đặt 6 viên đạn lên các Scene Tile
        session.setCurrentPhase(GamePhase.FS_PLACING_HINTS);
    }

    // Thêm hàm này vào GameService
    public synchronized void placeInitialHints(String playerId, Map<String, String> hints) {
        GameSession session = getCurrentGame();

        // 1. Kiểm tra Phase
        if (session.getCurrentPhase() != GamePhase.FS_PLACING_HINTS) {
            throw new IllegalStateException("Hành động bị từ chối: Hiện không phải là giai đoạn Bác sĩ pháp y đặt Hint!");
        }

        // 2. Kiểm tra Role
        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null || player.getRole() != RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Gian lận: Chỉ Bác sĩ pháp y mới được quyền đặt Hint!");
        }

        // 3. Kiểm tra số lượng Hint
        if (hints == null || hints.size() != 6) {
            throw new IllegalArgumentException("Bác sĩ pháp y bắt buộc phải đặt chính xác 6 viên đạn lên 6 thẻ hiện trường!");
        }

        // 4. Validate và Cập nhật vị trí viên đạn vào Board
        for (SceneTileHint boardHint : session.getBoardHints()) {
            String cardId = boardHint.getSceneCard().getId();

            // Check xem FS có gửi ID thẻ này lên không
            if (!hints.containsKey(cardId)) {
                throw new IllegalArgumentException("Thiếu Hint cho thẻ hiện trường: " + boardHint.getSceneCard().getName());
            }

            String selectedOption = hints.get(cardId);

            // Chống Hack: Check xem Text gửi lên có thực sự nằm trong 6 lựa chọn của thẻ đó không
            if (!boardHint.getSceneCard().getOptions().contains(selectedOption)) {
                throw new IllegalArgumentException("Lựa chọn '" + selectedOption + "' không tồn tại trên thẻ " + boardHint.getSceneCard().getName());
            }

            // Ghi nhận viên đạn đã được đặt
            boardHint.setSelectedOption(selectedOption);
        }

        // 5. Chuyển Phase sang giai đoạn Thảo Luận & Phá Án
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

        // 1. Kiểm tra Phase
        if (session.getCurrentPhase() != GamePhase.DISCUSSION_PRESENTATION) {
            throw new IllegalStateException("Hành động bị từ chối: Chỉ được phép phá án trong giai đoạn Thảo luận!");
        }

        // 2. Lấy thông tin người chơi đang thử phá án
        PlayerInGame player = session.getPlayers().get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Người chơi không tồn tại!");
        }

        // Bác sĩ Pháp Y không được phép phá án
        if (player.getRole() == RoleType.FORENSIC_SCIENTIST) {
            throw new IllegalArgumentException("Gian lận: Bác sĩ Pháp Y không được phép phá án!");
        }

        // Kiểm tra xem họ còn Huy hiệu không
        if (!player.isHasBadge()) {
            throw new IllegalStateException("Bạn đã sử dụng hết Huy hiệu phá án!");
        }

        // 3. THU HỒI HUY HIỆU (Bất kể đúng hay sai, cứ thử là mất huy hiệu)
        player.setHasBadge(false);

        // 4. KIỂM TRA ĐÁP ÁN (Sự thật phơi bày)
        // Chúng ta so sánh 3 yếu tố: Kẻ sát nhân, Manh mối, Hung khí
        boolean isCorrectTarget = session.getPlayers().get(targetPlayerId).getRole() == RoleType.MURDERER;
        boolean isCorrectClue = session.getSolutionClue().getId().equals(clueId);
        boolean isCorrectMeans = session.getSolutionMeans().getId().equals(meansId);

        if (isCorrectTarget && isCorrectClue && isCorrectMeans) {
            // PHÁ ÁN THÀNH CÔNG!
            // Game chuyển sang Phase cuối: Cho phép Sát Nhân/Tòng Phạm lật ngược thế cờ bằng cách tìm ra Nhân Chứng
            session.setCurrentPhase(GamePhase.WITNESS_REVERSAL);
        } else {
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

        if (session.isTimerActive()) {
            long remainingSeconds = (session.getPresentationEndTime() - System.currentTimeMillis()) / 1000;
            throw new IllegalStateException("Hành động bị từ chối: Người chơi "
                    + session.getPresentingPlayerId() + " đang trình bày. Vui lòng đợi "
                    + remainingSeconds + " giây nữa!");
        }

        // Set timer 45 sec
        session.setPresentingPlayerId(playerId);
        session.setPresentationEndTime(System.currentTimeMillis() + 45000);

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
}