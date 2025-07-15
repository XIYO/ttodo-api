# API Validation Rules Documentation

## Authentication Validation

### Email Validation (`@ValidEmail`)
- **Format**: RFC 5322 compliant email pattern
- **Max local part length**: 64 characters (before @)
- **Max domain part length**: 255 characters (after @)
- **Disposable emails**: 
  - Blocked for registration (sign-up)
  - Allowed for login (sign-in)
- **Blocked disposable domains**: 10minutemail.com, tempmail.com, guerrillamail.com, mailinator.com, yopmail.com, etc.
- **SQL injection protection**: Automatic pattern detection and blocking

### Password Validation (`@SecurePassword`)
- **Minimum length**: 8 characters
- **Maximum length**: 100 characters
- **Required components**:
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one number
  - At least one special character (@$!%*#?&)
- **Prohibited patterns**:
  - No consecutive characters (e.g., "abc", "123", "321")
  - No common weak passwords (e.g., "password123", "admin123", "qwerty123")
- **Size validation**: Additional `@Size(min = 8, max = 100)` annotation

### Username/Nickname Validation (`@ValidUsername`)
- **Minimum length**: 2 characters
- **Maximum length**: 20 characters
- **Allowed characters**:
  - Letters (a-z, A-Z)
  - Numbers (0-9)
  - Korean characters (가-힣)
  - Special characters: dot (.), underscore (_), hyphen (-)
- **Forbidden words**: admin, administrator, root, system, moderator, test, demo, anonymous, etc.
- **SQL injection protection**: Automatic pattern detection and blocking

## Error Response Format

All validation errors follow RFC 7807 Problem Details standard:

```json
{
  "type": "/errors/validation-error",
  "title": "입력값 검증 실패",
  "status": 400,
  "detail": "요청 데이터가 유효하지 않습니다.",
  "instance": "/auth/sign-up",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2024-01-14T10:30:00",
  "extensions": {
    "errors": {
      "email": "Disposable email addresses are not allowed",
      "password": "Password must be at least 8 characters long...",
      "nickname": "Username contains forbidden words"
    }
  }
}
```

## Sign Up Request Validation

```java
public record SignUpRequest(
    @NotBlank
    @ValidEmail(allowDisposable = false)
    @UniqueEmail
    String email,
    
    @NotBlank
    @SecurePassword
    @Size(min = 8, max = 100)
    @CompareTarget
    String password,
    
    @NotBlank
    @CompareResult
    String confirmPassword,
    
    @NotBlank
    @ValidUsername
    @Size(min = 2, max = 20)
    String nickname,
    
    @SanitizeHtml(mode = STRICT)
    String introduction,
    
    String timeZone,
    String locale
)
```

## Sign In Request Validation

```java
public record SignInRequest(
    @NotBlank
    @ValidEmail(allowDisposable = true)  // Login allows disposable emails
    String email,
    
    String password  // No validation on password for login
)
```

## Custom Validation Services

### ForbiddenWordService
- Loads forbidden words from `/resources/forbidden-words.txt`
- Performs case-insensitive matching
- Checks for both exact matches and substring contains

### DisposableEmailService
- Loads disposable domains from `/resources/disposable-email-domains.txt`
- Extracts domain from email and checks against blocklist
- Configurable per endpoint (allowDisposable parameter)

## Testing

Comprehensive test coverage is provided in `AuthControllerValidationTest.java` including:
- Valid input scenarios
- Individual field validation failures
- Multiple field validation failures
- Edge cases (boundary values)
- Forbidden word detection
- Disposable email blocking
- Password strength validation
- Consecutive character detection