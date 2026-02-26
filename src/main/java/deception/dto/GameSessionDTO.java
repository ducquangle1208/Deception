package deception.dto;

import deception.constant.GamePhase;
import deception.constant.RoleType;
import deception.gameplay.SceneTileHint;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GameSessionDTO {
    private String presentingPlayerId;
    private long presentationEndTime;
    private GamePhase currentPhase;
    private int currentRound;
    private RoleType winningSide;

    private List<SceneTileHint> boardHints;

    private List<PlayerDTO> players;

    private ClueCard solutionClue;
    private MeansCard solutionMeans;
}