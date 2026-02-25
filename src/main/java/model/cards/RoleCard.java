package model.cards;

import constant.CardType;
import constant.RoleType;
import lombok.Getter;
import lombok.Setter;
import model.abstract_model.Card;

@Getter
@Setter
public class RoleCard extends Card {

    private RoleType roleType;

    @Override
    public CardType getType() {
        return CardType.ROLE;
    }
}
