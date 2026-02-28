package deception.mapper;

import deception.constant.GamePhase;
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

        PlayerInGame requestingPlayer = session.getPlayers().get(requestingPlayerId);
        if (requestingPlayer == null) {
            throw new IllegalArgumentException("Người chơi không có trong phòng này!");
        }

        RoleType myRole = requestingPlayer.getRole();

        List<PlayerDTO> playerDTOs = new ArrayList<>();
        for (PlayerInGame p : session.getPlayers().values()) {
            playerDTOs.add(mapPlayer(session,p, myRole, requestingPlayerId));
        }

        boolean canSeeSolution = (myRole == RoleType.FORENSIC_SCIENTIST
                || myRole == RoleType.MURDERER
                || myRole == RoleType.ACCOMPLICE);

        return GameSessionDTO.builder()
                .currentPhase(session.getCurrentPhase())
                .currentRound(session.getCurrentRound())
                .boardHints(session.getBoardHints())
                .players(playerDTOs)
                .solutionClue(canSeeSolution ? session.getSolutionClue() : null)
                .solutionMeans(canSeeSolution ? session.getSolutionMeans() : null)
                .presentingPlayerId(session.getPresentingPlayerId())
                .presentationEndTime(session.getPresentationEndTime())
                .winningSide(session.getWinningSide())
                .build();
    }


    private PlayerDTO mapPlayer(GameSession session, PlayerInGame targetPlayer, RoleType myRole, String myPlayerId) {
        VisibleRole visibleRole;

        if (session.getCurrentPhase() == GamePhase.GAME_OVER) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        else if (targetPlayer.getRole() == RoleType.FORENSIC_SCIENTIST) {
            visibleRole = VisibleRole.FORENSIC_SCIENTIST;
        }
        else if (targetPlayer.getPlayerId().equals(myPlayerId)) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        else if (myRole == RoleType.FORENSIC_SCIENTIST) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        else if ((myRole == RoleType.MURDERER || myRole == RoleType.ACCOMPLICE) &&
                (targetPlayer.getRole() == RoleType.MURDERER || targetPlayer.getRole() == RoleType.ACCOMPLICE)) {
            visibleRole = VisibleRole.valueOf(targetPlayer.getRole().name());
        }
        else if (myRole == RoleType.WITNESS &&
                (targetPlayer.getRole() == RoleType.MURDERER || targetPlayer.getRole() == RoleType.ACCOMPLICE)) {
            visibleRole = VisibleRole.SUSPECT;
        }
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