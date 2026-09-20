package com.erprag.api.web;

import com.erprag.core.RagService;
import com.erprag.core.domain.KnowledgeBaseUser;
import com.erprag.api.web.dto.CreateEntityRequest;
import com.erprag.api.web.dto.CreateUserRequest;
import com.erprag.api.web.dto.EntityResponse;
import com.erprag.api.web.dto.UpdateEntityRequest;
import com.erprag.api.web.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/entities")
public class AdminEntityController {

    private final RagService ragService;

    public AdminEntityController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityResponse create(@Valid @RequestBody CreateEntityRequest request) {
        return EntityResponse.from(ragService.createEntity(request.entityCode(), request.name()));
    }

    @GetMapping
    public List<EntityResponse> list() {
        return ragService.listEntities().stream().map(EntityResponse::from).toList();
    }

    @PutMapping("/{entityCode}")
    public EntityResponse update(@PathVariable("entityCode") String entityCode, @Valid @RequestBody UpdateEntityRequest request) {
        return EntityResponse.from(ragService.updateEntity(entityCode, request.name()));
    }

    @DeleteMapping("/{entityCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("entityCode") String entityCode) {
        ragService.deleteEntity(entityCode);
    }

    @PostMapping("/{entityCode}/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@PathVariable("entityCode") String entityCode, @Valid @RequestBody CreateUserRequest request) {
        KnowledgeBaseUser user = ragService.createUser(entityCode, request.username(), request.password());
        return UserResponse.from(user);
    }
}
