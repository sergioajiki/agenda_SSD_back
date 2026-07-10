package com.ssd.agenda_SSD_back.controller;
import com.ssd.agenda_SSD_back.dto.LoginRequestDto;
import com.ssd.agenda_SSD_back.dto.LoginResponseDto;
import com.ssd.agenda_SSD_back.dto.UserDto;
import com.ssd.agenda_SSD_back.dto.UserResponseDto;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.security.JwtService;
import com.ssd.agenda_SSD_back.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @Operation(summary = "Cadastrar um novo usuário", description = "Cria um novo usuário no sistema")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserDto userDto){
        try {
            //Converte o Dto para entity
            User userToSave = UserDto.toEntity(userDto);

            //Salva user
            User newUser = userService.createUser(userToSave);

            //Converte a entidade para o DTO de resposta
            UserResponseDto responseDto = UserResponseDto.userResponseDto(newUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
