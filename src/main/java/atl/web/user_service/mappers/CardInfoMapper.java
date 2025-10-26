package atl.web.user_service.mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.user_service.dto.CardInfoDto;
import atl.web.user_service.dto.CardInfoResponseDto;
import atl.web.user_service.model.CardInfo;

@Mapper(componentModel = "spring")
public interface CardInfoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    CardInfo toCardInfo(CardInfoDto cardInfoDto);

    
    @Mapping(source = "user.id", target = "userId")
    CardInfoResponseDto toCardInfoResponseDto(CardInfo cardInfo);

    List<CardInfo> toCardInfoList(List<CardInfoDto> cardInfoDtos);

    List<CardInfoResponseDto> toCardInfoResponseDtoList(List<CardInfo> cardInfos);

    default Page<CardInfoResponseDto> toCardInfoResponseDtoPage(Page<CardInfo> cardsInfo) {
        if (cardsInfo == null) {
            return Page.empty();
        }

        List<CardInfoResponseDto> content = cardsInfo.getContent()
                .stream()
                .map(this::toCardInfoResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(
                content,
                cardsInfo.getPageable(),
                cardsInfo.getTotalElements());
    }

    default Optional<CardInfoResponseDto> toOptionalCardInfoResponseDto(Optional<CardInfo> cardInfo) {
        if (!cardInfo.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(toCardInfoResponseDto(cardInfo.get()));
    }
}
