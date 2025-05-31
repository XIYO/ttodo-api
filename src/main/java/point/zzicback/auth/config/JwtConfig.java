package point.zzicback.auth.config;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.util.StreamUtils;
import point.zzicback.auth.config.properties.JwtProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
private final JwtProperties jwtProperties;
private RSAPrivateKey privateKey;
private RSAPublicKey publicKey;

@PostConstruct
public void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
  this.privateKey = loadPrivateKey();
  this.publicKey = loadPublicKey();
}

private String readKey(Resource resource) throws IOException {
  try (InputStream inputStream = resource.getInputStream()) {
    return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).replaceAll("-----.*?-----", "")
            .replaceAll("\\s+", "");
  }
}

private RSAPrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
  byte[] keyBytes = Base64.getDecoder().decode(readKey(jwtProperties.privateKey()));
  PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
  return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
}

private RSAPublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
  byte[] keyBytes = Base64.getDecoder().decode(readKey(jwtProperties.publicKey()));
  X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
  return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
}

@Bean
public RSAPublicKey publicKey() {
  return publicKey;
}

@Bean
public JwtEncoder jwtEncoder() {
  RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(jwtProperties.keyId()).build();
  JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
  return new NimbusJwtEncoder(jwkSource);
}

@Bean
public JwtDecoder jwtDecoder() {
  return NimbusJwtDecoder.withPublicKey(publicKey).build();
}

@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
  JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
  grantedAuthoritiesConverter.setAuthorityPrefix("");
  JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
  jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
    Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
    if (authorities.isEmpty()) {
      authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    return authorities;
  });
  return jwtConverter;
}
}
