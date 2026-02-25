package deception.dto;

import lombok.Data;

@Data
public class CrimeSelectionRequest {
    private String playerId; // Tạm thời dùng để test, thực tế sau này sẽ lấy từ Token (JWT)
    private String clueId;
    private String meansId;
}