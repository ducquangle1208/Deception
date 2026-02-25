package gameplay;

import constant.GamePhase;
import lombok.Data;
import model.PlayerInGame;
import model.cards.ClueCard;
import model.cards.MeansCard;
import java.util.Map;
import java.util.List;

@Data
public class GameSession {
    private String roomId;
    private GamePhase currentPhase = GamePhase.WAITING_FOR_PLAYERS;

    // Quản lý người chơi trong phòng (Dùng Map với key là playerId để tra cứu nhanh)
    private Map<String, PlayerInGame> players;

    // Quản lý Vòng chơi (Round 1, 2, 3)
    private int currentRound = 1;

    // --- ĐÁP ÁN CỦA VỤ ÁN (SOLUTION) ---
    private String murdererId; // Lưu ID của kẻ sát nhân để check nhanh
    private ClueCard solutionClue;
    private MeansCard solutionMeans;

    // --- BÀN CHƠI CHUNG (BOARD) ---
    // Danh sách các Scene Tile đang hiển thị và vị trí viên đạn FS đã đặt
    private List<SceneTileHint> boardHints;

    // Các hàm helper để chuyển state (Ví dụ)
    public void advancePhase(GamePhase nextPhase) {
        this.currentPhase = nextPhase;
        // Có thể emit event WebSocket tại đây để báo cho toàn bộ client
    }
}