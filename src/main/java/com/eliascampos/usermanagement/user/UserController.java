package com.eliascampos.usermanagement.user;

import com.eliascampos.usermanagement.user.dto.PageResponse;
import com.eliascampos.usermanagement.user.dto.UserRequest;
import com.eliascampos.usermanagement.user.dto.UserResponse;
import com.eliascampos.usermanagement.user.dto.UserStatusRequest;
import com.eliascampos.usermanagement.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Usuarios", description = "Operacoes de gerenciamento de usuarios")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Cria um novo usuario")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Busca um usuario pelo id")
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable String id) {
        return userService.findById(id);
    }

    @Operation(summary = "Lista usuarios com paginacao, busca e filtros")
    @GetMapping
    public PageResponse<UserResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.findAll(search, role, active, pageable);
    }

    @Operation(summary = "Atualiza os dados de um usuario")
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @Operation(summary = "Ativa ou desativa um usuario")
    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(@PathVariable String id, @Valid @RequestBody UserStatusRequest request) {
        return userService.changeStatus(id, request);
    }

    @Operation(summary = "Remove um usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
