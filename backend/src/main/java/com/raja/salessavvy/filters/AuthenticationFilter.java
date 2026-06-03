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
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter implements Filter {

    private final AuthService authService;
    private final UserRepository userRepository;

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
    	System.out.println("FILTER EXECUTED");
    	

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        System.out.println("\n==============================");
        System.out.println("REQUEST URI = " + requestURI);
        System.out.println("==============================");

        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            System.out.println("PUBLIC ENDPOINT");
            chain.doFilter(request, response);
            return;
        }

        String token = getAuthTokenFromCookies(httpRequest);

        System.out.println("TOKEN = " + token);

        if (token == null) {
            System.out.println("TOKEN NOT FOUND");

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: Token missing");
            return;
        }

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

        if (requestURI.startsWith("/admin/")
                && authenticatedUser.getRole() != Role.ADMIN) {

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter()
                    .write("Forbidden: Admin access required");
            return;
        }

        httpRequest.setAttribute(
                "authenticatedUser",
                authenticatedUser
        );

        System.out.println("ATTRIBUTE ATTACHED SUCCESSFULLY");

        chain.doFilter(request, response);
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