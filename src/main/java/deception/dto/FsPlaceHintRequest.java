package deception.dto;


import lombok.Data;
import java.util.Map;

@Data
public class FsPlaceHintRequest {
    private String playerId;

    // Map chứa ID của SceneCard và Lựa chọn tương ứng
    // Ví dụ: { "SCENE_CAUSE_01": "Loss of Blood", "SCENE_LOC_01": "Hospital", ... }
    private Map<String, String> hints;
}