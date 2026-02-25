package model;

import constant.RoleType;
import lombok.Data;
import model.cards.ClueCard;
import model.cards.MeansCard;

import java.util.List;

@Data
public class PlayerInGame {
    private String playerId; // ID của user (link với DB nếu cần)
    private String playerName;

    private RoleType role;

    // Bài trên tay (FS sẽ có list rỗng)
    private List<ClueCard> clueCards;
    private List<MeansCard> meansCards;

    // Mỗi người (trừ FS) có 1 huy hiệu để phá án. Dùng rồi là mất.
    private boolean hasBadge = true;

    // Cờ đánh dấu đã trình bày xong trong vòng hiện tại chưa
    private boolean hasPresented = false;
}