package atl.web.user_service.mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.user_service.dto.UserDto;
import atl.web.user_service.dto.UserResponseDto;
import atl.web.user_service.model.User;

@Mapper(componentModel = "spring", uses = CardInfoMapper.class)
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toUser(UserDto userDto);

    List<UserResponseDto> toUserResponseDtoList(List<User> users);

    default Page<UserResponseDto> toUserResponseDtoPage(Page<User> users) {
        if (users == null) {
            return Page.empty();
        }

        List<UserResponseDto> content = users.getContent()
                .stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(
                content,
                users.getPageable(),
                users.getTotalElements());
    }

    default Optional<UserResponseDto> toOptionalUserResponseDto(Optional<User> user) {
        if (!user.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(toUserResponseDto(user.get()));
    }

}
