package point.zzicback.auth.repository;

public interface TokenRepository {
    
    void save(String key, String value, long expirationSeconds);
    
    String get(String key);
    
    void delete(String key);
    
    boolean exists(String key);
}
