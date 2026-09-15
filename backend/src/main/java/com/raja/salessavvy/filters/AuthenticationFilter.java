package com.raja.salessavvy.filters;

import com.raja.salessavvy.entities.Role;
import com.raja.salessavvy.entities.User;
import com.raja.salessavvy.repositories.UserRepository;
import com.raja.salessavvy.services.AuthService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter implements Filter {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${FRONTEND_URL:http://localhost:5174}")
    private String frontendUrl;

    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/users/register",
            "/api/auth/login"
    };

    public AuthenticationFilter(AuthService authService,
                                UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        System.out.println("Authentication Filter Started");
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Add CORS headers before any authentication logic
        String origin = httpRequest.getHeader("Origin");

        if (frontendUrl.equals(origin) || "http://localhost:5174".equals(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS"
            );
            httpResponse.setHeader(
                    "Access-Control-Allow-Headers",
                    "Content-Type, Authorization"
            );
        }

        // Handle CORS preflight immediately
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        System.out.println("FILTER EXECUTED");

        String requestURI = httpRequest.getRequestURI();

        System.out.println("\n==============================");
        System.out.println("REQUEST URI = " + requestURI);
        System.out.println("==============================");

        // Public endpoints
        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            System.out.println("PUBLIC ENDPOINT");
            chain.doFilter(request, response);
            return;
        }

        // Get JWT from cookie
        String token = getAuthToken(httpRequest);
        System.out.println("TOKEN = " + token);

        if (token == null) {
            System.out.println("TOKEN NOT FOUND");

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: Token missing");
            return;
        }

        // Validate JWT
        boolean valid = authService.validateToken(token);

        System.out.println("TOKEN VALID = " + valid);

        if (!valid) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: Invalid token");
            return;
        }

        String username;

        try {
            username = authService.extractUsername(token);
            System.out.println("JWT USERNAME = " + username);
        } catch (Exception e) {

            System.out.println("JWT PARSE ERROR = " + e.getMessage());

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: JWT parse failed");
            return;
        }

        // Find authenticated user
        Optional<User> userOptional =
                userRepository.findByUsername(username);

        System.out.println("USER FOUND = " + userOptional.isPresent());

        if (userOptional.isEmpty()) {

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: User not found");
            return;
        }

        User authenticatedUser = userOptional.get();

        System.out.println("AUTH USER = "
                + authenticatedUser.getUsername());

        System.out.println("ROLE = "
                + authenticatedUser.getRole());

        // Admin authorization
        if (requestURI.startsWith("/admin/")
                && authenticatedUser.getRole() != Role.ADMIN) {

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter()
                    .write("Forbidden: Admin access required");
            return;
        }

        // Attach authenticated user to request
        httpRequest.setAttribute(
                "authenticatedUser",
                authenticatedUser
        );

        System.out.println("ATTRIBUTE ATTACHED SUCCESSFULLY");

        chain.doFilter(request, response);
    }

    private String getAuthToken(HttpServletRequest request) {

        // First try Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null &&
                authorizationHeader.startsWith("Bearer ")) {

            return authorizationHeader.substring(7);
        }

        // Fallback to cookie
        return getAuthTokenFromCookies(request);
    }
    
    private String getAuthTokenFromCookies(
            HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {

            System.out.println("COOKIES FOUND:");

            for (Cookie cookie : cookies) {
                System.out.println(
                        cookie.getName() + " = " + cookie.getValue()
                );
            }

            return Arrays.stream(cookies)
                    .filter(cookie ->
                            "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        System.out.println("NO COOKIES FOUND");
        return null;
    }
}