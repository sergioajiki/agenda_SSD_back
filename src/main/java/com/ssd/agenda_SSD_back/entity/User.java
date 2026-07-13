package com.ssd.agenda_SSD_back.entity;

import com.ssd.agenda_SSD_back.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "users") //Nome explícito para a tabela
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(nullable = false, length = 100)
    private String password;
    // Opcional: nem todo usuário cadastrado pelo admin tem matrícula (ex.: colaboradores externos).
    @Column(nullable = true)
    private String matricula;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
    // "Apagar" usuário pela tela de gestão de acessos é desativar, não excluir
    // de verdade — Meeting e LogUpdate têm FK obrigatória pro usuário, então
    // um DELETE físico quebraria pra qualquer um que já tenha reunião/log.
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean enabled = true;
    // Marcado quando o admin gera uma senha temporária pro usuário — o front
    // força a tela de trocar senha antes de liberar qualquer outra coisa,
    // e UserService.changePassword zera isso depois de uma troca bem-sucedida.
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean mustChangePassword = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
