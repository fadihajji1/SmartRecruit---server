package com.smartrecruit.gateway;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
@Configuration
public class SecurityConfig {
 @Bean ReactiveJwtDecoder jwtDecoder(@Value("${identity.jwt.secret}") String secret,@Value("${identity.jwt.issuer}") String issuer) {
   var key=new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
   var decoder=NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
   decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer)); return decoder;
 }
 @Bean SecurityWebFilterChain chain(ServerHttpSecurity http) {
   return http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(ex -> ex.pathMatchers("/api/v1/auth/**","/api/v1/jobs/**","/actuator/health").permitAll().anyExchange().authenticated()).oauth2ResourceServer(o -> o.jwt(jwt -> {})).build();
 }
}
