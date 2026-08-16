package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.LoginRequest;
import com.nutriguideai.dto.request.LogoutRequest;
import com.nutriguideai.dto.request.RefreshTokenRequest;
import com.nutriguideai.dto.response.LoginResponse;
import com.nutriguideai.dto.response.RefreshTokenResponse;
import com.nutriguideai.entity.RefreshToken;
import com.nutriguideai.entity.User;
import com.nutriguideai.enums.Role;
import com.nutriguideai.exception.AccountLockedException;
import com.nutriguideai.exception.InvalidCredentialsException;
import com.nutriguideai.exception.RateLimitExceededException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.RefreshTokenRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.security.JwtTokenProvider;
import com.nutriguideai.security.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceImplTest {

    private static final String EMAIL = "alice@nutriguide.com";
    private static final String PASSWORD = "Password@123";

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenRepository refreshTokenRepository;
    private LoginRateLimiter loginRateLimiter;

    private AuthServiceImpl authService;

    private TestUserRepository userStore;
    private TestRefreshTokenRepository refreshTokenStore;
    private TestPasswordEncoder passwordEncoderStore;
    private TestJwtTokenProvider jwtTokenProviderStore;

    @BeforeEach
    void setUp() {

        /*
         * No Mockito.
         *
         * These are small in-memory test doubles implemented with
         * standard Java dynamic proxies.
         */

        userStore = new TestUserRepository();
        refreshTokenStore = new TestRefreshTokenRepository();
        passwordEncoderStore = new TestPasswordEncoder();
        jwtTokenProviderStore = new TestJwtTokenProvider();

        userRepository = userStore.proxy();
        passwordEncoder = passwordEncoderStore.proxy();
        jwtTokenProvider = jwtTokenProviderStore.proxy();

        refreshTokenRepository = refreshTokenStore.proxy();

        /*
         * Use the real LoginRateLimiter.
         */
        loginRateLimiter = new LoginRateLimiter();

        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenRepository,
                loginRateLimiter
        );

        /*
         * AuthServiceImpl gets this value from application configuration
         * in the real application. For the unit test we provide the value
         * directly.
         *
         * 604800000 ms = 7 days.
         */
        ReflectionTestUtils.setField(
                authService,
                "refreshExpirationMs",
                604_800_000L
        );
    }

    // ==================================================================
    // LOGIN
    // ==================================================================

    @Test
    void login_success_returnsAccessAndRefreshTokens() {

        User user = user();

        userStore.setUser(user);
        passwordEncoderStore.setPasswordMatches(true);
        jwtTokenProviderStore.setAccessToken("access-token");

        LoginResponse response =
                authService.login(loginRequest());

        assertThat(response.getToken())
                .isEqualTo("access-token");

        assertThat(response.getRefreshToken())
                .isNotBlank();

        assertThat(response.getUser())
                .isNotNull();

        assertThat(response.getUser().getEmail())
                .isEqualTo(EMAIL);

        assertThat(refreshTokenStore.getSaveCount())
                .isEqualTo(1);

        RefreshToken savedToken =
                refreshTokenStore.getLastSavedToken();

        assertThat(savedToken)
                .isNotNull();

        assertThat(savedToken.getUser())
                .isSameAs(user);

        assertThat(savedToken.getTokenHash())
                .isNotBlank();

        assertThat(savedToken.getExpiresAt())
                .isAfter(LocalDateTime.now());

        assertThat(savedToken.getRevoked())
                .isFalse();
    }

    @Test
    void login_wrongPassword_incrementsFailedAttempts() {

        User user = user();

        userStore.setUser(user);
        passwordEncoderStore.setPasswordMatches(false);

        assertThatThrownBy(() ->
                authService.login(loginRequest())
        )
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getFailedLoginAttempts())
                .isEqualTo(1);

        assertThat(user.getLockedUntil())
                .isNull();

        assertThat(userStore.getSaveCount())
                .isEqualTo(1);

        assertThat(refreshTokenStore.getSaveCount())
                .isZero();
    }

    @Test
    void login_fifthFailure_locksAccount() {

        User user = user();

        user.setFailedLoginAttempts(4);

        userStore.setUser(user);
        passwordEncoderStore.setPasswordMatches(false);

        assertThatThrownBy(() ->
                authService.login(loginRequest())
        )
                .isInstanceOf(AccountLockedException.class);

        assertThat(user.getLockedUntil())
                .isNotNull();

        assertThat(user.getLockedUntil())
                .isAfter(LocalDateTime.now());

        /*
         * AuthServiceImpl resets the counter to zero when the
         * fifth failed attempt locks the account.
         */
        assertThat(user.getFailedLoginAttempts())
                .isZero();

        assertThat(userStore.getSaveCount())
                .isEqualTo(1);

        assertThat(refreshTokenStore.getSaveCount())
                .isZero();
    }

    @Test
    void login_whenLocked_doesNotVerifyPassword() {

        User user = user();

        user.setLockedUntil(
                LocalDateTime.now().plusMinutes(10)
        );

        userStore.setUser(user);

        assertThatThrownBy(() ->
                authService.login(loginRequest())
        )
                .isInstanceOf(AccountLockedException.class);

        /*
         * PasswordEncoder must never be called when the account
         * is already locked.
         */
        assertThat(passwordEncoderStore.getMatchesCallCount())
                .isZero();
    }

    @Test
    void login_rateLimitExceeded_throws429() {

        /*
         * LoginRateLimiter allows 10 attempts per minute.
         *
         * We deliberately use the real LoginRateLimiter.
         *
         * The user does not exist, so each of the first 10 attempts
         * reaches the authentication logic and fails normally.
         *
         * The 11th attempt is rejected by the rate limiter BEFORE
         * UserRepository.findByEmail() is called.
         */

        userStore.setUser(null);
        passwordEncoderStore.setPasswordMatches(false);

        for (int i = 0; i < 10; i++) {

            assertThatThrownBy(() ->
                    authService.login(loginRequest())
            )
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        assertThatThrownBy(() ->
                authService.login(loginRequest())
        )
                .isInstanceOf(RateLimitExceededException.class);

        /*
         * Exactly the first 10 requests reach the repository.
         * The 11th request is blocked before the repository lookup.
         */
        assertThat(userStore.getFindByEmailCallCount())
                .isEqualTo(10);
    }

    // ==================================================================
    // REFRESH TOKEN
    // ==================================================================

    @Test
    void refresh_rotatesTokenPair() {

        User user = user();

        RefreshToken stored =
                RefreshToken.builder()
                        .id(1L)
                        .user(user)
                        .tokenHash("some-hash")
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusDays(7)
                        )
                        .revoked(false)
                        .build();

        refreshTokenStore.setToken(stored);
        jwtTokenProviderStore.setAccessToken("new-access");

        RefreshTokenResponse response =
                authService.refresh(
                        RefreshTokenRequest.builder()
                                .refreshToken("raw-token")
                                .build()
                );

        assertThat(response.getToken())
                .isEqualTo("new-access");

        assertThat(response.getRefreshToken())
                .isNotBlank();

        /*
         * Original refresh token must be revoked.
         */
        assertThat(stored.getRevoked())
                .isTrue();

        /*
         * A new refresh token must have been saved.
         */
        assertThat(refreshTokenStore.getSaveCount())
                .isEqualTo(1);

        RefreshToken newToken =
                refreshTokenStore.getLastSavedToken();

        assertThat(newToken)
                .isNotNull();

        assertThat(newToken.getUser())
                .isSameAs(user);

        assertThat(newToken.getTokenHash())
                .isNotBlank();

        assertThat(newToken.getExpiresAt())
                .isAfter(LocalDateTime.now());

        assertThat(newToken.getRevoked())
                .isFalse();
    }

    @Test
    void refresh_withUnknownToken_throwsUnauthorized() {

        refreshTokenStore.setToken(null);

        assertThatThrownBy(() ->
                authService.refresh(
                        RefreshTokenRequest.builder()
                                .refreshToken("unknown")
                                .build()
                )
        )
                .isInstanceOf(UnauthorizedException.class);

        assertThat(refreshTokenStore.getSaveCount())
                .isZero();
    }

    @Test
    void refresh_withRevokedToken_throwsUnauthorized() {

        RefreshToken stored =
                RefreshToken.builder()
                        .id(2L)
                        .user(user())
                        .tokenHash("hash")
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusDays(7)
                        )
                        .revoked(true)
                        .build();

        refreshTokenStore.setToken(stored);

        assertThatThrownBy(() ->
                authService.refresh(
                        RefreshTokenRequest.builder()
                                .refreshToken("raw")
                                .build()
                )
        )
                .isInstanceOf(UnauthorizedException.class);

        assertThat(refreshTokenStore.getSaveCount())
                .isZero();
    }

    @Test
    void refresh_withExpiredToken_throwsUnauthorized() {

        RefreshToken stored =
                RefreshToken.builder()
                        .id(3L)
                        .user(user())
                        .tokenHash("hash")
                        .expiresAt(
                                LocalDateTime.now()
                                        .minusMinutes(1)
                        )
                        .revoked(false)
                        .build();

        refreshTokenStore.setToken(stored);

        assertThatThrownBy(() ->
                authService.refresh(
                        RefreshTokenRequest.builder()
                                .refreshToken("raw")
                                .build()
                )
        )
                .isInstanceOf(UnauthorizedException.class);

        assertThat(refreshTokenStore.getSaveCount())
                .isZero();
    }

    // ==================================================================
    // LOGOUT
    // ==================================================================

    @Test
    void logout_revokesToken() {

        RefreshToken stored =
                RefreshToken.builder()
                        .id(7L)
                        .user(user())
                        .tokenHash("hash")
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusDays(7)
                        )
                        .revoked(false)
                        .build();

        refreshTokenStore.setToken(stored);

        authService.logout(
                LogoutRequest.builder()
                        .refreshToken("raw")
                        .build()
        );

        assertThat(stored.getRevoked())
                .isTrue();

        /*
         * Your current AuthServiceImpl directly changes the entity
         * during logout and does not explicitly call save().
         */
        assertThat(refreshTokenStore.getSaveCount())
                .isZero();
    }

    // ==================================================================
    // TEST DATA
    // ==================================================================

    private LoginRequest loginRequest() {

        return LoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();
    }

    private User user() {

        return User.builder()
                .id(1L)
                .email(EMAIL)
                .password("$2a$10$encoded")
                .firstName("Alice")
                .lastName("Ng")
                .role(Role.USER)
                .build();
    }

    // ==================================================================
    // PLAIN JAVA USER REPOSITORY TEST DOUBLE
    // ==================================================================

    private static final class TestUserRepository
            implements InvocationHandler {

        private User user;

        private final AtomicInteger findByEmailCallCount =
                new AtomicInteger();

        private final AtomicInteger saveCount =
                new AtomicInteger();

        private final Map<Long, User> users =
                new HashMap<>();

        UserRepository proxy() {

            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    this
            );
        }

        void setUser(User user) {

            this.user = user;

            users.clear();

            if (user != null && user.getId() != null) {
                users.put(user.getId(), user);
            }
        }

        int getFindByEmailCallCount() {
            return findByEmailCallCount.get();
        }

        int getSaveCount() {
            return saveCount.get();
        }

        @Override
        public Object invoke(
                Object proxy,
                Method method,
                Object[] args
        ) {

            String methodName = method.getName();

            switch (methodName) {

                case "findByEmail":
                    findByEmailCallCount.incrementAndGet();

                    return Optional.ofNullable(user);

                case "existsByEmail":

                    if (user == null || args == null || args.length == 0) {
                        return false;
                    }

                    return user.getEmail()
                            .equalsIgnoreCase(String.valueOf(args[0]));

                case "save":
                    saveCount.incrementAndGet();

                    User entity = (User) args[0];

                    if (entity.getId() != null) {
                        users.put(entity.getId(), entity);
                    }

                    user = entity;

                    return entity;

                case "toString":
                    return "TestUserRepository";

                case "hashCode":
                    return System.identityHashCode(proxy);

                case "equals":
                    return proxy == args[0];

                default:
                    throw new UnsupportedOperationException(
                            "TestUserRepository method not implemented: "
                                    + methodName
                    );
            }
        }
    }

    // ==================================================================
    // PLAIN JAVA REFRESH TOKEN REPOSITORY TEST DOUBLE
    // ==================================================================

    private static final class TestRefreshTokenRepository
            implements InvocationHandler {

        private RefreshToken token;

        private final AtomicInteger saveCount =
                new AtomicInteger();

        private RefreshToken lastSavedToken;

        RefreshTokenRepository proxy() {

            return (RefreshTokenRepository) Proxy.newProxyInstance(
                    RefreshTokenRepository.class.getClassLoader(),
                    new Class<?>[]{RefreshTokenRepository.class},
                    this
            );
        }

        void setToken(RefreshToken token) {
            this.token = token;
            this.lastSavedToken = null;
            this.saveCount.set(0);
        }

        int getSaveCount() {
            return saveCount.get();
        }

        RefreshToken getLastSavedToken() {
            return lastSavedToken;
        }

        @Override
        public Object invoke(
                Object proxy,
                Method method,
                Object[] args
        ) {

            String methodName = method.getName();

            switch (methodName) {

                case "findByTokenHash":
                    return Optional.ofNullable(token);

                case "save":

                    saveCount.incrementAndGet();

                    RefreshToken entity =
                            (RefreshToken) args[0];

                    lastSavedToken = entity;

                    return entity;

                case "deleteByExpiresAtBefore":
                    return 0L;

                case "toString":
                    return "TestRefreshTokenRepository";

                case "hashCode":
                    return System.identityHashCode(proxy);

                case "equals":
                    return proxy == args[0];

                default:
                    throw new UnsupportedOperationException(
                            "TestRefreshTokenRepository method not implemented: "
                                    + methodName
                    );
            }
        }
    }

    // ==================================================================
    // PLAIN JAVA PASSWORD ENCODER TEST DOUBLE
    // ==================================================================

    private static final class TestPasswordEncoder
            implements InvocationHandler {

        private final AtomicBoolean passwordMatches =
                new AtomicBoolean(false);

        private final AtomicInteger matchesCallCount =
                new AtomicInteger();

        PasswordEncoder proxy() {

            return (PasswordEncoder) Proxy.newProxyInstance(
                    PasswordEncoder.class.getClassLoader(),
                    new Class<?>[]{PasswordEncoder.class},
                    this
            );
        }

        void setPasswordMatches(boolean matches) {
            passwordMatches.set(matches);
        }

        int getMatchesCallCount() {
            return matchesCallCount.get();
        }

        @Override
        public Object invoke(
                Object proxy,
                Method method,
                Object[] args
        ) {

            String methodName = method.getName();

            switch (methodName) {

                case "matches":

                    matchesCallCount.incrementAndGet();

                    return passwordMatches.get();

                case "encode":

                    return "$2a$10$testEncodedPasswordHash";

                case "upgradeEncoding":

                    return false;

                case "toString":
                    return "TestPasswordEncoder";

                case "hashCode":
                    return System.identityHashCode(proxy);

                case "equals":
                    return proxy == args[0];

                default:
                    throw new UnsupportedOperationException(
                            "TestPasswordEncoder method not implemented: "
                                    + methodName
                    );
            }
        }
    }

    // ==================================================================
    // PLAIN JAVA JWT PROVIDER TEST DOUBLE
    // ==================================================================

    private static final class TestJwtTokenProvider
            implements InvocationHandler {

        private String accessToken = "test-access-token";

        JwtTokenProvider proxy() {

            return (JwtTokenProvider) Proxy.newProxyInstance(
                    JwtTokenProvider.class.getClassLoader(),
                    new Class<?>[]{JwtTokenProvider.class},
                    this
            );
        }

        void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public Object invoke(
                Object proxy,
                Method method,
                Object[] args
        ) {

            String methodName = method.getName();

            switch (methodName) {

                case "generateToken":
                    return accessToken;

                case "getEmailFromToken":
                    return EMAIL;

                case "isValidToken":
                    return true;

                case "toString":
                    return "TestJwtTokenProvider";

                case "hashCode":
                    return System.identityHashCode(proxy);

                case "equals":
                    return proxy == args[0];

                default:
                    throw new UnsupportedOperationException(
                            "TestJwtTokenProvider method not implemented: "
                                    + methodName
                    );
            }
        }
    }
}