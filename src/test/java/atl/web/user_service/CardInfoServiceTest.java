package atl.web.user_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import atl.web.user_service.dto.CardInfoDto;
import atl.web.user_service.dto.CardInfoResponseDto;
import atl.web.user_service.dto.UserResponseDto;
import atl.web.user_service.exceptions.CardNotFoundException;
import atl.web.user_service.exceptions.CardNumberAlreadyExistsException;
import atl.web.user_service.exceptions.UserNotFoundException;
import atl.web.user_service.mappers.CardInfoMapper;
import atl.web.user_service.model.CardInfo;
import atl.web.user_service.model.User;
import atl.web.user_service.repositories.CardInfoRepository;
import atl.web.user_service.services.CardInfoService;
import atl.web.user_service.services.UserService;

@ExtendWith(MockitoExtension.class)
public class CardInfoServiceTest {

    @Mock
    private CardInfoRepository cardInfoRepository;

    @Mock
    private CardInfoMapper cardInfoMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardInfoService cardInfoService;

    private User user;
    private UserResponseDto userResponse;

    private CardInfo cardInfo;
    private CardInfoResponseDto cardInfoResponse;
    private CardInfoDto cardInfoDto;

    @BeforeEach
    private void init() {
        user = User.builder()
                .id(1L)
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1, 1, 1))
                .email("email@gmail.com")
                .cards(List.of())
                .build();

        cardInfo = CardInfo.builder()
                .id(1L)
                .user(user)
                .number("1234567812345678")
                .holder("name_surname")
                .expirationDate(LocalDate.of(2027, 10, 10))
                .build();

        user.setCards(List.of(cardInfo));

        userResponse = UserResponseDto.builder()
                .id(1L)
                .name("name")
                .surname("surname")
                .birthDate(LocalDate.of(1, 1, 1))
                .email("email@gmail.com")
                .cards(List.of())
                .build();

        cardInfoResponse = CardInfoResponseDto.builder()
                .id(1L)
                .userId(1L)
                .number("1234567812345678")
                .holder("name_surname")
                .expirationDate(LocalDate.of(2027, 10, 10))
                .build();

        userResponse.setCards(List.of(cardInfoResponse));

        cardInfoDto = CardInfoDto.builder()
                .number("1234567812345678")
                .holder("name_surname")
                .expirationDate(LocalDate.of(2027, 10, 10))
                .build();
    }

    @Test
    @DisplayName("Should return list of cards by user id")
    void findByUserId_ShouldReturnCardsList() {
        when(cardInfoRepository.findByUserId(1L)).thenReturn(List.of(cardInfo));
        when(cardInfoMapper.toCardInfoResponseDtoList(List.of(cardInfo))).thenReturn(List.of(cardInfoResponse));

        List<CardInfoResponseDto> result = cardInfoService.findByUserId(1L);

        assertEquals(List.of(cardInfoResponse), result);
    }

    @Test
    @DisplayName("Should return page of cards by user id")
    void findByUserId_ShouldReturnPage() {
        when(cardInfoRepository.findByUserId(1L, Pageable.unpaged())).thenReturn(new PageImpl<>(List.of(cardInfo)));
        when(cardInfoMapper.toCardInfoResponseDtoPage(new PageImpl<>(List.of(cardInfo))))
                .thenReturn(new PageImpl<>(List.of(cardInfoResponse)));

        Page<CardInfoResponseDto> result = cardInfoService.findByUserId(1L, Pageable.unpaged());

        assertEquals(new PageImpl<>(List.of(cardInfoResponse)), result);
    }

    @Test
    @DisplayName("Should return card by id")
    void findById_ShouldReturnCard() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.toOptionalCardInfoResponseDto(Optional.of(cardInfo)))
                .thenReturn(Optional.of(cardInfoResponse));

        Optional<CardInfoResponseDto> result = cardInfoService.findById(1L);

        assertEquals(Optional.of(cardInfoResponse), result);
    }

    @Test
    @DisplayName("Should return card by number")
    void findByNumber_ShouldReturnCard() {
        when(cardInfoRepository.findByNumber("1234567812345678")).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.toOptionalCardInfoResponseDto(Optional.of(cardInfo)))
                .thenReturn(Optional.of(cardInfoResponse));

        Optional<CardInfoResponseDto> result = cardInfoService.findByNumber("1234567812345678");

        assertEquals(Optional.of(cardInfoResponse), result);
    }

    @Test
    @DisplayName("Should return list of cards")
    void findAll_ShouldReturnList() {
        when(cardInfoRepository.findAll()).thenReturn(List.of(cardInfo));
        when(cardInfoMapper.toCardInfoResponseDtoList(List.of(cardInfo))).thenReturn(List.of(cardInfoResponse));

        List<CardInfoResponseDto> cards = cardInfoService.findAll();

        assertEquals(List.of(cardInfoResponse), cards);
    }

    @Test
    @DisplayName("Should create and return card")
    void createCardInfo_ShouldReturnCard() {
        when(cardInfoRepository.existsByNumber("1234567812345678")).thenReturn(false);
        when(cardInfoMapper.toCardInfo(cardInfoDto)).thenReturn(cardInfo);
        when(userService.findUserEntityById(1L)).thenReturn(Optional.of(user));
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toCardInfoResponseDto(cardInfo)).thenReturn(cardInfoResponse);

        CardInfoResponseDto result = cardInfoService.createCardInfo(cardInfoDto, 1L);

        assertEquals(cardInfoResponse, result);
    }

    @Test
    @DisplayName("Should throw exception if number exists")
    void createCardInfo_ShouldThrowException_WhenExistsNumber() {
        when(cardInfoRepository.existsByNumber("1234567812345678")).thenReturn(true);

        assertThrows(CardNumberAlreadyExistsException.class, () -> cardInfoService.createCardInfo(cardInfoDto, 1L));
    }

    @Test
    @DisplayName("Should throw exception if user exists")
    void createCardInfo_ShouldThrowException_WhenUserNotExists() {
        when(cardInfoRepository.existsByNumber("1234567812345678")).thenReturn(false);
        when(cardInfoMapper.toCardInfo(cardInfoDto)).thenReturn(cardInfo);
        when(userService.findUserEntityById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardInfoService.createCardInfo(cardInfoDto, 1L));
    }

    @Test
    @DisplayName("Should update and return card")
    void updateCardInfo_ShouldReturnCard() {
        CardInfoDto ci = new CardInfoDto("1234567812345678", "name_surname", LocalDate.of(1000, 10, 10));
        CardInfoResponseDto responseDto = new CardInfoResponseDto(1L, 1L, "1234567812345678", "name_surname",
                LocalDate.of(1000, 10, 10));
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toCardInfoResponseDto(cardInfo)).thenReturn(responseDto);

        CardInfoResponseDto result = cardInfoService.updateCardInfo(1L, ci);

        assertEquals(responseDto, result);
    }

    @Test
    @DisplayName("Should throw exception if card not found")
    void updateCardInfo_ShouldThrowException_WhenCardNotFound() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardInfoService.updateCardInfo(1L, cardInfoDto));
    }

    @Test
    @DisplayName("Should throw exception if number exists")
    void updateCardInfo_ShouldThrowException_WhenCardNumberExists() {
        CardInfoDto ci = new CardInfoDto("987654329765432", "name_surname", LocalDate.of(1000, 10, 10));
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.existsByNumber("987654329765432")).thenReturn(true);

        assertThrows(CardNumberAlreadyExistsException.class, () -> cardInfoService.updateCardInfo(1L, ci));
    }

    @Test
    @DisplayName("Should throw exception if it not found")
    void deleteCardInfo_ShouldThrowException_WhenCardInfoNotFound() {
        when(cardInfoRepository.existsById(1L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardInfoService.deleteCardInfo(1L));
    }
}
