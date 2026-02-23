package model.abstract_model;

import constant.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.cards.ClueCard;
import model.cards.MeanCard;
import model.cards.RoleCard;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    protected Long id;
    protected String username;

    protected RoleCard roleCard;
    protected List<ClueCard> clueCards = new ArrayList<>();
    protected List<MeanCard> meansCards = new ArrayList<>();

    public RoleType getRole() {
        return roleCard.getRoleType();
    }

    public User(Long id, String username, RoleCard roleCard) {
        this.id = id;
        this.username = username;
        this.roleCard = roleCard;
    }

    public abstract boolean canReceiveCards();

}
