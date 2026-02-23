package model.abstract_model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Card {

    protected String id;
    protected String name;

    public abstract String getType();

    @Override
    public String toString() {
        return getType() + ": " + name;
    }
}