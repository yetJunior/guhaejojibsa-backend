/**
 * @project backend
 * @author ARA
 * @since 2023-09-07 AM 9:11
 */

package mutsa.common.repository.image;

import mutsa.common.domain.models.Status;
import mutsa.common.domain.models.image.Image;
import mutsa.common.domain.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> getAllByRefApiId(String refApiId);

    @Query("SELECT img FROM Image AS img WHERE img.refApiId = :refApiId AND img.status = :status")
    List<Image> getAllByRefApiIdWithGivenStatus(@Param("refApiId")String refApiId, @Param("status")Status status);

    List<Image> getAllByUser(User user);

    Optional<Image> getByFileName(String filename);
}
