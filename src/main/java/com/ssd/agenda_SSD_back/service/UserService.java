package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.repository.UserRepository;
import com.ssd.agenda_SSD_back.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User newUser) {
        //Criar uma verificação de formato de email válido

        //Criar uma verificação se o email já está cadastrado

        //Criptografar a senha antes de salvar no BD

        return userRepository.save(newUser);
    }

    public User findUserById(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new RuntimeException(
                "Usuário não encontrado com ID "+ id ));

        //Criar NotFoundExcepiton para substituir RuntimeException
    }
}
