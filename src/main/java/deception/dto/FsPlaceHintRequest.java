package deception.dto;


import lombok.Data;
import java.util.Map;

@Data
public class FsPlaceHintRequest {
    private String playerId;

    private Map<String, String> hints;
}