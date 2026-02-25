package deception.constant;

public enum VisibleRole {
    FORENSIC_SCIENTIST,
    MURDERER,
    ACCOMPLICE,
    WITNESS,
    INVESTIGATOR,
    UNKNOWN, // Dùng để che giấu role của người khác
    SUSPECT  // Dành riêng cho Witness: Biết 2 người này là phe ác, nhưng không biết ai là Sát nhân, ai là Tòng phạm
}