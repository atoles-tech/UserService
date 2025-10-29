package atl.web.user_service.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardUpdateDto {
    
    @Size(min = 16, max = 20, message = "Size of number must be between 16 and 20")
    private String number;

    @Size(min = 3, max = 100, message = "Size must be between 3 and 100")
    private String holder;

    private LocalDate expirationDate;

}