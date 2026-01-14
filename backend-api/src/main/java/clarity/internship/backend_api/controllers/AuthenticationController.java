package clarity.internship.backend_api.controllers;

import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import clarity.internship.backend_api.data.UserRepository;
import clarity.internship.backend_api.models.User;
import jakarta.servlet.http.HttpSession;

@RestController
public class AuthenticationController {
    @Autowired
    HttpSession session;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/user")
    public String createUser(@RequestBody User user) {
        try {
            user.setPassword(hashPassword(user.getPassword()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SHA-256 Algorithm not Found");
        }
        if (userRepository.findOneByUsername(user.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        } else {
            userRepository.save(user);
        }
        return "Congratulations, you have created an account with the username " + user.getUsername();
    }

    @PostMapping("/login")
    public User login(@RequestBody User user) {
        try {
            user.setPassword(hashPassword(user.getPassword()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SHA-256 Algorithm not Found");
        }
        User existingUser = userRepository.findOneByUsername(user.getUsername());
        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password does not exist");
        }
        if (existingUser.getUsername().equals(user.getUsername())
                && existingUser.getPassword().equals(user.getPassword()))

        {
            session.setAttribute("loggedInUser", user.getUsername());
            return user;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Username or Password");
        }
    }

    @GetMapping("/getUser")
    public User getUser() {
        Object loggedIn = session.getAttribute("loggedInUser");

        if (loggedIn == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No user is currently logged in");
        }

        String username = loggedIn.toString();
        User existingUser = userRepository.findOneByUsername(username);

        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User from session not found");
        }

        return existingUser;
    }

    @PutMapping("/updateUser")
    public String updateUser(@RequestBody User user) {
        String username = (String) session.getAttribute("loggedInUser");

        if (username == null || !username.equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You can only update your own account");
        }

        User existingUser = userRepository.findOneByUsername(user.getUsername());

        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setBio(user.getBio());

        userRepository.save(existingUser);
        return "User updated successfully";
    }

    @PostMapping("/logout")
    public String logout() {
        session.invalidate();
        return "User is logged out successfully.";
    }

    private String hashPassword(String password) {
        if (password == null) {
            password = "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            return new String(hashedPassword, "UTF-8");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Password hashing failed");
        }
    }

    @GetMapping("/users/search")
    public List<User> searchUsers(@RequestParam("q") String query) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query,
                        query, query);
    }

    @GetMapping("/users/{username}")
    public User getUserByUsername(@PathVariable String username) {
        User user = userRepository.findOneByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

}
