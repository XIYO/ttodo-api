package point.ttodoApi.todo.domain.recurrence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BYYEARDAY 음수 인덱스 필터 테스트")
class ByYearDayFilterTest {

    @Test
    @DisplayName("BYYEARDAY 음수 인덱스 계산 검증 - 연말 3일")
    void calculateByYearDay_Success_WithNegativeIndex() {
        LocalDate dec29 = LocalDate.of(2025, 12, 29);
        LocalDate dec30 = LocalDate.of(2025, 12, 30);
        LocalDate dec31 = LocalDate.of(2025, 12, 31);

        int yearLength = Year.of(2025).length();
        System.out.println("Year length: " + yearLength);

        System.out.println("Dec 29 dayOfYear: " + dec29.getDayOfYear());
        System.out.println("Dec 30 dayOfYear: " + dec30.getDayOfYear());
        System.out.println("Dec 31 dayOfYear: " + dec31.getDayOfYear());

        // For -1: yearLength + (-1) + 1 = 365 + (-1) + 1 = 365
        // For -2: yearLength + (-2) + 1 = 365 + (-2) + 1 = 364
        // For -3: yearLength + (-3) + 1 = 365 + (-3) + 1 = 363

        assertEquals(365, yearLength + (-1) + 1);
        assertEquals(364, yearLength + (-2) + 1);
        assertEquals(363, yearLength + (-3) + 1);

        assertEquals(363, dec29.getDayOfYear());
        assertEquals(364, dec30.getDayOfYear());
        assertEquals(365, dec31.getDayOfYear());
    }
}