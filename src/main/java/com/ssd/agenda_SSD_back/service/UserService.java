package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.ChangePasswordDto;
import com.ssd.agenda_SSD_back.dto.SelfProfileUpdateDto;
import com.ssd.agenda_SSD_back.dto.UserCreatedDto;
import com.ssd.agenda_SSD_back.dto.UserUpdateDto;
import com.ssd.agenda_SSD_back.exception.BusinessRuleException;
import com.ssd.agenda_SSD_back.exception.DuplicateEntryException;
import com.ssd.agenda_SSD_back.exception.InvalidEmailFormatException;
import com.ssd.agenda_SSD_back.exception.NotFoundException;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.util.EmailValidator;
import com.ssd.agenda_SSD_back.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // O admin não escolhe a senha de ninguém — o sistema gera uma temporária,
    // marca mustChangePassword=true e devolve o valor em texto puro só nesta
    // resposta (ver UserCreatedDto). changePassword() zera a flag depois.
    public UserCreatedDto createUser(User newUser) {
        boolean isEmail = EmailValidator.isValidEmail(newUser.getEmail());
        if (!isEmail) {
            throw new InvalidEmailFormatException("Invalid email format");
        }
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new DuplicateEntryException("Email já cadastrado");
        }

        String temporaryPassword = PasswordGenerator.generate();
        newUser.setPassword(passwordEncoder.encode(temporaryPassword));
        newUser.setMustChangePassword(true);

        User savedUser = userRepository.save(newUser);

        return new UserCreatedDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), temporaryPassword);
    }

    public User findUserById(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Usuário não encontrado com ID " + id));
    }

    // Lista completa, sem filtro — usada pela tela de gestão de acessos (admin).
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Busca por nome ou email — usada pelo campo de busca da tela de gestão de acessos.
    public List<User> searchUsers(String term) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
    }

    public User updateUser(Long id, UserUpdateDto userUpdateDto, String requestingUserEmail) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID " + id));

        // Ninguém troca a própria role por aqui — evita o admin se rebaixar
        // por engano e, como efeito colateral, garante que o sistema nunca
        // fica sem nenhum ADMIN (quem seria capaz de fazer isso é sempre o
        // próprio admin agindo sobre si mesmo).
        if (isSelf(existingUser, requestingUserEmail) && existingUser.getRole() != userUpdateDto.getRole()) {
            throw new BusinessRuleException("Você não pode alterar sua própria role.");
        }

        boolean isEmail = EmailValidator.isValidEmail(userUpdateDto.getEmail());
        if (!isEmail) {
            throw new InvalidEmailFormatException("Invalid email format");
        }

        // Só barra duplicidade se o e-mail novo já pertence a OUTRO usuário.
        userRepository.findByEmail(userUpdateDto.getEmail()).ifPresent(otherUser -> {
            if (!otherUser.getId().equals(existingUser.getId())) {
                throw new DuplicateEntryException("Email já cadastrado");
            }
        });

        existingUser.setName(userUpdateDto.getName());
        existingUser.setEmail(userUpdateDto.getEmail());
        existingUser.setMatricula(userUpdateDto.getMatricula());
        existingUser.setRole(userUpdateDto.getRole());

        return userRepository.save(existingUser);
    }

    // "Apagar" usuário pela tela de gestão de acessos = desativar. Preserva o
    // histórico (Meeting/LogUpdate têm FK obrigatória pro usuário) e é reversível.
    public void deactivateUser(Long id, String requestingUserEmail) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID " + id));

        if (isSelf(existingUser, requestingUserEmail)) {
            throw new BusinessRuleException("Você não pode desativar sua própria conta.");
        }

        existingUser.setEnabled(false);
        userRepository.save(existingUser);
    }

    public void reactivateUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID " + id));

        existingUser.setEnabled(true);
        userRepository.save(existingUser);
    }

    // Mesmo mecanismo do cadastro (PasswordGenerator + mustChangePassword),
    // agora pra quem já existe — útil quando alguém esquece a senha. O admin
    // não pode resetar a própria senha por aqui: pra isso já existe o
    // autoatendimento (changePassword), que exige saber a senha atual.
    public UserCreatedDto resetPassword(Long id, String requestingUserEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID " + id));

        if (isSelf(user, requestingUserEmail)) {
            throw new BusinessRuleException("Use \"Minha conta\" para trocar a sua própria senha.");
        }

        String temporaryPassword = PasswordGenerator.generate();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        return new UserCreatedDto(user.getId(), user.getName(), user.getEmail(), temporaryPassword);
    }

    private boolean isSelf(User target, String requestingUserEmail) {
        return target.getEmail().equalsIgnoreCase(requestingUserEmail);
    }

    // Base pro autoatendimento (trocar o próprio email ou senha): confirma
    // que quem está pedindo a alteração realmente sabe a senha atual, antes
    // de qualquer endpoint decidir o que fazer com isso. Reaproveitado tanto
    // pra troca de perfil quanto pra troca de senha (ver roadmap "Média").
    public User verifyCurrentPassword(String requestingUserEmail, String currentPassword) {
        User user = userRepository.findByEmail(requestingUserEmail)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado: " + requestingUserEmail));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        return user;
    }

    // Autoatendimento: o próprio usuário troca nome/email (não mexe em role
    // nem matrícula — isso continua exclusivo do admin em updateUser).
    // Trocar o email invalida o token atual (o JWT usa o email como subject),
    // então o front precisa deslogar e pedir novo login depois disso.
    public User updateProfile(String requestingUserEmail, SelfProfileUpdateDto dto) {
        User user = verifyCurrentPassword(requestingUserEmail, dto.getCurrentPassword());

        boolean isEmail = EmailValidator.isValidEmail(dto.getEmail());
        if (!isEmail) {
            throw new InvalidEmailFormatException("Invalid email format");
        }

        userRepository.findByEmail(dto.getEmail()).ifPresent(otherUser -> {
            if (!otherUser.getId().equals(user.getId())) {
                throw new DuplicateEntryException("Email já cadastrado");
            }
        });

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        return userRepository.save(user);
    }

    // Autoatendimento: troca de senha, exige a senha atual (ver
    // verifyCurrentPassword). Não invalida tokens já emitidos — limitação
    // conhecida do JWT sem blocklist, registrada no roadmap.
    public void changePassword(String requestingUserEmail, ChangePasswordDto dto) {
        User user = verifyCurrentPassword(requestingUserEmail, dto.getCurrentPassword());
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        // Troca de senha bem-sucedida sempre cumpre a exigência de primeiro
        // acesso, se houver uma pendente.
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Usuário desativado");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Senha inválida");
        }
        return user;
    }
}
