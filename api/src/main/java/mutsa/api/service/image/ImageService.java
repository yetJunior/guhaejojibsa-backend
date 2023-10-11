/**
 * @project backend
 * @author ARA
 * @since 2023-10-11 PM 4:50
 */

package mutsa.api.service.image;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mutsa.api.dto.image.ImageResponseDto;
import mutsa.api.dto.image.ImagesRequestDto;
import mutsa.common.domain.models.image.Image;
import mutsa.common.domain.models.image.ImageReference;
import mutsa.common.domain.models.image.ImageStatusFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageModuleService imageModuleService;

    @Transactional
    public List<ImageResponseDto> saveAll(List<ImagesRequestDto> imagesRequestDtos, String articleApiId, ImageReference imgRefType) {
        return imageModuleService.saveAll(imagesRequestDtos, articleApiId, imgRefType)
                .stream()
                .sorted(Comparator.comparing(Image::getId))
                .map(image -> ImageResponseDto.to(image))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteByRefApiId(String refApiId) {
        imageModuleService.deleteByRefApiId(refApiId);
    }

    @Transactional
    public void deleteAllByRefId(String refApiId) {
        imageModuleService.deleteAllByRefId(refApiId);
    }

    public List<ImageResponseDto> getAllByRefId(String refApiId) {
        return imageModuleService.getAllByRefId(refApiId, ImageStatusFilter.ACTIVE)
                .stream()
                .sorted(Comparator.comparing(Image::getId))
                .map(image -> ImageResponseDto.to(image))
                .collect(Collectors.toList());
    }
}
