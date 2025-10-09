package com.ssd.agenda_SSD_back.controller;
import com.ssd.agenda_SSD_back.dto.UserDto;
import com.ssd.agenda_SSD_back.dto.UserResponseDto;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Tag(name = "Users", description = "Gerenciamento de usuários")
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    final private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    @Operation(summary = "Login de usuário", description = "Autentica usuário pelo email e senha")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserDto userDto) {
        try {
            User loggedUser = userService.login(userDto.getEmail(), userDto.getPassword());
            UserResponseDto responseDto = UserResponseDto.userResponseDto(loggedUser);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
