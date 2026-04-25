package com.am9.okazx.service.impl;

import com.am9.okazx.dto.response.PageResponse;
import com.am9.okazx.exception.ResourceNotFoundException;
import com.am9.okazx.dto.response.UserResponse;
import com.am9.okazx.exception.UserAlreadyExistsException;
import com.am9.okazx.mapper.UserMapper;
import com.am9.okazx.dto.request.UserRequest;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.repository.OrderRepository;
import com.am9.okazx.repository.UserRepository;
import com.am9.okazx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int pageNo, int pageSize, String SortBy) {
        String[] sortBy = SortBy.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(sortBy[1]), sortBy[0]);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> content = page.getContent()
                .stream()
                .map(userMapper::toDto)
                .toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest updatedUserDto) {
       User user = userRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(updatedUserDto.email())
                && userRepository.findByEmail(updatedUserDto.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        userMapper.updateEntityFromRequest(updatedUserDto, user);
       return userMapper.toDto(
               userRepository.save(user)
       );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        orderRepository.detachFromUser(id);
        userRepository.deleteById(id);
    }


}
