package deception.dto;

import lombok.Data;

@Data
public class SolveAttemptRequest {
    private String playerId;       // ID của người đang thử phá án (người gửi request)
    private String targetPlayerId; // ID của người bị tình nghi là Kẻ Sát Nhân
    private String clueId;         // ID của lá Manh mối (Clue) bị nghi ngờ
    private String meansId;        // ID của lá Hung khí (Means) bị nghi ngờ
}