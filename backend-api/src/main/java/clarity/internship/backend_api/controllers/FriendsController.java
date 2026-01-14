package clarity.internship.backend_api.controllers;

import java.time.Instant;
import java.util.ArrayList;
// import java.util.HashSet;
import java.util.List;
// import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import clarity.internship.backend_api.data.FriendRequestRepository;
import clarity.internship.backend_api.models.FriendRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/friends")
public class FriendsController {

    @Autowired
    private HttpSession session;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @PostMapping("/request")
    public String sendFriendRequest(@RequestParam String toUser) {
        String fromUser = (String) session.getAttribute("loggedInUser");
        if (fromUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in.");
        }

        if (fromUser.equals(toUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot friend yourself.");
        }

        if (areFriends(fromUser, toUser)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already friends.");
        }

        List<FriendRequest> existing = friendRequestRepository.findByRequestingUserIdAndRequestRecipientId(fromUser,
                toUser);
        List<FriendRequest> inverse = friendRequestRepository.findByRequestingUserIdAndRequestRecipientId(toUser,
                fromUser);

        if (!existing.isEmpty() || !inverse.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A friend request is already pending.");
        }

        FriendRequest fr = new FriendRequest();
        fr.setRequestingUserId(fromUser);
        fr.setRequestRecipientId(toUser);
        fr.setRequestTimestamp(Instant.now().toString());
        fr.setAccepted(false);
        fr.setRejected(false);

        friendRequestRepository.save(fr);
        return "Friend request sent to " + toUser;
    }

    @GetMapping("/count")
    public long getFriendCount(@RequestParam(required = false) String username) {
        String targetUser = (username != null && !username.isEmpty()) ? username
                : (String) session.getAttribute("loggedInUser");

        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not specified.");
        }

        List<FriendRequest> sent = friendRequestRepository.findByRequestingUserIdAndAcceptedTrue(targetUser);
        List<FriendRequest> received = friendRequestRepository.findByRequestRecipientIdAndAcceptedTrue(targetUser);

        return Stream.concat(sent.stream(), received.stream())
                .map(fr -> fr.getRequestingUserId().equals(targetUser) ? fr.getRequestRecipientId()
                        : fr.getRequestingUserId())
                .distinct()
                .count();
    }

    @GetMapping("/requests")
    public List<FriendRequest> getPendingRequests() {
        String currentUser = (String) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }
        return friendRequestRepository.findByRequestRecipientIdAndAcceptedFalseAndRejectedFalse(currentUser);
    }

    @PostMapping("/respond")
    public String respondToFriendRequest(@RequestParam String fromUser, @RequestParam boolean accept) {
        String currentUser = (String) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        List<FriendRequest> requests = friendRequestRepository.findByRequestingUserIdAndRequestRecipientId(fromUser,
                currentUser);
        if (requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found.");
        }

        FriendRequest fr = requests.get(0);

        if (fr.isAccepted() || fr.isRejected()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request already responded to.");
        }

        if (accept) {
            fr.setAccepted(true);
            fr.setRejected(false);
        } else {
            fr.setAccepted(false);
            fr.setRejected(true);
        }
        friendRequestRepository.save(fr);
        return accept ? "Friend request accepted." : "Friend request rejected.";
    }

    @GetMapping
    public List<FriendRequest> getConfirmedFriends() {
        String user = (String) session.getAttribute("loggedInUser");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        List<FriendRequest> sentAndAccepted = friendRequestRepository
                .findByRequestingUserIdAndAcceptedTrue(user);
        List<FriendRequest> receivedAndAccepted = friendRequestRepository
                .findByRequestRecipientIdAndAcceptedTrue(user);

        List<FriendRequest> allFriendships = new ArrayList<>(sentAndAccepted);
        allFriendships.addAll(receivedAndAccepted);

        return allFriendships.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{friendUsername}")
    public String unfriend(@PathVariable String friendUsername) {
        String user = (String) session.getAttribute("loggedInUser");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        List<FriendRequest> requestsAsSender = friendRequestRepository.findByRequestingUserIdAndRequestRecipientId(user,
                friendUsername);
        List<FriendRequest> requestsAsReceiver = friendRequestRepository
                .findByRequestingUserIdAndRequestRecipientId(friendUsername, user);

        List<FriendRequest> friendshipsToDelete = new ArrayList<>(requestsAsSender);
        friendshipsToDelete.addAll(requestsAsReceiver);

        if (friendshipsToDelete.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friendship found.");
        }

        friendRequestRepository.deleteAll(friendshipsToDelete);
        return "Unfriended " + friendUsername;
    }

    @GetMapping("/areFriends")
    public boolean areFriends(@RequestParam String user1, @RequestParam String user2) {
        List<FriendRequest> from1 = friendRequestRepository
                .findByAcceptedTrueAndRequestingUserIdAndRequestRecipientId(user1, user2);
        List<FriendRequest> from2 = friendRequestRepository
                .findByAcceptedTrueAndRequestingUserIdAndRequestRecipientId(user2, user1);
        return !from1.isEmpty() || !from2.isEmpty();
    }

}
