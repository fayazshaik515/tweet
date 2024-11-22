package com.auth.controller;

import com.auth.model.Tweet;
import com.auth.model.User;
import com.auth.service.TweetService;
// import com.auth.service.UserCacheService;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tweets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TweetController {
    
    @Autowired
    private TweetService tweetService;
    // @Autowired
    // private UserCacheService userCacheService;


    @Data
    public static class TweetRequest {
        private String content;
    }

    @PostMapping
    public ResponseEntity<?> createTweet( @RequestBody TweetRequest request,
    // information about the currently authenticated (logged-in) user.
     @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Tweet tweet = tweetService.createTweet(
                request.getContent(),
                userDetails.getUsername()
            );
            return ResponseEntity.ok(tweet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to create tweet"));
        }
    }

    @GetMapping
    public ResponseEntity<Page<Tweet>> getTweets(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(tweetService.getTweets(page, size));
    }


@GetMapping("/users/{username}")
public ResponseEntity<?> getUserWithTweets(@PathVariable String username) {
    User user = tweetService.findUserWithTweets(username);
    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
    }
    return ResponseEntity.ok(user); // Return user with tweets
}
}
