package deception.dto;

import lombok.Data;

@Data
public class ReplaceTileRequest {
    private String playerId;
    private String oldCardId;
    private String newCardOption;
}