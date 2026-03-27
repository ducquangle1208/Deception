package deception.service;

import deception.gameplay.GameSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GameFlowScheduler {

    private final GameService gameService;
    private final GameNotificationService notificationService;

    public GameFlowScheduler(GameService gameService, GameNotificationService notificationService) {
        this.gameService = gameService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 500)
    public void advancePresentationFlow() {
        GameSession updatedSession = gameService.advanceAutomaticFlow();
        if (updatedSession != null) {
            notificationService.broadcastGameState(updatedSession);
        }
    }
}
