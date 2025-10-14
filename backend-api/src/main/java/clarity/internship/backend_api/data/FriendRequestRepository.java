package clarity.internship.backend_api.data;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import clarity.internship.backend_api.models.FriendRequest;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    List<FriendRequest> findByRequestRecipientIdAndAcceptedFalseAndRejectedFalse(String requestRecipientId);

    List<FriendRequest> findByAcceptedTrueAndRequestingUserIdAndRequestRecipientId(String user1, String user2);

    List<FriendRequest> findByRequestingUserIdOrRequestRecipientIdAndAcceptedTrue(String user1, String user2);

    List<FriendRequest> findByRequestingUserIdAndRequestRecipientId(String from, String to);
}
