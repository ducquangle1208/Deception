package model.player;

import model.abstract_model.User;

public class Witness extends User {

    @Override
    public boolean canReceiveCards() {
        return true;
    }
}
