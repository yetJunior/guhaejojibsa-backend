/**
 * @project backend
 * @author ARA
 * @since 2023-09-07 AM 9:16
 */

package mutsa.api.service.image;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mutsa.api.dto.image.ImagesRequestDto;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.Status;
import mutsa.common.domain.models.image.Image;
import mutsa.common.domain.models.image.ImageReference;
import mutsa.common.domain.models.image.ImageStatusFilter;
import mutsa.common.domain.models.user.User;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.image.ImageRepository;
import mutsa.common.repository.user.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ImageModuleService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<Image> saveAll(List<ImagesRequestDto> imagesRequestDtos, String refApiId, ImageReference imgRefType) {
        User currentUser = userRepository
                .findByUsername(SecurityUtil.getCurrentUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.SECURITY_CONTEXT_ERROR));

        if (imagesRequestDtos == null || imagesRequestDtos.isEmpty()) {
            return null;
        }

        List<Image> images = new ArrayList<>();

        for (int i = 0; i < imagesRequestDtos.size(); i++) {
            images.add(
                    Image.builder()
                            .path(imagesRequestDtos.get(i).getS3URL())
                            .fileName(imagesRequestDtos.get(i).getFilename())
                            .user(currentUser)
                            .imgIdx(i)
                            .imageReference(imgRefType)
                            .refApiId(refApiId)
                            .status(Status.ACTIVE)
                            .build()
            );
        }

        images = imageRepository.saveAll(images);
        return images;
    }
//    @Transactional
//    public List<Image> saveAllReviewImage(List<ImagesRequestDto> imagesRequestDtos, String reviewApiId) {
//        User currentUser = userRepository
//            .findByUsername(SecurityUtil.getCurrentUsername())
//            .orElseThrow(() -> new BusinessException(ErrorCode.SECURITY_CONTEXT_ERROR));
//
//        List<Image> images = new ArrayList<>();
//
//        for (int i = 0; i < imagesRequestDtos.size(); i++) {
//            images.add(
//                Image.builder()
//                    .path(imagesRequestDtos.get(i).getS3URL())
//                    .fileName(imagesRequestDtos.get(i).getFilename())
//                    .user(currentUser)
//                    .imgIdx(i)
//                    .imageReference(ImageReference.REVIEW)
//                    .refApiId(reviewApiId)
//                    .status(Status.ACTIVE)
//                    .build()
//            );
//        }
//
//        images = imageRepository.saveAll(images);
//        return images;
//    }


    @Transactional
    public void deleteByRefApiId(String refApiId) {
        //  REFACTOR 유저 로그인 하는 부분 AOP로 처리해보는 방법 고민하기
        //  REFACTOR 어드민 권한이 있는 유저의 경우도 예외처리하는 AOP 고민하기
//        User currentUser = userRepository
//                .findByUsername(SecurityUtil.getCurrentUsername())
//                .orElseThrow(() -> new BusinessException(ErrorCode.SECURITY_CONTEXT_ERROR));

        List<Image> images = imageRepository.getAllByRefApiIdWithGivenStatus(refApiId, Status.ACTIVE);

        images.forEach(image -> image.setStatus(Status.DELETED));
    }

    @Transactional
    public void deleteAllByRefId(String refApiId) {
        //  REFACTOR 유저 로그인 하는 부분 AOP로 처리해보는 방법 고민하기
        //  REFACTOR 어드민 권한이 있는 유저의 경우도 예외처리하는 AOP 고민하기
//        User currentUser = userRepository
//                .findByUsername(SecurityUtil.getCurrentUsername())
//                .orElseThrow(() -> new BusinessException(ErrorCode.SECURITY_CONTEXT_ERROR));

        List<Image> images = imageRepository.getAllByRefApiIdWithGivenStatus(refApiId, Status.ACTIVE);

        images.forEach(image -> {
            //  일시적으로 유저가 작성한 이미지인지 확인하는 기능 주석
//            if (!image.getUser().equals(currentUser)) {
//                throw new BusinessException(ErrorCode.IMAGE_USER_NOT_MATCH);
//            }

            image.setStatus(Status.DELETED);
        });
    }

    public List<Image> getAllByRefId(String refApiId, ImageStatusFilter statusFilter) {
        List<Image> images;
        switch (statusFilter) {
            case ACTIVE, DELETED -> {
                images = imageRepository.getAllByRefApiIdWithGivenStatus(refApiId, statusFilter.getStatus());
                break;
            }
            default -> images = imageRepository.getAllByRefApiId(refApiId);
        }
        return images;
    }
}
