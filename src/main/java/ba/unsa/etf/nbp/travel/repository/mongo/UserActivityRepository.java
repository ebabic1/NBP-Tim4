package ba.unsa.etf.nbp.travel.repository.mongo;

import ba.unsa.etf.nbp.travel.model.document.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends MongoRepository<UserActivity, String> {

    List<UserActivity> findByUserIdOrderByTimestampDesc(Long userId);
}
