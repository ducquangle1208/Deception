package model.abstract_model;

import constant.CardType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Card {

    @EqualsAndHashCode.Include // Dùng duy nhất ID để định danh và so sánh 2 thẻ
    protected String id;

    protected String name;

    // Tùy chọn thêm để hỗ trợ Frontend hiển thị
    protected String imageUrl;

    // Tùy chọn: Dùng cho đa ngôn ngữ (i18n) nếu bạn muốn game chơi được nhiều thứ tiếng
    // protected String translationKey;

    // Sử dụng Enum thay vì String
    public abstract CardType getType();

    @Override
    public String toString() {
        return getType().name() + ": " + name;
    }
}