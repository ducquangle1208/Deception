package deception.dto;

import deception.constant.VisibleRole;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PlayerDTO {
    private String playerId;
    private String playerName;

    private VisibleRole role;

    private List<ClueCard> clueCards;
    private List<MeansCard> meansCards;

    private boolean hasBadge;
    private boolean hasPresented;
}