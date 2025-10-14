package clarity.internship.backend_api.controllers;

import clarity.internship.backend_api.data.FriendRequestRepository;
import clarity.internship.backend_api.models.FriendRequest;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

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

        List<FriendRequest> existingFriendship = friendRequestRepository
                .findByRequestingUserIdOrRequestRecipientIdAndAcceptedTrue(fromUser, toUser);
        boolean alreadyFriends = existingFriendship.stream()
                .anyMatch(fr -> (fr.getRequestingUserId().equals(fromUser) && fr.getRequestRecipientId().equals(toUser))
                        ||
                        (fr.getRequestingUserId().equals(toUser) && fr.getRequestRecipientId().equals(fromUser)));

        if (alreadyFriends) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already friends.");
        }

        List<FriendRequest> existing = friendRequestRepository
                .findByRequestingUserIdAndRequestRecipientId(fromUser, toUser);
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already sent.");
        }

        List<FriendRequest> reverseRequests = friendRequestRepository
                .findByRequestingUserIdAndRequestRecipientId(toUser, fromUser);
        if (!reverseRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already sent you a request.");
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
    public int getFriendCount() {
        String user = (String) session.getAttribute("loggedInUser");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        List<FriendRequest> accepted = friendRequestRepository
                .findByRequestingUserIdOrRequestRecipientIdAndAcceptedTrue(user, user);

        return (int) accepted.stream()
                .filter(fr -> fr.isAccepted() &&
                        (fr.getRequestingUserId().equals(user) || fr.getRequestRecipientId().equals(user)))
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
        if (accept) {
            fr.setAccepted(true);
        } else {
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

        return friendRequestRepository.findByRequestingUserIdOrRequestRecipientIdAndAcceptedTrue(user, user);
    }

    @DeleteMapping("/{friendUsername}")
    public String unfriend(@PathVariable String friendUsername) {
        String user = (String) session.getAttribute("loggedInUser");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        List<FriendRequest> requests = friendRequestRepository.findByRequestingUserIdAndRequestRecipientId(user,
                friendUsername);
        requests.addAll(friendRequestRepository.findByRequestingUserIdAndRequestRecipientId(friendUsername, user));

        if (requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friendship found.");
        }

        friendRequestRepository.deleteAll(requests);
        return "Unfriended " + friendUsername;
    }

    @GetMapping("/areFriends")
    public boolean areFriends(@RequestParam String user1, @RequestParam String user2) {

        List<FriendRequest> fromUser1 = friendRequestRepository
                .findByAcceptedTrueAndRequestingUserIdAndRequestRecipientId(user1, user2);

        List<FriendRequest> fromUser2 = friendRequestRepository
                .findByAcceptedTrueAndRequestingUserIdAndRequestRecipientId(user2, user1);

        return !fromUser1.isEmpty() || !fromUser2.isEmpty();
    }

}
