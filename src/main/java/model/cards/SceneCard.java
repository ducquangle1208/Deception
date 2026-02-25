package model.cards;

import constant.CardType;
import constant.SceneType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import model.abstract_model.Card;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SceneCard extends Card {

    // Ví dụ: ["Phòng khách", "Nhà bếp", "Công viên", "Siêu thị", "Trường học", "Nhà kho"]
    private List<String> options;

    private SceneType sceneType;

    @Override
    public CardType getType() {
        return CardType.SCENE;
    }
}