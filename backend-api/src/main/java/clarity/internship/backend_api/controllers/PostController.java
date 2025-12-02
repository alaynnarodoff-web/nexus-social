package clarity.internship.backend_api.controllers;

import clarity.internship.backend_api.data.PostRepository;
import clarity.internship.backend_api.data.UserRepository;
import clarity.internship.backend_api.data.FriendRequestRepository;
import clarity.internship.backend_api.models.Comment;
import clarity.internship.backend_api.models.FriendRequest;
import clarity.internship.backend_api.models.Post;
import clarity.internship.backend_api.models.User;
import clarity.internship.backend_api.controllers.FriendsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

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
    public Post toggleLikePost(@PathVariable String postId, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to like or unlike posts");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (post.getLikedBy().contains(username)) {

            post.getLikedBy().remove(username);
        } else {

            post.getLikedBy().add(username);
        }

        return postRepository.save(post);
    }

    @PostMapping("/posts/{postId}/comment")
    public Post addComment(@PathVariable String postId, @RequestBody Map<String, String> payload, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        String author = payload.get("author");
        String text = payload.get("text");

        if (username == null || !username.equals(author)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to comment");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User user = userRepository.findByUsername(author);

        Comment newComment = new Comment(author, text);

        if (user != null && user.getAvatar() != null) {
            newComment.setAuthorAvatar(user.getAvatar());
        }

        post.getComments().add(newComment);
        return postRepository.save(post);
    }

    @GetMapping("/posts/friends")
    public List<Post> getFriendsPosts(HttpSession session) {

        String currentUser = (String) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in.");
        }

        List<FriendRequest> friendships = friendRequestRepository
                .findByRequestingUserIdOrRequestRecipientIdAndAcceptedTrue(currentUser, currentUser);

        List<String> friendUsernames = new ArrayList<>();
        for (FriendRequest friendship : friendships) {
            if (friendship.getRequestingUserId().equals(currentUser)) {
                friendUsernames.add(friendship.getRequestRecipientId());
            } else {
                friendUsernames.add(friendship.getRequestingUserId());
            }
        }
        Sort sortByTimestampDesc = Sort.by(Sort.Direction.DESC, "timestamp");
        return postRepository.findByAuthorIdIn(friendUsernames, sortByTimestampDesc);
    }

    @GetMapping("/posts/fof")
    public List<Post> getFriendsOfFriendsPosts(HttpSession session) {
        String currentUser = (String) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in.");
        }

        Set<String> friendsOfFriends = getFriendsOfFriends(currentUser);

        if (friendsOfFriends.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<String> fofList = new ArrayList<>(friendsOfFriends);
            Sort sortByTimestampDesc = Sort.by(Sort.Direction.DESC, "timestamp");
            return postRepository.findByAuthorIdIn(fofList, sortByTimestampDesc);
        }
    }

    @GetMapping("/fof/{userId}")
    public List<User> getFriendsOfFriends(@PathVariable String userId, HttpSession session) {
        String currentUser = (String) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in.");
        }

        Set<String> friendsOfFriends = getFriendsOfFriends(userId);

        if (friendsOfFriends.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<User> fofList = new ArrayList<>();
            for (String fofUsername : friendsOfFriends) {
                User user = userRepository.findByUsername(fofUsername);
                if (user != null) {
                    fofList.add(user);
                }
            }
            return fofList;
        }
    }

    public Set<String> getFriendsOfFriends(String currentUser) {
        Set<String> directFriends = getDirectFriends(currentUser);

        Set<String> friendsOfFriends = new HashSet<>();
        if (!directFriends.isEmpty()) {
            for (String friend : directFriends) {

                Set<String> friendsOfFriend = getDirectFriends(friend);
                for (String fof : friendsOfFriend) {
                    if (!fof.equals(currentUser) && !directFriends.contains(fof)) {
                        friendsOfFriends.add(fof);
                    }
                }

            }
        }
        return friendsOfFriends;
    }

    public Set<String> getDirectFriends(String username) {

        List<FriendRequest> sentF1 = friendRequestRepository.findByRequestingUserIdAndAcceptedTrue(username);
        List<FriendRequest> receivedF1 = friendRequestRepository.findByRequestRecipientIdAndAcceptedTrue(username);
        Set<String> directFriends = new HashSet<>();
        sentF1.forEach(fr -> directFriends.add(fr.getRequestRecipientId()));
        receivedF1.forEach(fr -> directFriends.add(fr.getRequestingUserId()));
        return directFriends;
    }

}
