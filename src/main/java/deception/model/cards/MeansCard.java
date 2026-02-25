package deception.model.cards;

import deception.constant.CardType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import deception.model.abstract_model.Card;

@EqualsAndHashCode(callSuper = true)
@Data
public class MeansCard extends Card {

    private String weapon;
    @Override
    public CardType getType() {
        return CardType.MEANS;
    }
}
