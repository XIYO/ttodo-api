package point.zzicback.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String timestamp;

    private int status;

    private String error;

    private String path;

    private String message;

    private List<FieldErrorResponse> errors;
}