package clarity.internship.backend_api.data;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import clarity.internship.backend_api.models.Post;
import java.util.List;
import org.springframework.data.mongodb.repository.Query;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByAuthorId(String authorId);

    List<Post> findByAuthorIdIn(List<String> authorIds, Sort sort);

    List<Post> findAllByOrderByTimestampDesc();

    List<Post> findByTopics(String topic, Sort sort);

    @Query("{ '$or': [ { 'likedBy': ?0 }, { 'comments.author': ?0 } ] }")
    List<Post> findPostsInteractedBy(String username);
}