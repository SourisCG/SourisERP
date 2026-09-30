package com.portfolio.erp.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.erp.domain.exception.AuthenticationFailedException;
import com.portfolio.erp.domain.ports.in.AuthUseCase;
import com.portfolio.erp.domain.ports.in.AuthUseCase.AuthResult;
import com.portfolio.erp.domain.ports.out.CurrentActorPort;
import com.portfolio.erp.infrastructure.in.web.api.AuthApi;
import com.portfolio.erp.infrastructure.in.web.dto.LoginRequest;
import com.portfolio.erp.infrastructure.in.web.dto.TokenResponse;
import com.portfolio.erp.infrastructure.in.web.dto.UserDetail;
import com.portfolio.erp.infrastructure.out.mapper.UserMapper;

@RestController
public class AuthController implements AuthApi {

    private final AuthUseCase authUseCase;
    private final CurrentActorPort currentActor;
    private final UserMapper userMapper;

    public AuthController(AuthUseCase authUseCase, CurrentActorPort currentActor, UserMapper userMapper) {
        this.authUseCase = authUseCase;
        this.currentActor = currentActor;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<TokenResponse> login(LoginRequest loginRequest) {
        AuthResult result = authUseCase.login(loginRequest.getUsername(), loginRequest.getPassword());

        TokenResponse response = new TokenResponse();
        response.setAccessToken(result.accessToken());
        response.setTokenType("Bearer");
        response.setExpiresIn(Math.toIntExact(result.expiresInSeconds()));
        response.setUser(userMapper.toSummary(result.user()));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserDetail> getCurrentUser() {
        Long actorId = currentActor.currentActorId()
                .orElseThrow(() -> new AuthenticationFailedException("error.unauthorized"));
        return ResponseEntity.ok(userMapper.toDetail(authUseCase.currentUser(actorId)));
    }
}
