package com.smartrecruit.identity.api;

import com.smartrecruit.identity.domain.UserAccount;
import com.smartrecruit.identity.repo.UserRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import io.minio.http.Method;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
public class IdentityController {
    private final UserRepository repo;
    private final PasswordEncoder passwords;
    private final AuthenticationManager auth;
    private final JwtEncoder jwt;
    private final MinioClient minio;
    @Value("${identity.jwt.issuer}") private String issuer;
    @Value("${minio.bucket:smartrecruit-documents}") private String bucket;

    public IdentityController(UserRepository repo, PasswordEncoder passwords, AuthenticationManager auth,
                              JwtEncoder jwt, MinioClient minio) {
        this.repo = repo; this.passwords = passwords; this.auth = auth; this.jwt = jwt; this.minio = minio;
    }

    public record Register(@Email @NotBlank String email, @NotBlank @Size(min=12,max=72) String password,
                           @NotBlank String fullName, String phone, String address, String city, String country) {}
    public record Login(@Email @NotBlank String email, @NotBlank String password) {}
    public record Token(String accessToken, String tokenType, long expiresIn) {}
    public record UserView(UUID id, String email, String fullName, String phone, String address, String city,
                           String country, UserAccount.Role role, UserAccount.Status status) {
        static UserView from(UserAccount u) { return new UserView(u.id,u.email,u.fullName,u.phone,u.address,u.city,u.country,u.role,u.status); }
    }
    public record ProfileUpdate(@NotBlank String fullName, String phone, String address, String city, String country) {}
    public record AdminUserUpdate(UserAccount.Status status, UserAccount.Role role, String fullName) {}
    public record EmploymentProofLink(UUID userId,String url) {}

    @PostMapping("/api/v1/auth/register/candidate") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public UserView candidate(@Valid @RequestBody Register d) { return UserView.from(create(d, UserAccount.Role.CANDIDATE, UserAccount.Status.ACTIVE)); }

    @PostMapping(value="/api/v1/auth/register/recruiter", consumes="multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED) @Transactional
    public UserView recruiter(@RequestParam @Email @NotBlank String email, @RequestParam @NotBlank @Size(min=12,max=72) String password,
                              @RequestParam @NotBlank String fullName, @RequestParam(required=false) String phone,
                              @RequestParam(required=false) String address, @RequestParam(required=false) String city,
                              @RequestParam(required=false) String country, @RequestPart("employmentProof") MultipartFile proof) {
        if (proof.isEmpty()) throw new IllegalArgumentException("Employment proof is required");
        if (proof.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Employment proof must be at most 10 MB");
        UserAccount u = create(new Register(email,password,fullName,phone,address,city,country), UserAccount.Role.RECRUITER, UserAccount.Status.PENDING);
        try {
            if (!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            String key = "employment-proof/" + u.id + "/" + UUID.randomUUID();
            try (InputStream data = proof.getInputStream()) {
                minio.putObject(PutObjectArgs.builder().bucket(bucket).object(key).stream(data,proof.getSize(),-1)
                    .contentType(proof.getContentType()==null ? "application/octet-stream" : proof.getContentType()).build());
            }
            u.employmentProofObjectKey = key;
            return UserView.from(u);
        } catch (Exception e) { throw new IllegalStateException("Could not store employment proof", e); }
    }

    private UserAccount create(Register d, UserAccount.Role role, UserAccount.Status status) {
        if (repo.existsByEmailIgnoreCase(d.email())) throw new IllegalArgumentException("Email already registered");
        UserAccount u = new UserAccount(); u.email=d.email().trim().toLowerCase(); u.passwordHash=passwords.encode(d.password());
        u.fullName=d.fullName(); u.phone=d.phone(); u.address=d.address(); u.city=d.city(); u.country=d.country(); u.role=role; u.status=status;
        return repo.save(u);
    }

    @PostMapping("/api/v1/auth/login") public Token login(@Valid @RequestBody Login d) {
        auth.authenticate(new UsernamePasswordAuthenticationToken(d.email(),d.password()));
        UserAccount u=repo.findByEmailIgnoreCase(d.email()).orElseThrow(); Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer(issuer).subject(u.email).issuedAt(now).expiresAt(now.plusSeconds(3600))
            .claim("user_id",u.id.toString()).claim("roles",List.of(u.role.name())).build();
        String token=jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new Token(token,"Bearer",3600);
    }
    @PostMapping("/api/v1/auth/logout") @PreAuthorize("isAuthenticated()") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() { /* Stateless JWT clients discard the bearer token on logout. */ }

    @GetMapping("/api/v1/users/me") public UserView me(Authentication a) { return UserView.from(current(a)); }
    @PatchMapping("/api/v1/users/me") @Transactional public UserView updateMe(Authentication a,@Valid @RequestBody ProfileUpdate d) {
        UserAccount u=current(a); u.fullName=d.fullName();u.phone=d.phone();u.address=d.address();u.city=d.city();u.country=d.country();return UserView.from(u);
    }
    @GetMapping("/api/v1/recruiters/me") @PreAuthorize("hasRole('RECRUITER')") public UserView recruiterMe(Authentication a) { return UserView.from(current(a)); }
    @PatchMapping("/api/v1/recruiters/me") @PreAuthorize("hasRole('RECRUITER')") @Transactional
    public UserView updateRecruiter(Authentication a,@Valid @RequestBody ProfileUpdate d) { return updateMe(a,d); }

    @GetMapping("/api/v1/admin/users") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") public List<UserView> all() { return repo.findAll().stream().map(UserView::from).toList(); }
    @GetMapping("/api/v1/admin/users/{userId}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
    public UserView adminGet(@PathVariable UUID userId) { return UserView.from(find(userId)); }
    @PatchMapping("/api/v1/admin/users/{userId}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @Transactional
    public UserView adminUpdate(@PathVariable UUID userId,@RequestBody AdminUserUpdate d) {
        UserAccount u=find(userId); if(d.fullName()!=null)u.fullName=d.fullName();if(d.status()!=null)u.status=d.status();if(d.role()!=null)u.role=d.role();return UserView.from(u);
    }
    @PostMapping("/api/v1/admin/recruiters/{userId}/approve") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @Transactional
    public UserView approve(@PathVariable UUID userId) { UserAccount u=findRecruiter(userId);u.status=UserAccount.Status.ACTIVE;return UserView.from(u); }
    @PostMapping("/api/v1/admin/recruiters/{userId}/reject") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @Transactional
    public UserView reject(@PathVariable UUID userId) { UserAccount u=findRecruiter(userId);u.status=UserAccount.Status.REJECTED;return UserView.from(u); }
    @GetMapping("/api/v1/admin/recruiters/{userId}/employment-proof") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
    public EmploymentProofLink employmentProof(@PathVariable UUID userId) {
        UserAccount u=findRecruiter(userId);if(u.employmentProofObjectKey==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Employment proof not found");
        try { return new EmploymentProofLink(u.id,minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucket).object(u.employmentProofObjectKey).expiry(300).build())); }
        catch(Exception e) { throw new IllegalStateException("Could not create employment proof link",e); }
    }

    private UserAccount current(Authentication a) { return repo.findByEmailIgnoreCase(a.getName()).orElseThrow(); }
    private UserAccount find(UUID id) { return repo.findById(id).orElseThrow(); }
    private UserAccount findRecruiter(UUID id) { UserAccount u=find(id);if(u.role!=UserAccount.Role.RECRUITER)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not a recruiter");return u; }
}
