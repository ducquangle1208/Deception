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
            allClueCards = loadCardsFromFile("/data/clue_cards.json", new TypeReference<List<ClueCard>>() {});
            allMeansCards = loadCardsFromFile("/data/means_cards.json", new TypeReference<List<MeansCard>>() {});
            allSceneCards = loadCardsFromFile("/data/scene_cards.json", new TypeReference<List<SceneCard>>() {});

            log.info("Đã load thành công: {} Clue Cards, {} Means Cards, {} Scene Cards",
                    allClueCards.size(), allMeansCards.size(), allSceneCards.size());
        } catch (Exception e) {
            log.error("Lỗi khi load dữ liệu thẻ bài: ", e);
            throw new RuntimeException("Không thể khởi tạo CardRegistryService");
        }
    }

    private <T> List<T> loadCardsFromFile(String filePath, TypeReference<List<T>> typeReference) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Không tìm thấy file: " + filePath);
            }
            return objectMapper.readValue(is, typeReference);
        }
    }


    public List<ClueCard> getAllClueCards() {
        return new ArrayList<>(allClueCards);
    }

    public List<MeansCard> getAllMeansCards() {
        return new ArrayList<>(allMeansCards);
    }

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

    public List<SceneCard> getRandomSceneCardsByType(SceneType type, int count) {
        List<SceneCard> filteredCards = allSceneCards.stream()
                .filter(card -> card.getSceneType() == type)
                .collect(Collectors.toList());

        if (filteredCards.size() < count) {
            throw new IllegalStateException("Không đủ thẻ Scene loại " + type + " để bốc!");
        }

        Collections.shuffle(filteredCards);
        return filteredCards.subList(0, count);
    }
}