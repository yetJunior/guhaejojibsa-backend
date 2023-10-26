/**
 * @project backend
 * @author ARA
 * @since 2023-08-16 PM 1:20
 */

package mutsa.api.dto.article;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import mutsa.api.dto.image.ImagesRequestDto;
import mutsa.common.domain.models.article.ArticleType;

@Getter
@Setter
public class ArticleCreateRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    @Min(value = 0, message = "가격은 최소 0원 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "가격은 최대 2^63-1원을 넘길 수 없습니다.")
    private Long price;
    private List<ImagesRequestDto> images;
    @NotNull
    private ArticleType articleType;
    @FutureOrPresent
    private LocalDateTime startDate;
    @FutureOrPresent
    private LocalDateTime endDate;
}
