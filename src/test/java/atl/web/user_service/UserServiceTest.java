package atl.web.user_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import atl.web.user_service.dto.CardInfoResponseDto;
import atl.web.user_service.dto.UserDto;
import atl.web.user_service.dto.UserResponseDto;
import atl.web.user_service.exceptions.EmailAlreadyExistsException;
import atl.web.user_service.exceptions.UserNotFoundException;
import atl.web.user_service.mappers.UserMapper;
import atl.web.user_service.model.CardInfo;
import atl.web.user_service.model.User;
import atl.web.user_service.repositories.UserRepository;
import atl.web.user_service.services.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDto userResponseDto;
    private UserDto userDto;

    private CardInfo cardInfo;
    private CardInfoResponseDto cardInfoResponseDto;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(1L)
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1, 2, 2))
                .email("evgenijkhodosok@gmail.com")
                .cards(List.of())
                .build();

        cardInfo = CardInfo.builder()
                .id(1L)
                .user(user)
                .number("1234567812345678")
                .holder("evgenij")
                .expirationDate(LocalDate.of(2000, 10, 10))
                .build();

        user.setCards(List.of(cardInfo));

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1, 2, 2))
                .email("evgenijkhodosok@gmail.com")
                .cards(List.of())
                .build();

        cardInfoResponseDto = CardInfoResponseDto.builder()
                .id(1L)
                .userId(1L)
                .number("1234567812345678")
                .holder("evgenij")
                .expirationDate(LocalDate.of(2000, 10, 10))
                .build();

        userResponseDto.setCards(List.of(cardInfoResponseDto));

        userDto = UserDto.builder()
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1, 2, 2))
                .build();
    }

    @Test
    @DisplayName("Should return user by id")
    void getUserById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toOptionalUserResponseDto(Optional.of(user))).thenReturn(Optional.of(userResponseDto));

        Optional<UserResponseDto> result = userService.findUserById(1L);

        assertEquals(userResponseDto, result.get());
        verify(userMapper, times(1)).toOptionalUserResponseDto(Optional.of(user));
    }

    @Test
    @DisplayName("Should return user by email")
    void getUserByEmail_ShouldReturnUser() {
        when(userRepository.findUserByEmail("evgenijkhodosok@gmail.com")).thenReturn(Optional.of(user));
        when(userMapper.toOptionalUserResponseDto(Optional.of(user))).thenReturn(Optional.of(userResponseDto));

        Optional<UserResponseDto> result = userService.findUserByEmail("evgenijkhodosok@gmail.com");

        assertEquals(userResponseDto, result.get());
        verify(userMapper, times(1)).toOptionalUserResponseDto(Optional.of(user));
    }

    @Test
    @DisplayName("Should return all users")
    void getAllUsers_ShouldReturnListUser() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponseDtoList(List.of(user))).thenReturn(List.of(userResponseDto));

        List<UserResponseDto> result = userService.findAllUsers();

        assertEquals(List.of(userResponseDto), result);
    }

    @Test
    @DisplayName("Should return users by name and surname")
    void getUserByNameAndSurname_ShouldReturnPageUser() {
        when(userRepository.findByNameAndSurname("name", "surname", Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponseDtoPage(new PageImpl<>(List.of(user))))
                .thenReturn(new PageImpl<>(List.of(userResponseDto)));

        Page<UserResponseDto> result = userService.findByNameAndSurname("name", "surname", Pageable.unpaged());

        assertEquals(new PageImpl<>(List.of(userResponseDto)), result);
    }

    @Test
    @DisplayName("Should create and return user")
    void createUser_ShouldReturnUserResponse() {
        when(userRepository.existsByEmail("evgenijkhodosok@gmail.com")).thenReturn(false);
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userDto, "evgenijkhodosok@gmail.com");

        assertEquals(userResponseDto, result);
    }

    @Test
    @DisplayName("Should throw exception if email is exists")
    void createUser_ShouldThrowException_WhenEmailExist() {
        when(userRepository.existsByEmail("evgenijkhodosok@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(userDto, "evgenijkhodosok@gmail.com"));
    }

    @Test
    @DisplayName("Should throw exception if user not found")
    void deleteUser_ShouldThrowExeption_WhenIdNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    @DisplayName("Should update and return updated user")
    void updateUser_ShouldReturnUserResponse() {
        UserDto uDto = new UserDto("name", "surname", LocalDate.of(100, 2, 2));
        UserResponseDto uResponseDto = new UserResponseDto(1L, "name", "surname", LocalDate.of(100, 2, 2),
                "evgenijkhodosok@gmail.com", List.of(cardInfoResponseDto));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(uResponseDto);

        UserResponseDto response = userService.updateUser(1L, uDto);

        assertEquals(uResponseDto, response);

    }

    @Test
    @DisplayName("Should throw exception if user not found")
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, userDto));
    }

}
