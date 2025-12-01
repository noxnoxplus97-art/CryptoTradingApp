package com.example.tradingapp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserConstructorAndGetters() {
        // Arrange & Act
        user.setId(1L);
        user.setUsername("trader1");
        user.setEmail("trader1@trading.com");

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("trader1", user.getUsername());
        assertEquals("trader1@trading.com", user.getEmail());
    }

    @Test
    void testUserWithValidEmail() {
        // Arrange & Act
        user.setEmail("john.doe@example.com");

        // Assert
        assertEquals("john.doe@example.com", user.getEmail());
    }

    @Test
    void testUserWithSimpleUsername() {
        // Arrange & Act
        user.setUsername("alice");

        // Assert
        assertEquals("alice", user.getUsername());
    }

    @Test
    void testUserWithComplexUsername() {
        // Arrange & Act
        user.setUsername("trader_pro_2024");

        // Assert
        assertEquals("trader_pro_2024", user.getUsername());
    }

    @Test
    void testUserIdAssignment() {
        // Arrange & Act
        user.setId(100L);

        // Assert
        assertEquals(100L, user.getId());
    }

    @Test
    void testMultipleUserCreation() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        // Act & Assert
        assertNotEquals(user1.getId(), user2.getId());
        assertNotEquals(user1.getUsername(), user2.getUsername());
    }

    @Test
    void testUserEquality() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");
        user1.setEmail("test@trading.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");
        user2.setEmail("test@trading.com");

        // Act & Assert
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getUsername(), user2.getUsername());
        assertEquals(user1.getEmail(), user2.getEmail());
    }

    @Test
    void testUserWithNullValues() {
        // Arrange & Act
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);

        // Assert
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
    }

    @Test
    void testUserModification() {
        // Arrange
        user.setUsername("originalname");
        user.setEmail("original@email.com");

        // Act - modify user
        user.setUsername("newname");
        user.setEmail("new@email.com");

        // Assert
        assertEquals("newname", user.getUsername());
        assertEquals("new@email.com", user.getEmail());
    }

    @Test
    void testUserWithEmptyStrings() {
        // Arrange & Act
        user.setUsername("");
        user.setEmail("");

        // Assert
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
    }

    @Test
    void testUserWithSpecialCharactersInEmail() {
        // Arrange & Act
        user.setEmail("user+tag@example.co.uk");

        // Assert
        assertEquals("user+tag@example.co.uk", user.getEmail());
    }

    @Test
    void testDefaultUserCreation() {
        // Arrange & Act
        User defaultUser = new User();

        // Assert - verify default state
        assertNull(defaultUser.getId());
        assertNull(defaultUser.getUsername());
        assertNull(defaultUser.getEmail());
    }
}
