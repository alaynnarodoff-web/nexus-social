package clarity.internship.backend_api.controllers;

import clarity.internship.backend_api.data.PostRepository;
import clarity.internship.backend_api.data.UserRepository;
import clarity.internship.backend_api.models.Post;
import clarity.internship.backend_api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

        User user = userRepository.findByUsername(authorId);
        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            post.setAuthorAvatar(user.getAvatar());
        }

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

    @PostMapping("/posts/{postId}/like")
    public Post likePost(@PathVariable String postId, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");

        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to like posts");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getLikedBy().contains(username)) {
            post.getLikedBy().add(username);
            return postRepository.save(post);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already liked this post");
        }
    }

}
