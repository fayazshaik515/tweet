import React, { useState, useEffect, useCallback, useRef } from "react";
import Tweet from "./Tweet";
import TweetForm from "./TweetForm";
import "./Dashboard.css";
import DoublyLinkedList from "./DoublyLinkedList";

function Dashboard() {
  const [tweetsList] = useState(new DoublyLinkedList());
  const [tweets, setTweets] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [hasMore, setHasMore] = useState(true);
  const observer = useRef();

  const loadTweets = useCallback(async () => {
    if (!hasMore || isLoading) return;

    setIsLoading(true);
    setError("");

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/tweets?page=${tweetsList.toArray().length / 10}&size=10`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch tweets");
      }

      // Debug: Log raw response to identify issues
      const responseText = await response.text();
      console.log("Raw Response:", responseText);

      const data = JSON.parse(responseText); // Parse response manually
      if (!data || !data.content || data.content.length === 0) {
        setHasMore(false);
        return;
      }

      // Append new tweets to the linked list
      data.content.forEach((tweet) => tweetsList.append(tweet));
      setTweets(tweetsList.toArray());
    } catch (err) {
      setError("Failed to load tweets. Please try again later.");
      console.error("Error loading tweets:", err);
    } finally {
      setIsLoading(false);
    }
  }, [tweetsList, isLoading, hasMore]);

  useEffect(() => {
    loadTweets();
  }, [loadTweets]);

  const lastTweetRef = useCallback(
    (node) => {
      if (isLoading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          loadTweets();
        }
      });
      if (node) observer.current.observe(node);
    },
    [isLoading, loadTweets, hasMore]
  );

  const handleTweetPosted = () => {
    tweetsList.clear();
    setHasMore(true);
    loadTweets();
  };

  return (
    <div className="dashboard">
      <div className="dashboard-content">
        <TweetForm onTweetPosted={handleTweetPosted} />

        {error && <div className="error-message">{error}</div>}

        <div className="tweet-list">
          {tweets.length === 0 && !isLoading ? (
            <div className="no-tweets">
              No tweets yet. Be the first to tweet!
            </div>
          ) : (
            tweets.map((tweet, index) => {
              if (tweets.length === index + 1) {
                return (
                  <Tweet key={tweet.id} tweet={tweet} ref={lastTweetRef} />
                );
              }
              return <Tweet key={tweet.id} tweet={tweet} />;
            })
          )}
        </div>

        {isLoading && <div className="loading">Loading tweets...</div>}
      </div>
    </div>
  );
}

export default Dashboard;
