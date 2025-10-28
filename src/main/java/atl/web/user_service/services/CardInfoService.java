package atl.web.user_service.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.user_service.dto.CardInfoDto;
import atl.web.user_service.dto.CardInfoResponseDto;
import atl.web.user_service.dto.CardUpdateDto;
import atl.web.user_service.exceptions.CardNotFoundException;
import atl.web.user_service.exceptions.CardNumberAlreadyExistsException;
import atl.web.user_service.exceptions.UserNotFoundException;
import atl.web.user_service.mappers.CardInfoMapper;
import atl.web.user_service.model.CardInfo;
import atl.web.user_service.model.User;
import atl.web.user_service.repositories.CardInfoRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CardInfoService {

    private final CardInfoRepository cardInfoRepository;
    private final CardInfoMapper cardInfoMapper;

    private final UserService userService;

    public List<CardInfoResponseDto> findByUserId(Long userId) {
        return cardInfoMapper.toCardInfoResponseDtoList(cardInfoRepository.findByUserId(userId));
    }

    public Page<CardInfoResponseDto> findByUserId(Long id, Pageable pageable) {
        return cardInfoMapper.toCardInfoResponseDtoPage(cardInfoRepository.findByUserId(id, pageable));
    }

    public Optional<CardInfoResponseDto> findById(Long id){
        return cardInfoMapper.toOptionalCardInfoResponseDto(cardInfoRepository.findById(id));
    }

    public Optional<CardInfoResponseDto> findByNumber(String number) {
        return cardInfoMapper.toOptionalCardInfoResponseDto(cardInfoRepository.findByNumber(number));
    }

    public Boolean existsByNumber(String number) {
        return cardInfoRepository.existsByNumber(number);
    }

    public List<CardInfoResponseDto> findAll() {
        return cardInfoMapper.toCardInfoResponseDtoList(cardInfoRepository.findAll());
    }

    @Transactional
    public CardInfoResponseDto createCardInfo(CardInfoDto cardInfoDto, Long userId) {
        if (existsByNumber(cardInfoDto.getNumber())) {
            throw new CardNumberAlreadyExistsException(cardInfoDto.getNumber());
        }

        CardInfo cardInfo = cardInfoMapper.toCardInfo(cardInfoDto);
        User user = userService.findUserEntityById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        cardInfo.setUser(user);

        cardInfoRepository.save(cardInfo);
        
        return cardInfoMapper.toCardInfoResponseDto(cardInfo);
    }

    @Transactional
    public CardInfoResponseDto updateCardInfo(Long id, CardUpdateDto cardInfo) {
        CardInfo currentCardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!currentCardInfo.getNumber().equals(cardInfo.getNumber())
                && existsByNumber(cardInfo.getNumber())) {
            throw new CardNumberAlreadyExistsException(cardInfo.getNumber());
        }

        currentCardInfo.setNumber(cardInfo.getNumber());
        currentCardInfo.setHolder(cardInfo.getHolder());
        currentCardInfo.setExpirationDate(cardInfo.getExpirationDate());

        cardInfoRepository.save(currentCardInfo);

        return cardInfoMapper.toCardInfoResponseDto(currentCardInfo);
    }

    @Transactional
    public void deleteCardInfo(Long id) {
        if (!cardInfoRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }
        cardInfoRepository.deleteById(id);
    }
}
