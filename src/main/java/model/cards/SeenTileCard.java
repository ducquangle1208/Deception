package model.cards;

import lombok.*;
import model.abstract_model.Card;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SeenTileCard extends Card {

    private List<String> descriptions = new ArrayList<String>();

    @Override
    public String getType() {
        return "";
    }
}
