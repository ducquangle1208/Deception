package deception.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LobbyPlayer {
    private String playerId;
    private String username;
    private boolean isReady;
}
