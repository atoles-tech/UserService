package atl.web.user_service.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardInfoResponseDto implements Serializable{
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
}
