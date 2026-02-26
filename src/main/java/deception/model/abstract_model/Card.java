package deception.model.abstract_model;

import deception.constant.CardType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Card {

    @EqualsAndHashCode.Include
    protected String id;

    protected String name;

    protected String imageUrl;


    public abstract CardType getType();

    @Override
    public String toString() {
        return getType().name() + ": " + name;
    }
}