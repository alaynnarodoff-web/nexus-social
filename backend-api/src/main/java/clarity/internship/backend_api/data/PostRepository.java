package clarity.internship.backend_api.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import clarity.internship.backend_api.models.Post;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByAuthorId(String authorId);
}