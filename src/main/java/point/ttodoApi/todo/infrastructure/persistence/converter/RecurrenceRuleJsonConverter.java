package point.ttodoApi.todo.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import point.ttodoApi.todo.domain.recurrence.RecurrenceRule;

@Converter(autoApply = false)
public class RecurrenceRuleJsonConverter implements AttributeConverter<RecurrenceRule, String> {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(RecurrenceRule attribute) {
        if (attribute == null) return null;
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize RecurrenceRule to JSON", e);
        }
    }

    @Override
    public RecurrenceRule convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return mapper.readValue(dbData, RecurrenceRule.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize RecurrenceRule from JSON", e);
        }
    }
}

