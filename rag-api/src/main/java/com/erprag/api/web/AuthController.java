package com.erprag.api.web;

import com.erprag.api.security.JwtService;
import com.erprag.api.web.dto.LoginRequest;
import com.erprag.api.web.dto.LoginResponse;
import com.erprag.core.RagService;
import com.erprag.core.domain.KnowledgeBaseUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RagService ragService;
    private final JwtService jwtService;

    public AuthController(RagService ragService, JwtService jwtService) {
        this.ragService = ragService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        KnowledgeBaseUser user = ragService.authenticate(request.entityCode(), request.username(), request.password());
        String token = jwtService.issue(user.username(), request.entityCode(), user.role());
        return new LoginResponse(token, jwtService.expirySeconds());
    }
}
