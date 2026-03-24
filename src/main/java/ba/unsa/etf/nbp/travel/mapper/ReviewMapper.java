package ba.unsa.etf.nbp.travel.mapper;

import ba.unsa.etf.nbp.travel.dto.response.ReviewResponse;
import ba.unsa.etf.nbp.travel.model.entity.ReviewEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReviewMapper {

    public static ReviewResponse toResponse(ReviewEntity entity, String username) {
        return new ReviewResponse(
                entity.getId(),
                entity.getUserId(),
                username,
                entity.getBookingId(),
                entity.getRating(),
                entity.getComment(),
                entity.getReviewDate()
        );
    }
}
