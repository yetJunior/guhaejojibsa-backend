/**
 * @project backend
 * @author ARA
 * @since 2023-08-16 PM 2:25
 */

package mutsa.api.dto.article;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;
import mutsa.api.dto.image.ImagesRequestDto;
import mutsa.common.domain.models.article.ArticleStatus;

import java.util.List;
import mutsa.common.domain.models.article.ArticleType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUpdateRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String apiId;
    @NotNull
    private ArticleStatus articleStatus;
    private List<ImagesRequestDto> images;
    @NotNull
    @Min(value = 0, message = "가격은 최소 0 원 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "가격은 최대 2^63-1원을 넘길 수 없습니다.")
    private Long price;
    @NotNull
    private ArticleType articleType;
    @FutureOrPresent
    private LocalDateTime startDate;
    @FutureOrPresent
    private LocalDateTime endDate;
}
