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

    // Service này cung cấp toàn bộ thẻ bài trong game (Dữ liệu tĩnh)
    private final CardRegistryService cardRegistry;

    public GameService(CardRegistryService cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    public GameSession setupNewGame(String roomId, List<String> playerIds) {
        if (playerIds.size() != 6) {
            throw new IllegalArgumentException("Trò chơi yêu cầu chính xác 6 người chơi!");
        }

        GameSession session = new GameSession();
        session.setRoomId(roomId);
        session.setCurrentPhase(GamePhase.CRIME_SELECTION); // Setup xong chuyển ngay sang lúc hung thủ chọn bài

        // 1. CHIA VAI TRÒ (Assign Roles)
        List<RoleType> roles = Arrays.asList(
                RoleType.FORENSIC_SCIENTIST,
                RoleType.MURDERER,
                RoleType.ACCOMPLICE,
                RoleType.WITNESS,
                RoleType.INVESTIGATOR,
                RoleType.INVESTIGATOR
        );
        Collections.shuffle(roles);

        // 2. XÁO BÀI (Shuffle Cards)
        // Lấy danh sách copy từ CardRegistry để xáo mà không ảnh hưởng data gốc
        List<ClueCard> deckClues = new ArrayList<>(cardRegistry.getAllClueCards());
        List<MeansCard> deckMeans = new ArrayList<>(cardRegistry.getAllMeansCards());
        Collections.shuffle(deckClues);
        Collections.shuffle(deckMeans);

        // 3. KHỞI TẠO NGƯỜI CHƠI VÀ PHÁT BÀI
        Map<String, PlayerInGame> playersMap = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            PlayerInGame player = new PlayerInGame();
            player.setPlayerId(playerIds.get(i));
            player.setRole(roles.get(i));

            if (player.getRole() == RoleType.FORENSIC_SCIENTIST) {
                // FS không cầm bài Clue và Means, không có huy hiệu phá án
                player.setClueCards(new ArrayList<>());
                player.setMeansCards(new ArrayList<>());
                player.setHasBadge(false);
            } else {
                // Các role khác nhận 4 thẻ Clue và 4 thẻ Means
                player.setClueCards(drawCards(deckClues));
                player.setMeansCards(drawCards(deckMeans));
                player.setHasBadge(true);
            }
            playersMap.put(player.getPlayerId(), player);
        }
        session.setPlayers(playersMap);

        // 4. KHỞI TẠO BẢN ĐỒ HIỆN TRƯỜNG (Scene Board)
        List<SceneTileHint> boardHints = new ArrayList<>();

        // Theo luật: 1 Thẻ Nguyên nhân tử vong (Cause of Death)
        SceneCard causeOfDeathCard = cardRegistry.getRandomSceneCardByType(SceneType.CAUSE_OF_DEATH);
        boardHints.add(new SceneTileHint(causeOfDeathCard));

        // 1 Thẻ Địa điểm phạm tội (Location)
        SceneCard locationCard = cardRegistry.getRandomSceneCardByType(SceneType.LOCATION_OF_CRIME);
        boardHints.add(new SceneTileHint(locationCard));

        // 4 Thẻ ngẫu nhiên (Random Scenes)
        List<SceneCard> randomScenes = cardRegistry.getRandomSceneCardsByType(SceneType.RANDOM_SCENE, 4);
        for (SceneCard card : randomScenes) {
            boardHints.add(new SceneTileHint(card));
        }
        session.setBoardHints(boardHints);

        return session;
    }

    // Hàm helper rút bài
    private <T extends Card> List<T> drawCards(List<T> deck) {
        List<T> drawn = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            drawn.add(deck.remove(0)); // Rút từ trên đầu bộ bài
        }
        return drawn;
    }
}