package clarity.internship.backend_api.controllers;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import clarity.internship.backend_api.data.PostRepository;
import clarity.internship.backend_api.models.Post;

@RestController
public class TopicController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/users/{username}/interests")
    public List<Map<String, Object>> getUserInterests(@PathVariable String username) {
        List<Post> posts = postRepository.findPostsInteractedBy(username);

        Map<String, Integer> frequencyMap = new HashMap<>();

        for (Post post : posts) {
            if (post.getTopics() != null) {
                for (String topic : post.getTopics()) {
                    String key = topic.toLowerCase();
                    frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1);
                }
            }
        }

        return frequencyMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("topic", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .sorted((a, b) -> ((Integer) b.get("count")).compareTo((Integer) a.get("count")))

                .collect(Collectors.toList());
    }
}