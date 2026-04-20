package com.am9.okazx.service;

import com.am9.okazx.dto.request.UserRequest;
import com.am9.okazx.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserResponse update(Long id, UserRequest updatedUserDto);
    void delete(Long id);

}
