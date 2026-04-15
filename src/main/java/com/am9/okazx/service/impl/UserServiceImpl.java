package com.am9.okazx.service.impl;

import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.model.dto.UserResponse;
import com.am9.okazx.mapper.UserMapper;
import com.am9.okazx.model.dto.UserRequest;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserResponse create(UserRequest userDto) {
        return userMapper.toDto(
                userRepository.save(userMapper.toEntity(userDto))
        );
    }

    @Override
    public UserResponse update(Long id, UserRequest updatedUserDto) {
       User user = userRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

       userMapper.updateEntityFromRequest(updatedUserDto, user);
       return userMapper.toDto(
               userRepository.save(user)
       );
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }


}
