package deception.dto;

import lombok.Data;

@Data
public class CrimeSelectionRequest {
    private String playerId;
    private String clueId;
    private String meansId;
}