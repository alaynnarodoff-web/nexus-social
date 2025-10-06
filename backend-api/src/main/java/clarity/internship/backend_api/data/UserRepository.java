package clarity.internship.backend_api.data;

import org.springframework.data.mongodb.repository.MongoRepository;

import clarity.internship.backend_api.models.User;

public interface UserRepository extends MongoRepository<User, String> {
    public User findOneByUsername(String username);

    User findByUsername(String username);

}