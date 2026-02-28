package deception.dto;

import lombok.Data;

@Data
public class WitnessReversalRequest {
    private String playerId;
    private String suspectId;
}