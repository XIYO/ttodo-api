package point.ttodoApi.common.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Modern Java 21 Test Example
 * Demonstrates slice testing principles with modern Java features
 */
@DisplayName("Modern Java 21 Test Features Example")
class ModernTestExample {

    record TestData(String input, String expected, String description) {}

    @Test
    @DisplayName("Text blocks for multi-line test data")
    void textBlockExample() {
        String jsonPayload = """
            {
                "email": "test@example.com",
                "password": "Password123!",
                "nickname": "테스트유저"
            }
            """;
        
        assertThat(jsonPayload).contains("test@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user@test.dev", "admin@ttodo.com"})
    @DisplayName("Parameterized test for email validation")
    void emailValidationTest(String email) {
        // Using modern switch expression for validation
        boolean isValid = switch (email) {
            case String e when e.contains("@") && e.contains(".") -> true;
            default -> false;
        };
        
        assertThat(isValid).isTrue();
    }

    @TestFactory
    @DisplayName("Dynamic tests for date ranges")
    Stream<DynamicTest> dateRangeTests() {
        List<TestData> testCases = List.of(
            new TestData("2025-01-01", "2025-01-01", "Same day"),
            new TestData("2025-01-01", "2025-01-07", "Week range"),
            new TestData("2025-01-01", "2025-01-31", "Month range")
        );

        return testCases.stream()
            .map(testData -> dynamicTest(
                testData.description(),
                () -> {
                    LocalDate start = LocalDate.parse(testData.input());
                    LocalDate end = LocalDate.parse(testData.expected());
                    assertThat(end).isAfterOrEqualTo(start);
                }
            ));
    }

    record ValidationResult(boolean isValid, String message) {
        static ValidationResult valid() {
            return new ValidationResult(true, "Valid");
        }
        
        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }

    @Test
    @DisplayName("Record-based result objects")
    void recordBasedTest() {
        ValidationResult result = ValidationResult.valid();
        assertThat(result.isValid()).isTrue();
        assertThat(result.message()).isEqualTo("Valid");
    }
}