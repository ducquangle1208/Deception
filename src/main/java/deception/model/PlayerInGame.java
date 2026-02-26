package deception.model;

import deception.constant.RoleType;
import lombok.Data;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;

import java.util.List;

@Data
public class PlayerInGame {
    private String playerId;
    private String playerName;

    private RoleType role;

    private List<ClueCard> clueCards;
    private List<MeansCard> meansCards;

    private boolean hasBadge = true;

    private boolean hasPresented = false;
}