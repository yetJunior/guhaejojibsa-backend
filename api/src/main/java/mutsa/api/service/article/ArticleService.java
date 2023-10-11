/**
 * @project backend
 * @author ARA
 * @since 2023-08-16 PM 1:18
 */

package mutsa.api.service.article;

import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mutsa.api.dto.article.ArticleCreateRequestDto;
import mutsa.api.dto.article.ArticleFilterDto;
import mutsa.api.dto.article.ArticlePageResponseDto;
import mutsa.api.dto.article.ArticleResponseDto;
import mutsa.api.dto.article.ArticleUpdateRequestDto;
import mutsa.api.dto.image.ImageResponseDto;
import mutsa.api.service.image.ImageModuleService;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.filter.article.ArticleFilter;
import mutsa.common.domain.models.article.Article;
import mutsa.common.domain.models.image.Image;
import mutsa.common.domain.models.image.ImageReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleModuleService articleModuleService;
    private final ImageModuleService imageModuleService;

    public ArticleResponseDto save(ArticleCreateRequestDto requestDto) {
        Article article = articleModuleService.save(requestDto);

        if (requestDto.getImages() == null || requestDto.getImages().isEmpty()) {
            return ArticleResponseDto.to(article, null);
        }

        List<ImageResponseDto> imageResponseDtos = imageModuleService.saveAll(requestDto.getImages(),
                        article.getApiId(), ImageReference.ARTICLE)
                .stream()
                .sorted(Comparator.comparing(Image::getId))
                .map(image -> ImageResponseDto.to(image))
                .collect(Collectors.toList());
        return ArticleResponseDto.to(article, imageResponseDtos);
    }

    public ArticleResponseDto update(ArticleUpdateRequestDto updateDto) {
        Article article = articleModuleService.update(updateDto);

        if (updateDto.getImages() == null || updateDto.getImages().isEmpty()) {
            return ArticleResponseDto.to(article, null);
        }

        //  기존에 있던 이미지는 논리 삭제 처리
        imageModuleService.deleteByRefApiId(article.getApiId());
//        article = deleteImages(article);
        article.setThumbnail(null);
        List<ImageResponseDto> imageResponseDtos = imageModuleService.saveAll(updateDto.getImages(),
                article.getApiId(), ImageReference.ARTICLE)
                .stream()
                .sorted(Comparator.comparing(Image::getId))
                .map(image -> ImageResponseDto.to(image))
                .collect(Collectors.toList());
        return ArticleResponseDto.to(article, imageResponseDtos);
    }

    protected void deleteImages(Article article) {
        imageModuleService.deleteAllByRefId(article.getApiId());
    }

    protected Article getByApiId(String apiId) {
        return articleModuleService.getByApiId(apiId);
    }

    protected Article getById(Long id) {
        return articleModuleService.getById(id);
    }

    public ArticleResponseDto read(String apiId) {
        Article articleEntity = articleModuleService.getByApiId(apiId);
        List<ImageResponseDto> imageResponseDtos = imageModuleService.getAllByRefId(apiId)
                .stream()
                //  Id를 기준으로 정렬 수행
                .sorted(Comparator.comparing(Image::getId))
                .map(image -> ImageResponseDto.to(image))
                .collect(Collectors.toList());

        return ArticleResponseDto.to(articleEntity, imageResponseDtos);
    }

    public Page<ArticlePageResponseDto> getPageByUsername(
            String username, Sort.Direction direction, ArticleFilterDto articleFilter
    ) {
        return getPageByUsername(username, 0, 10, direction, articleFilter);
    }

    public Page<ArticlePageResponseDto> getPageByUsername(
            String username, int pageNum, int size, Sort.Direction direction, ArticleFilterDto articleFilter
    ) {
        return articleModuleService.getPageByUsername(
                username,
                pageNum,
                size,
                direction,
                "id",
                ArticleFilterDto.to(articleFilter)
        ).map(ArticlePageResponseDto::to);
    }

    public Page<ArticlePageResponseDto> getPageByUsername(
            String username,
            int pageNum,
            int size,
            Sort.Direction direction,
            String orderProperties,
            ArticleFilterDto articleFilter
    ) {
        return articleModuleService.getPageByUsername(
                username,
                pageNum,
                size,
                direction,
                orderProperties,
                ArticleFilterDto.to(articleFilter)
        ).map(ArticlePageResponseDto::to);
    }

    public Page<ArticlePageResponseDto> getPage(
            int pageNum, int size, Sort.Direction direction, ArticleFilterDto articleFilter
    ) {
        return articleModuleService.getPage(
                pageNum,
                size,
                direction,
                "id",
                ArticleFilterDto.to(articleFilter)
        ).map(ArticlePageResponseDto::to);
    }

    public Page<ArticlePageResponseDto> getPage(
            int pageNum, int size, Sort.Direction direction, String orderProperties, ArticleFilterDto articleFilter
    ) {
        return articleModuleService.getPage(
                pageNum,
                size,
                direction,
                orderProperties,
                ArticleFilterDto.to(articleFilter)
        ).map(ArticlePageResponseDto::to);
    }

    public void deleteByApiId(String apiId) {
        articleModuleService.deleteByApiId(apiId);
        imageModuleService.deleteByRefApiId(apiId);
    }

    /**
     * 유저 호출에 의한 삭제를 할 경우 이 메소드를 실행할 것! 현재 호출 한 유저가 게시글 작성자인지, 아니면 어드민 권한을 가지고 있는지 확인 후 삭제 기능 수행
     *
     * @param apiId 해당 게시글의 apiId(uuid)
     */
    public void delete(String apiId) {
        articleModuleService.delete(apiId);
        imageModuleService.deleteAllByRefId(apiId);
    }
}
