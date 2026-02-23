package model.cards;

import constant.RoleType;
import lombok.Getter;
import lombok.Setter;
import model.abstract_model.Card;

@Getter
@Setter
public class RoleCard extends Card {

    private final RoleType roleType;

    public RoleCard(String id, RoleType roleType) {
        super(id, roleType.name());
        this.roleType = roleType;
    }


    @Override
    public String getType() {
        return "Role Card";
    }
}
