package gameplay;

import lombok.Data;

import model.cards.SceneCard;

@Data
public class SceneTileHint {

    // Tấm thẻ đang được đặt trên bàn
    private SceneCard sceneCard;

    // Lựa chọn mà Bác sĩ pháp y đã đặt viên đạn lên.
    // Nếu Bác sĩ chưa đặt, giá trị này sẽ là null.
    // Ví dụ Bác sĩ đặt vào "Hospital", chuỗi này sẽ lưu "Hospital"
    private String selectedOption;

    public SceneTileHint(SceneCard sceneCard) {
        this.sceneCard = sceneCard;
        this.selectedOption = null; // Khởi tạo ban đầu chưa có viên đạn
    }
}