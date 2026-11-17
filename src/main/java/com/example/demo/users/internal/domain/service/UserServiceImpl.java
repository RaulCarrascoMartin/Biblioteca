package com.example.demo.users.internal.domain.service;

import com.example.demo.shared.PasswordHasher;
import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserCredentialsDto;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import com.example.demo.users.internal.domain.mapper.UserMapper;
import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
import com.example.demo.users.spi.UserInUseChecker;
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
    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserDto> getByIds(Collection<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toMap(UserDto::id, Function.identity()));
    }

    @Override
    @Transactional
    public Optional<UserDto> register(NewUserDto dto) {
        Email email = new Email(dto.email());
        if (userRepository.findByEmail(email.value()).isPresent()) {
            return Optional.empty();
        }
        User user = userMapper.toModel(dto, passwordHasher.hash(dto.password()));
        return Optional.of(userMapper.toDto(userRepository.save(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentialsDto> findCredentialsByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toCredentialsDto);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        Email newEmail = new Email(dto.email());
        if (!user.email().equals(newEmail) && userRepository.findByEmail(newEmail.value()).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        User updated = user.rename(dto.name()).changeEmail(newEmail);
        if (dto.password() != null && !dto.password().isBlank()) {
            updated = updated.changePassword(passwordHasher.hash(dto.password()));
        }
        return userMapper.toDto(userRepository.save(updated));
    }

    @Override
    @Transactional
    public void revokeSessions(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.save(user.revokeSessions());
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

    @Override
    @Transactional
    public void ensureAdmin(String email, String rawPassword) {
        Email adminEmail = new Email(email);
        if (userRepository.findByEmail(adminEmail.value()).isPresent()) {
            return;
        }
        userRepository.save(User.newAdmin(adminEmail, passwordHasher.hash(rawPassword)));
    }
}
