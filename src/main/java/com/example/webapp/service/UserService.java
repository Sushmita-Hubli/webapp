package com.example.webapp.service;

import com.example.webapp.dto.UserRequestDTO;
import com.example.webapp.dto.UserResponseDTO;
import com.example.webapp.dto.UserUpdateDTO;
import com.example.webapp.model.User;

import java.util.UUID;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO updateUser(String email, UserUpdateDTO userUpdateDTO);

    User loadUserByEmail(String email);

    boolean emailExists(String email);
}