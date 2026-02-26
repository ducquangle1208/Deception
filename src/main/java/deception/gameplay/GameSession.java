package deception.gameplay;

import deception.constant.GamePhase;
import deception.constant.RoleType;
import lombok.Data;
import deception.model.PlayerInGame;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import java.util.Map;
import java.util.List;

@Data
public class GameSession {
    private GamePhase currentPhase = GamePhase.WAITING_FOR_PLAYERS;

    private String presentingPlayerId = null;

    private RoleType winningSide;

    private long presentationEndTime = 0;

    private Map<String, PlayerInGame> players;

    private int currentRound = 1;

    //Game's solution
    private String murdererId;
    private ClueCard solutionClue;
    private MeansCard solutionMeans;

    private List<SceneTileHint> boardHints;

    public boolean isTimerActive() {
        return System.currentTimeMillis() < presentationEndTime;
    }

}