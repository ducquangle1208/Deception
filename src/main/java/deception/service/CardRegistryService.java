package deception.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import deception.constant.SceneType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import deception.model.cards.ClueCard;
import deception.model.cards.MeansCard;
import deception.model.cards.SceneCard;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CardRegistryService {

    private final ObjectMapper objectMapper;

    // Lưu trữ in-memory
    private List<ClueCard> allClueCards = new ArrayList<>();
    private List<MeansCard> allMeansCards = new ArrayList<>();
    private List<SceneCard> allSceneCards = new ArrayList<>();

    public CardRegistryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("Bắt đầu load dữ liệu thẻ bài vào RAM...");
        try {
            // Đọc file từ thư mục src/main/resources/static/data/
            allClueCards = loadCardsFromFile("/data/clue_cards.json", new TypeReference<>() {});
            allMeansCards = loadCardsFromFile("/data/means_cards.json", new TypeReference<>() {});
            allSceneCards = loadCardsFromFile("/data/scene_cards.json", new TypeReference<>() {});

            log.info("Đã load thành công: {} Clue Cards, {} Means Cards, {} Scene Cards",
                    allClueCards.size(), allMeansCards.size(), allSceneCards.size());
        } catch (Exception e) {
            log.error("Lỗi khi load dữ liệu thẻ bài: ", e);
            throw new RuntimeException("Không thể khởi tạo CardRegistryService");
        }
    }

    // Hàm helper đọc file JSON
    private <T> List<T> loadCardsFromFile(String filePath, TypeReference<List<T>> typeReference) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Không tìm thấy file: " + filePath);
            }
            return objectMapper.readValue(is, typeReference);
        }
    }

    // --- CÁC HÀM GETTER ĐỂ GAMESERVICE SỬ DỤNG ---

    public List<ClueCard> getAllClueCards() {
        // Trả về bản copy để tránh bị modify list gốc khi xáo bài
        return new ArrayList<>(allClueCards);
    }

    public List<MeansCard> getAllMeansCards() {
        return new ArrayList<>(allMeansCards);
    }

    // Lấy ngẫu nhiên 1 thẻ Scene theo loại (VD: CAUSE_OF_DEATH)
    public SceneCard getRandomSceneCardByType(SceneType type) {
        List<SceneCard> filteredCards = allSceneCards.stream()
                .filter(card -> card.getSceneType() == type)
                .collect(Collectors.toList());

        if (filteredCards.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thẻ Scene nào thuộc loại: " + type);
        }

        Collections.shuffle(filteredCards);
        return filteredCards.get(0);
    }

    // Lấy ngẫu nhiên N thẻ Scene theo loại (Dành cho RANDOM_SCENE)
    public List<SceneCard> getRandomSceneCardsByType(SceneType type, int count) {
        List<SceneCard> filteredCards = allSceneCards.stream()
                .filter(card -> card.getSceneType() == type)
                .collect(Collectors.toList());

        if (filteredCards.size() < count) {
            throw new IllegalStateException("Không đủ thẻ Scene loại " + type + " để bốc!");
        }

        Collections.shuffle(filteredCards);
        return filteredCards.subList(0, count); // Lấy `count` phần tử đầu tiên
    }
}