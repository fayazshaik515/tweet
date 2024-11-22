// package com.auth.service;

// import com.auth.model.Tweet;
// import com.auth.model.User;
// import com.auth.repository.TweetRepository;
// import com.auth.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.stereotype.Service;
// import java.time.LocalDateTime;

// @Service
// public class TweetService {
//     @Autowired
//     private TweetRepository tweetRepository;

//     @Autowired
//     private UserRepository userRepository;

//     public Tweet createTweet(String content, String username) {
//         if (content == null || content.trim().isEmpty()) {
//             throw new IllegalArgumentException("Tweet content cannot be empty");
//         }
//         if (content.length() > 280) {
//             throw new IllegalArgumentException("Tweet content cannot exceed 280 characters");
//         }

//         User user = userRepository.findByUsername(username)
//             .orElseThrow(() -> new RuntimeException("User not found"));

//         Tweet tweet = new Tweet();
//         tweet.setContent(content.trim());
//         tweet.setTimestamp(LocalDateTime.now());
//         tweet.setUser(user);

//         return tweetRepository.save(tweet);
//     }


//     public Page<Tweet> getTweets(int page, int size) {
//         if (size >20) {
//             size = 10; // Limit maximum page size
//         }
//         return tweetRepository.findAllByOrderByTimestampDesc(
//             PageRequest.of(page, size)
//         );
//     }

//     public Page<Tweet> getUserTweets(String username, int page, int size) {
//         System.out.println("Fetching from database for username: " + username + ", page: " + page + ", size: " + size);

//         User user = userRepository.findByUsername(username)
//             .orElseThrow(() -> new RuntimeException("User not found"));
            
//         if (size > 20) {
//             size = 10; 
//         }   
//         return tweetRepository.findByUserOrderByTimestampDesc(
//             user,
//             PageRequest.of(page, size)
//         );
//     }
// }
package com.auth.service;

import com.auth.model.Tweet;
import com.auth.model.User;
import com.auth.repository.TweetRepository;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TweetService {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    private final BinarySearchTree<User> userCache = new BinarySearchTree<>();

    @PostConstruct
    public void initializeCache() {
        refreshUserCache(); // Load the cache after dependencies are injected
    }
//tweet creation
    public Tweet createTweet(String content, String username) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Tweet content cannot be empty");
        }
        if (content.length() > 280) {
            throw new IllegalArgumentException("Tweet content cannot exceed 280 characters");
        }

        User user = findUserWithTweets(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Tweet tweet = new Tweet();
        tweet.setContent(content.trim());
        tweet.setTimestamp(LocalDateTime.now());
        tweet.setUser(user);

        return tweetRepository.save(tweet);
    }



    public Page<Tweet> getTweets(int page, int size) {
        if (size > 20) {
            size = 10; // Limit maximum page size
        }
        return tweetRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }

    
    public Page<Tweet> getUserTweets(String username, int page, int size) {
        System.out.println("Fetching tweets for username: " + username + ", page: " + page + ", size: " + size);

        User user = findUserWithTweets(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (size > 20) {
            size = 10;
        }

        return tweetRepository.findByUserOrderByTimestampDesc(user, PageRequest.of(page, size));
    }



    public synchronized User findUserWithTweets(String username) {
        User searchUser = new User();
        searchUser.setUsername(username);

        User foundUser = userCache.search(searchUser);
        if (foundUser != null) {
            foundUser.getTweets().size(); // Ensure tweets are initialized
            return foundUser; // Return user with tweets
        }
        return null; // Return null if not found
    }

 // Cache management logic
 @Scheduled(fixedRate = 60000) // Refresh cache every 1 minute
 public synchronized void refreshUserCache() {
     List<User> users = userRepository.findAll(); // Fetch all users from the database
     userCache.clear(); // Clear the existing cache
     for (User user : users) {
         if (user.getUsername() != null) {
             user.getTweets().size(); // Load the tweets (trigger lazy loading)
             userCache.insert(user); // Add the user with tweets to the BST
         } else {
             System.err.println("Skipped user with null username: " + user);
         }
     }
     System.out.println("User cache refreshed at " + LocalDateTime.now());
 }
}