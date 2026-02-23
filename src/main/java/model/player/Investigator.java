package model.player;

import model.abstract_model.User;

public class Investigator extends User {
    @Override
    public boolean canReceiveCards() {
        return true;
    }
}
