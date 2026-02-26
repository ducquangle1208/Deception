package deception.dto;

import lombok.Data;

@Data
public class SolveAttemptRequest {
    private String playerId;       //id of who's attempting
    private String targetPlayerId; //if of who is murderer allegedly
    /**
     * Clue and means of alleged murderer
     */
    private String clueId;
    private String meansId;
}