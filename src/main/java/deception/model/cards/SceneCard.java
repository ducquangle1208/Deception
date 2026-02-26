package deception.model.cards;

import deception.constant.CardType;
import deception.constant.SceneType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import deception.model.abstract_model.Card;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SceneCard extends Card {

    private List<String> options;

    private SceneType sceneType;

    @Override
    public CardType getType() {
        return CardType.SCENE;
    }
}