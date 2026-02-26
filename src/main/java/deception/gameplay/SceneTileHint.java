package deception.gameplay;

import lombok.Data;

import deception.model.cards.SceneCard;

@Data
public class SceneTileHint {

    private SceneCard sceneCard;

    private String selectedOption;

    public SceneTileHint(SceneCard sceneCard) {
        this.sceneCard = sceneCard;
        this.selectedOption = null;
    }
}