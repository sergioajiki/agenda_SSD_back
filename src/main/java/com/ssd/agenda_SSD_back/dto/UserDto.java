package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {
    @Schema(description = "Nome do usuário")
    @NotBlank(message = "O nome é obrigatório")
    private String name;
    @Schema(description = "Email do usuário")
    @NotBlank(message = "O email é obrigatório")
    private String email;
    @Schema(description = "Senha do usuário, entre 6 e 10 caracteres")
    @Size(min = 6, max = 10, message = "A senha deve ter entre 6 e 10 caracteres.")
    private String password;
    @Schema(description = "Matrícula do usuário")
    private Long matricula;
    @Schema(description = "Tipo acesso do usuário")
    private UserRole role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getMatricula() {
        return matricula;
    }

    public void setMatricula(Long matricula) {
        this.matricula = matricula;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public static User toEntity(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword()); //Será criptografada no Service
        user.setMatricula(userDto.getMatricula());
        user.setRole(userDto.getRole());
        return user;
    }
}
