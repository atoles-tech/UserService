package atl.web.user_service.services;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.user_service.dto.UserDto;
import atl.web.user_service.dto.UserResponseDto;
import atl.web.user_service.exceptions.EmailAlreadyExistsException;
import atl.web.user_service.exceptions.UserNotFoundException;
import atl.web.user_service.mappers.UserMapper;
import atl.web.user_service.model.User;
import atl.web.user_service.repositories.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "users")
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDto> findAllUsers() {
        return userMapper.toUserResponseDtoList(userRepository.findAll());
    }

    public Page<UserResponseDto> findAllUsers(Pageable pageable) {
        return userMapper.toUserResponseDtoPage(userRepository.findAll(pageable));
    }

    public Optional<UserResponseDto> findUserByEmail(String email) {
        return userMapper.toOptionalUserResponseDto(userRepository.findUserByEmail(email));
    }

    @Cacheable(key = "#id")
    public Optional<UserResponseDto> findUserById(Long id){
        return userMapper.toOptionalUserResponseDto(userRepository.findById(id));
    }

    public Page<UserResponseDto> findByNameAndSurname(String name, String surname, Pageable pageable) {
        return userMapper.toUserResponseDtoPage(userRepository.findByNameAndSurname(name, surname, pageable));
    }

    @Transactional
    public UserResponseDto createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EmailAlreadyExistsException(userDto.getEmail());
        }
        User user = userMapper.toUser(userDto);
        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }

    @Transactional
    @CachePut(key="#id")
    public UserResponseDto updateUser(Long id, UserDto userDetails) {

        User currentUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(id));

        if (!currentUser.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new EmailAlreadyExistsException(userDetails.getEmail());
        }

        currentUser.setName(userDetails.getName());
        currentUser.setSurname(userDetails.getSurname());
        currentUser.setBirthDate(userDetails.getBirthDate());
        currentUser.setEmail(userDetails.getEmail());

        userRepository.save(currentUser);

        return userMapper.toUserResponseDto(currentUser);
    }

    // ENTITY

    public Optional<User> findUserEntityById(Long id){
        return userRepository.findById(id);
    }
}
