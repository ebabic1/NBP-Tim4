package ba.unsa.etf.nbp.travel.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_activities")
public class UserActivity {

    @Id
    private String id;

    private Long userId;

    private String username;

    private String action;

    @Indexed
    private LocalDateTime timestamp;
}
