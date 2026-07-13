package com.ssd.agenda_SSD_back.controller;
import com.ssd.agenda_SSD_back.dto.ChangePasswordDto;
import com.ssd.agenda_SSD_back.dto.LoginRequestDto;
import com.ssd.agenda_SSD_back.dto.LoginResponseDto;
import com.ssd.agenda_SSD_back.dto.SelfProfileUpdateDto;
import com.ssd.agenda_SSD_back.dto.UserCreatedDto;
import com.ssd.agenda_SSD_back.dto.UserDto;
import com.ssd.agenda_SSD_back.dto.UserResponseDto;
import com.ssd.agenda_SSD_back.dto.UserUpdateDto;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.security.JwtService;
import com.ssd.agenda_SSD_back.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Users", description = "Gerenciamento de usuários")
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    final private UserService userService;

    @Autowired
    final private JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // Cadastro deixou de ser público: só quem já está logado como ADMIN pode
    // criar novos usuários — quem tem acesso à agenda é escolhido pelo admin,
    // não é autoatendimento. "hasRole('ADMIN')" casa com a authority
    // "ROLE_ADMIN" que o CustomUserDetailsService monta a partir do token.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar um novo usuário", description = "Cria um novo usuário com senha temporária gerada pelo sistema (somente ADMIN)")
    public ResponseEntity<UserCreatedDto> createUser(@RequestBody UserDto userDto){
        User userToSave = UserDto.toEntity(userDto);
        UserCreatedDto createdUser = userService.createUser(userToSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // Lista/busca/edição de usuário — base da tela de gestão de acessos do
    // admin (Monitoring). Tudo restrito a ADMIN, igual o cadastro.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários cadastrados (somente ADMIN)")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.findAllUsers().stream()
                .map(UserResponseDto::userResponseDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuários", description = "Busca usuários por nome ou email (somente ADMIN)")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String q) {
        List<UserResponseDto> users = userService.searchUsers(q).stream()
                .map(UserResponseDto::userResponseDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    // Autoatendimento: qualquer usuário logado mexe na própria conta — não
    // precisa de @PreAuthorize de role, só estar autenticado (já garantido
    // pelo "anyRequest().authenticated()" do SecurityConfig). "/me" é
    // resolvido pelo token (Authentication), nunca por ID vindo do cliente.
    @PutMapping("/me")
    @Operation(summary = "Atualizar meu perfil", description = "Atualiza nome/email da própria conta (exige a senha atual)")
    public ResponseEntity<UserResponseDto> updateProfile(
            @RequestBody SelfProfileUpdateDto selfProfileUpdateDto,
            Authentication authentication
    ) {
        User updatedUser = userService.updateProfile(authentication.getName(), selfProfileUpdateDto);
        return ResponseEntity.ok(UserResponseDto.userResponseDto(updatedUser));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Trocar minha senha", description = "Troca a senha da própria conta (exige a senha atual)")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordDto changePasswordDto,
            Authentication authentication
    ) {
        userService.changePassword(authentication.getName(), changePasswordDto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar usuário", description = "Atualiza dados e role de um usuário (somente ADMIN)")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDto userUpdateDto,
            Authentication authentication
    ) {
        User updatedUser = userService.updateUser(id, userUpdateDto, authentication.getName());
        return ResponseEntity.ok(UserResponseDto.userResponseDto(updatedUser));
    }

    // "Apagar" usuário = desativar (ver UserService.deactivateUser) — preserva
    // reuniões e logs já existentes, e é reversível via /reactivate.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar usuário", description = "Desativa o acesso de um usuário, sem apagar histórico (somente ADMIN)")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id, Authentication authentication) {
        userService.deactivateUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reativar usuário", description = "Reativa o acesso de um usuário previamente desativado (somente ADMIN)")
    public ResponseEntity<UserResponseDto> reactivateUser(@PathVariable Long id) {
        userService.reactivateUser(id);
        return ResponseEntity.ok(UserResponseDto.userResponseDto(userService.findUserById(id)));
    }

    // Mesma senha temporária + mustChangePassword do cadastro, agora pra
    // quem já existe (esqueceu a senha, por exemplo). Não dá pra usar em si
    // mesmo — ver o guard em UserService.resetPassword.
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resetar senha", description = "Gera uma nova senha temporária pro usuário trocar no próximo login (somente ADMIN)")
    public ResponseEntity<UserCreatedDto> resetPassword(@PathVariable Long id, Authentication authentication) {
        UserCreatedDto result = userService.resetPassword(id, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica usuário pelo email e senha e retorna um token JWT")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            User loggedUser = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

            // Token que o front deve reenviar (Authorization: Bearer <token>)
            // em toda chamada autenticada a partir daqui.
            String token = jwtService.generateToken(loggedUser.getEmail(), loggedUser.getRole().name());

            return ResponseEntity.ok(LoginResponseDto.fromEntity(loggedUser, token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
