package point.ttodoApi.todo.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import point.ttodoApi.todo.domain.recurrence.Frequency;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;
import point.ttodoApi.todo.domain.recurrence.WeekDay;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceRuleJsonConverterTest {

    private RecurrenceRuleJsonConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        converter = new RecurrenceRuleJsonConverter();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void testSerializationWithNewFields() {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.HOURLY);
        rule.setInterval(2);
        rule.setByHour(Set.of(9, 14, 18));
        rule.setByMinute(Set.of(0, 30));
        rule.setByWeekNo(Set.of(1, 26, 52));
        rule.setByYearDay(Set.of(1, 100, 365));
        rule.setAnchorDate(LocalDate.of(2025, 1, 1));
        rule.setTimezone("Asia/Seoul");

        String json = converter.convertToDatabaseColumn(rule);
        assertNotNull(json);
        assertTrue(json.contains("HOURLY"));
        assertTrue(json.contains("byHour"));
        assertTrue(json.contains("byMinute"));
        assertTrue(json.contains("byWeekNo"));
        assertTrue(json.contains("byYearDay"));
    }

    @Test
    void testDeserializationWithNewFields() {
        String json = """
            {
                "frequency": "MINUTELY",
                "interval": 15,
                "byHour": [9, 17],
                "byMinute": [0, 15, 30, 45],
                "bySecond": [0, 30],
                "byWeekNo": [1, -1],
                "byYearDay": [100, -100],
                "weekStart": "MO",
                "timezone": "Asia/Seoul",
                "anchorDate": "2025-01-01"
            }
            """;

        RecurrenceRule rule = converter.convertToEntityAttribute(json);
        assertNotNull(rule);
        assertEquals(Frequency.MINUTELY, rule.getFrequency());
        assertEquals(15, rule.getInterval());
        assertEquals(Set.of(9, 17), rule.getByHour());
        assertEquals(Set.of(0, 15, 30, 45), rule.getByMinute());
        assertEquals(Set.of(0, 30), rule.getBySecond());
        assertEquals(Set.of(1, -1), rule.getByWeekNo());
        assertEquals(Set.of(100, -100), rule.getByYearDay());
        assertEquals(LocalDate.of(2025, 1, 1), rule.getAnchorDate());
    }

    @Test
    void testRoundTripSerialization() {
        RecurrenceRule original = new RecurrenceRule();
        original.setFrequency(Frequency.SECONDLY);
        original.setInterval(30);
        original.setByHour(Set.of(8, 12, 16, 20));
        original.setByMinute(Set.of(0, 30));
        original.setBySecond(Set.of(0));
        original.setByWeekNo(Set.of(1, 13, 26, 39, 52));
        original.setByYearDay(Set.of(1, 365));
        original.setWeekStart(WeekDay.MO);
        original.setTimezone("UTC");
        original.setAnchorDate(LocalDate.of(2025, 1, 1));

        String json = converter.convertToDatabaseColumn(original);
        RecurrenceRule deserialized = converter.convertToEntityAttribute(json);

        assertEquals(original.getFrequency(), deserialized.getFrequency());
        assertEquals(original.getInterval(), deserialized.getInterval());
        assertEquals(original.getByHour(), deserialized.getByHour());
        assertEquals(original.getByMinute(), deserialized.getByMinute());
        assertEquals(original.getBySecond(), deserialized.getBySecond());
        assertEquals(original.getByWeekNo(), deserialized.getByWeekNo());
        assertEquals(original.getByYearDay(), deserialized.getByYearDay());
        assertEquals(original.getWeekStart(), deserialized.getWeekStart());
        assertEquals(original.getTimezone(), deserialized.getTimezone());
        assertEquals(original.getAnchorDate(), deserialized.getAnchorDate());
    }
}