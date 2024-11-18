import React, { forwardRef } from "react";
import "./Tweet.css";

function formatTimestamp(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleString();
}

const Tweet = forwardRef(({ tweet }, ref) => (
  <div className="tweet" ref={ref}>
    <div className="tweet-header">
      <span className="username">{tweet.user.username}</span>
      <span className="timestamp">{formatTimestamp(tweet.timestamp)}</span>
    </div>
    <div className="tweet-content">{tweet.content}</div>
  </div>
));

export default Tweet;
