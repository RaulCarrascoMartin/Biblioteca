package com.example.demo.users.internal.domain.service;

import com.example.demo.shared.PasswordHasher;
import com.example.demo.users.UserInUseChecker;
import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import com.example.demo.users.internal.domain.mapper.UserMapper;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final List<UserInUseChecker> inUseCheckers;
    private final PasswordHasher passwordHasher;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           List<UserInUseChecker> inUseCheckers,
                           PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.inUseCheckers = inUseCheckers;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserResponseDto> getByIds(Collection<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toMap(UserResponseDto::id, Function.identity()));
    }

    @Override
    @Transactional
    public Optional<UserResponseDto> register(UserDto dto) {
        String email = dto.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            return Optional.empty();
        }

        User user = new User();
        user.setName(dto.name().trim());
        user.setEmail(email);
        user.setPassword(passwordHasher.hash(dto.password()));
        user.setRole("USER");

        return Optional.of(userMapper.toResponse(userRepository.save(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentialsDto> findCredentialsByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toCredentials);
    }

    @Override
    @Transactional
    public UserResponseDto update(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        String newEmail = dto.email().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        user.setName(dto.name().trim());
        user.setEmail(newEmail);

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordHasher.hash(dto.password()));
            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void revokeSessions(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id, Long requesterId) {
        if (id.equals(requesterId)) {
            throw new SelfDeletionException();
        }
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        if (inUseCheckers.stream().anyMatch(checker -> checker.isInUse(id))) {
            throw new UserWithLoansException();
        }
        userRepository.deleteById(id);
    }
}
