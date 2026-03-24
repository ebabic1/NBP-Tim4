package ba.unsa.etf.nbp.travel.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {

    private Long id;
    private Long userId;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
}
