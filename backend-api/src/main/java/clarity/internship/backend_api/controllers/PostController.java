package clarity.internship.backend_api.controllers;

import clarity.internship.backend_api.data.PostRepository;
import clarity.internship.backend_api.data.UserRepository;
import clarity.internship.backend_api.models.Post;
import clarity.internship.backend_api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/posts")
    public Post createPost(
            @RequestParam("authorId") String authorId,
            @RequestParam("content") String content,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);

        // ✅ Set avatar from user account (already base64-encoded)
        User user = userRepository.findByUsername(authorId);
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            post.setAuthorAvatar(user.getAvatar());
        }

        // ✅ Encode image file to base64 string
        if (imageFile != null && !imageFile.isEmpty()) {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            post.setImageBase64(base64Image);
        }

        return postRepository.save(post);
    }

    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @GetMapping("/posts/user/{authorId}")
    public List<Post> getPostsByUser(@PathVariable String authorId) {
        return postRepository.findByAuthorId(authorId);
    }
}
