package com.raja.salessavvy.controllers;

import com.raja.salessavvy.entities.*;
import com.raja.salessavvy.services.*;
import jakarta.servlet.http.HttpServletRequest;
import com.raja.salessavvy.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http. ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {
@Autowired
private CartService cartService;
@Autowired
private UserRepository userRepository;
// Fetch userld from username coming from the filter and get cart item count

@GetMapping("/items/count")
public ResponseEntity<Integer> getCartItemCount(@RequestParam String username) {

// Fetch user by username to get the userld
User user = userRepository.findByUsername (username).orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
// Call the service to get the total cart item count
int count = cartService.getCartItemCount(user.getUserId());
return ResponseEntity.ok(count);
}

//Fetch all cart items for the user (based on username)
@GetMapping("/items") public ResponseEntity<Map<String, Object>> getCartitems(HttpServletRequest request) {
//Fetch user by username to get the userld
User user= (User) request.getAttribute("authenticatedUser");
//User user = userRepository.findByUsername(un)
//.orElse Throw(() -> new IllegalArgumentException("User not found with username: " + username));
//Call the service to get cart items for the user
Map<String, Object> cartitems = cartService.getCartItems(user.getUserId());
return ResponseEntity.ok(cartitems);
}
//Add an item to the cart
@PostMapping("/add")
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
public ResponseEntity<Void> addToCart(@RequestBody Map<String, Object> request) {

String username = (String) request.get("username");
int productId = (int) request.get("productid");
//Handle quantity: Default to 1 if not provided
int quantity = request.containsKey("quantity") ? (int) request.get("quantity"): 1;
//Fetch the user using username
User user = userRepository.findByUsername (username).orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
//Add the product to the cart
cartService.addToCart(user.getUserId(), productId, quantity); 
return ResponseEntity.status(HttpStatus.CREATED).build();
}

//Update Cart Item Quantity
@PutMapping("/update")
public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request) {

String username = (String) request.get("username");
int productId = (int) request.get("productId");
int quantity = (int) request.get("quantity");
//Fetch the user using username
User user = userRepository.findByUsername (username).orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
//Update the cart item quantity
cartService.updateCartItemQuantity(user.getUserId(), productId, quantity);
return ResponseEntity.status(HttpStatus.OK).build();
}
//Delete Cart Item
@DeleteMapping("/delete")
public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request) {
String username = (String) request.get("username");
int productId = (int) request.get("productId");
//Fetch the user using username
User user = userRepository.findByUsername (username).orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
//Delete the cart item
cartService.deleteCartItem(user.getUserId(), productId); 
return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}
}