package deception.mapper;

import deception.constant.RoleType;
import deception.constant.VisibleRole;
import deception.dto.GameSessionDTO;
import deception.dto.PlayerDTO;
import deception.gameplay.GameSession;
import deception.model.PlayerInGame;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class GameStateMapper {

    public GameSessionDTO toDTO(GameSession session, String requestingPlayerId) {

        // 1. Tìm ra người đang request là ai để biết quyền hạn của họ
        PlayerInGame requestingPlayer = session.getPlayers().get(requestingPlayerId);
        if (requestingPlayer == null) {
            throw new IllegalArgumentException("Người chơi không có trong phòng này!");
        }

        RoleType myRole = requestingPlayer.getRole();

        // 2. Map danh sách Player
        List<PlayerDTO> playerDTOs = new ArrayList<>();
        for (PlayerInGame p : session.getPlayers().values()) {
            playerDTOs.add(mapPlayer(p, myRole, requestingPlayerId));
        }

        // 3. Quyết định xem có được xem Đáp Án (Solution) không
        boolean canSeeSolution = (myRole == RoleType.FORENSIC_SCIENTIST
                || myRole == RoleType.MURDERER
                || myRole == RoleType.ACCOMPLICE);

        // 4. Build DTO tổng
        return GameSessionDTO.builder()
                .roomId(session.getRoomId())
                .currentPhase(session.getCurrentPhase())
                .currentRound(session.getCurrentRound())
                .boardHints(session.getBoardHints())
                .players(playerDTOs)
                .solutionClue(canSeeSolution ? session.getSolutionClue() : null)
                .solutionMeans(canSeeSolution ? session.getSolutionMeans() : null)
                .build();
    }

    // --- HÀM LOGIC CHE GIẤU ROLE (QUAN TRỌNG NHẤT) ---
    private PlayerDTO mapPlayer(PlayerInGame targetPlayer, RoleType myRole, String myPlayerId) {
        VisibleRole visibleRole;

        // Bác sĩ pháp y thì ai cũng biết
        if (targetPlayer.getRole() == RoleType.FORENSIC_SCIENTIST) {
            visibleRole = VisibleRole.FORENSIC_SCIENTIST;
        }
        // Xem chính bản thân mình
        else if (targetPlayer.getPlayerId().equals(myPlayerId)) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        // Bác sĩ pháp y nhìn thấy hết tất cả role
        else if (myRole == RoleType.FORENSIC_SCIENTIST) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        // Sát nhân và Tòng phạm nhìn thấy nhau
        else if ((myRole == RoleType.MURDERER || myRole == RoleType.ACCOMPLICE) &&
                (targetPlayer.getRole() == RoleType.MURDERER || targetPlayer.getRole() == RoleType.ACCOMPLICE)) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        // Nhân chứng nhìn thấy Sát nhân và Tòng phạm, nhưng không biết ai là ai -> Đánh dấu là SUSPECT
        else if (myRole == RoleType.WITNESS &&
                (targetPlayer.getRole() == RoleType.MURDERER || targetPlayer.getRole() == RoleType.ACCOMPLICE)) {
            visibleRole = VisibleRole.SUSPECT;
        }
        // Còn lại (Thám tử nhìn người khác, hoặc Hung thủ nhìn Thám tử/Nhân chứng) -> MÙ TỊT
        else {
            visibleRole = VisibleRole.UNKNOWN;
        }

        return PlayerDTO.builder()
                .playerId(targetPlayer.getPlayerId())
                .playerName(targetPlayer.getPlayerName())
                .role(visibleRole)
                .clueCards(targetPlayer.getClueCards())
                .meansCards(targetPlayer.getMeansCards())
                .hasBadge(targetPlayer.isHasBadge())
                .hasPresented(targetPlayer.isHasPresented())
                .build();
    }
}