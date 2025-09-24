package clarity.internship.backend_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import clarity.internship.backend_api.data.UserRepository;
import clarity.internship.backend_api.models.User;
import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;

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
    public String login(@RequestBody User user) {
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
            return "Login Successful for " + user.getUsername();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Username or Password");
        }
    }

    @GetMapping("/getLoginUser")
    public String getLoginUser() {
        Object loggedIn = session.getAttribute("loggedInUser");
        if (loggedIn == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No user is logged in.");
        }
        return "Currently logged in user: " + loggedIn.toString();
    }

    @DeleteMapping("/logout")
    public String logout() {
        session.invalidate();
        return "User is logged out successfully.";
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            return new String(hashedPassword, "UTF-8");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "SHA-256 Algorithm not Found");
        }
    }
}
