/**
 * @project backend
 * @author ARA
 * @since 2023-08-16 PM 1:20
 */

package mutsa.api.dto.article;

import java.time.LocalDateTime;
import lombok.*;
import mutsa.api.dto.image.ImageResponseDto;
import mutsa.common.domain.models.Status;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.article.ArticleStatus;
import mutsa.common.domain.models.article.ArticleType;
import mutsa.common.domain.models.image.Image;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static mutsa.common.constants.ImageConstants.DEFAULT_ARTICLE_IMG;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleResponseDto {
    private String title;
    private String description;
    private String username;
    private String apiId;
    private Status status;
    private ArticleStatus articleStatus;
    private String createdDate;
    private List<ImageResponseDto> images;
    private Long price;
    private ArticleType articleType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static ArticleResponseDto to(Article entity, List<ImageResponseDto> images) {
        return ArticleResponseDto.builder()
                .title(entity.getTitle())
                .description(entity.getDescription())
                .username(entity.getUser().getUsername())
                .apiId(entity.getApiId())
                .status(entity.getStatus())
                .articleStatus(entity.getArticleStatus())
                .createdDate(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm:ss")))
                .price(entity.getPrice())
                .images(images)
                .articleType(entity.getArticleType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }
}
