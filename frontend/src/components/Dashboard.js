import React, { useState, useEffect, useCallback, useRef } from "react";
import Tweet from "./Tweet";
import TweetForm from "./TweetForm";
import "./Dashboard.css";
import DoublyLinkedList from "./DoublyLinkedList";

function Dashboard() {
  const [tweetsList] = useState(new DoublyLinkedList());
  const [tweets, setTweets] = useState([]); // Stores tweets in an array (used for displaying in the UI).
  const [isLoading, setIsLoading] = useState(false); // Tells if tweets are being fetched from the server.
  const [error, setError] = useState("");
  const [hasMore, setHasMore] = useState(true); // Checks if there are more tweets to load.
  const observer = useRef(); // A useRef hook that holds an IntersectionObserver instance for infinite scrolling.

  const loadTweets = useCallback(async () => {
    if (!hasMore || isLoading) return; // Prevents loading if there are no more tweets or currently loading.

    setIsLoading(true);
    setError(""); // Reset the error message.

    // Makes an API call to fetch the next page of tweets:
    try {
      const token = localStorage.getItem("token");
      const page = Math.floor(tweetsList.toArray().length / 10); // Calculate the page number based on the tweet list length.
      const response = await fetch(
        `http://localhost:8080/api/tweets?page=${page}&size=10`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      // Checks the API response. If it’s not 200 OK, an error is thrown.
      if (!response.ok) {
        throw new Error(`Failed to fetch tweets: ${response.status}`);
      }
      const data = await response.json();

      if (!data || !data.content || data.content.length === 0) {
        setHasMore(false); // No more tweets to load.
        return;
      }

      // Append new tweets to the linked list
      data.content.forEach((tweet) => tweetsList.append(tweet)); // Appends tweets to the linked list.

      // Updates the state tweets with the array form of tweetsList.
      setTweets([...tweetsList.toArray()]); // Ensures the state is updated immutably.
    } catch (err) {
      setError("Failed to load tweets. Please try again later.");
      console.error("Error loading tweets:", err); // Log the error for debugging purposes.
    } finally {
      setIsLoading(false); // Set loading state back to false after fetching.
    }
  }, [tweetsList, isLoading, hasMore]); // Dependencies: tweetsList, isLoading, hasMore.

  useEffect(() => {
    loadTweets(); // Calls loadTweets when the component mounts.
  }, [loadTweets]); // Re-runs the effect if loadTweets changes.

  const lastTweetRef = useCallback(
    (node) => {
      if (isLoading) return; // Prevents observer when loading.

      if (observer.current) observer.current.disconnect(); // Disconnect the previous observer.
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          loadTweets(); // Loads more tweets when the last tweet becomes visible.
        }
      });

      if (node) observer.current.observe(node); // Start observing the node (last tweet).
    },
    [isLoading, loadTweets, hasMore] // Dependencies: isLoading, loadTweets, hasMore.
  );

  const handleTweetPosted = () => {
    tweetsList.clear(); // Clears the linked list when a new tweet is posted.
    setHasMore(true); // Resets the hasMore flag to true.
    loadTweets(); // Reloads the tweets.
  };

  return (
    <div className="dashboard">
      <div className="dashboard-content">
        <TweetForm onTweetPosted={handleTweetPosted} /> {/* Handles new tweet posting */}

        {error && <div className="error-message">{error}</div>} {/* Displays error message if any. */}

        <div className="tweet-list">
          {tweets.length === 0 && !isLoading ? (
            <div className="no-tweets">No tweets yet. Be the first to tweet!</div> // If no tweets available.
          ) : (
            tweets.map((tweet, index) => {
              if (tweets.length === index + 1) {
                // If this is the last tweet, add the ref to trigger infinite scroll.
                return <Tweet key={tweet.id} tweet={tweet} ref={lastTweetRef} />;
              }
              return <Tweet key={tweet.id} tweet={tweet} />;
            })
          )}
        </div>

        {isLoading && <div className="loading">Loading tweets...</div>} {/* Displays loading indicator. */}
      </div>
    </div>
  );
}

export default Dashboard;
