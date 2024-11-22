package com.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements Comparable<User> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference  // Serialize this side of the relationship
    private List<Tweet> tweets;


    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    @Override
    public int compareTo(User o) {
        if (this.username == null && o.username == null) {
            return 0;
        }
        if (this.username == null) {
            return -1;
        }
        if (o.username == null) {
            return 1;
        }
        return this.username.compareTo(o.username);
    }
    @Override
     public String toString() {
    return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
}


}