package deception.dto;

import deception.constant.GamePhase;
import deception.gameplay.SceneTileHint;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GameSessionDTO {
    private GamePhase currentPhase;
    private int currentRound;

    // Board thì ai cũng thấy như nhau
    private List<SceneTileHint> boardHints;

    // Danh sách người chơi ĐÃ ĐƯỢC LỌC ROLE
    private List<PlayerDTO> players;

    // Đáp án của vụ án (Chỉ FS, Sát nhân và Tòng phạm mới có data ở 2 trường này)
    private ClueCard solutionClue;
    private MeansCard solutionMeans;
}