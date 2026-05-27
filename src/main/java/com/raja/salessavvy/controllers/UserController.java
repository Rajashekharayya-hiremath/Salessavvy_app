package com.raja.salessavvy.controllers;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation. PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.raja.salessavvy.entities. User;
import com.raja.salessavvy.services.UserService;
@RestController
@RequestMapping("/api/users")
public class UserController {
private UserService userService;

public UserController (UserService userService) {
this.userService = userService;
}
@PostMapping("/register")

public ResponseEntity registerUser(@RequestBody User user) {
User registeredUser = userService.registerUser(user);
try {
 return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", registeredUser));
}
catch(Exception e) {
return ResponseEntity.badRequest().body (Map.of("error", e.getMessage()));
}
}
}