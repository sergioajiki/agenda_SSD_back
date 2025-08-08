package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.exception.DuplicateEntryException;
import com.ssd.agenda_SSD_back.exception.InvalidEmailFormatException;
import com.ssd.agenda_SSD_back.exception.NotFoundException;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.util.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User newUser) {
        //Criar uma verificação de formato de email válido
        boolean isEmail = EmailValidator.isValidEmail(newUser.getEmail());
        if (!isEmail) {
            throw new InvalidEmailFormatException("Invalid email format");
        }
        //Criar uma verificação se o email já está cadastrado
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new DuplicateEntryException("Email já cadastrado");
        }

        //Criptografar a senha antes de salvar no BD

        return userRepository.save(newUser);
    }

    public User findUserById(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                "Usuário não encontrado com ID " + id));
    }
}
