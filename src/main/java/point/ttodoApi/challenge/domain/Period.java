package point.ttodoApi.challenge.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

/**
 * 챌린지 기간을 나타내는 값 객체
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Period {
    private LocalDate startDate;
    private LocalDate endDate;
    
    /**
     * 주어진 날짜가 기간 내에 있는지 확인
     */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * 기간의 일수 계산
     */
    public long getDays() {
        return endDate.toEpochDay() - startDate.toEpochDay() + 1;
    }
}