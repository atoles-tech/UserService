package atl.web.user_service.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardInfoDto {
    
    @NotBlank(message = "Name is required")
    @Size(min=16, max=20, message = "Size of number must be between 16 and 20")
    private String number;

    @NotBlank(message = "Holder is required")
    @Size(min=3, max=100, message = "Size must be between 3 and 100")
    private String holder;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

}
    