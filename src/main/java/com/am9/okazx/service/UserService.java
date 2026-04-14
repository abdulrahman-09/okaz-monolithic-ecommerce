package com.am9.okazx.service;

import com.am9.okazx.model.dto.UserRequest;
import com.am9.okazx.model.dto.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserResponse create(UserRequest userDto);
    UserResponse update(Long id, UserRequest updatedUserDto);
    void delete(Long id);

}
