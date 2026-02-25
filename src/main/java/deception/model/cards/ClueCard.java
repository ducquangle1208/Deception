package deception.model.cards;

import deception.constant.CardType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import deception.model.abstract_model.Card;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClueCard extends Card {

    private String clue;

    @Override
    public CardType getType() {
        return CardType.CLUE;
    }
}
