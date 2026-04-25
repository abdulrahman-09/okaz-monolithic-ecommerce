package com.am9.okazx.service;

import com.am9.okazx.dto.request.UserRequest;
import com.am9.okazx.dto.response.PageResponse;
import com.am9.okazx.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    PageResponse<UserResponse> findAll(int pageNo, int pageSize, String sortBy);
    UserResponse findById(Long id);
    UserResponse update(Long id, UserRequest updatedUserDto);
    void delete(Long id);

}
