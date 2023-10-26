/**
 * @project backend
 * @author ARA
 * @since 2023-08-16 PM 1:20
 */

package mutsa.api.dto.article;

import static mutsa.common.constants.ImageConstants.DEFAULT_ARTICLE_IMG;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mutsa.api.dto.image.ImageResponseDto;
import mutsa.common.domain.models.Status;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.article.ArticleStatus;
import mutsa.common.domain.models.article.ArticleType;
import mutsa.common.domain.models.image.Image;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticlePageResponseDto {
    private String title;
    private String description;
    private String username;
    private String thumbnail;
    private String apiId;
    private Status status;
    private ArticleStatus articleStatus;
    private String createdDate;
    private Long price;
    private ArticleType articleType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static ArticlePageResponseDto to(Article entity) {
        return ArticlePageResponseDto.builder()
                .title(entity.getTitle())
                .description(entity.getDescription())
                .username(entity.getUser().getUsername())
                .thumbnail(entity.getThumbnail())
                .apiId(entity.getApiId())
                .status(entity.getStatus())
                .articleStatus(entity.getArticleStatus())
                .createdDate(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm:ss")))
                .price(entity.getPrice())
                .articleType(entity.getArticleType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }
}
