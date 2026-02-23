package model.player;

import constant.RoleType;
import model.abstract_model.User;
import model.cards.RoleCard;

public class ForensicScientist extends User {

    public ForensicScientist(Long id, String username) {
        super(id, username, new RoleCard(id + "_role", RoleType.FORENSIC_SCIENTIST));
    }

    @Override
    public boolean canReceiveCards() {
        return false;
    }
}