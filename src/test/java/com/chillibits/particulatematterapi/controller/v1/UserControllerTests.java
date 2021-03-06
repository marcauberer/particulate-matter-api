/*
 * Copyright © Marc Auberer 2019-2021. All rights reserved
 */

package com.chillibits.particulatematterapi.controller.v1;

import com.chillibits.particulatematterapi.exception.ErrorCode;
import com.chillibits.particulatematterapi.exception.exception.UserDataException;
import com.chillibits.particulatematterapi.model.db.main.User;
import com.chillibits.particulatematterapi.model.dto.UserDto;
import com.chillibits.particulatematterapi.model.dto.UserInsertUpdateDto;
import com.chillibits.particulatematterapi.repository.UserRepository;
import com.chillibits.particulatematterapi.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("logging")
@DisplayName("User Controller")
public class UserControllerTests {

    @Autowired
    private UserController userController;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    public JavaMailSender mailSender;

    private final List<User> testData = getTestData();
    private final List<UserInsertUpdateDto> insertUpdateTestData = getInsertUpdateTestData();
    private final List<UserDto> assertData = getAssertData();

    @TestConfiguration
    static class SensorControllerImplTestContextConfiguration {

        @Bean
        public UserController userController() {
            return new UserController();
        }

        @Bean
        public UserService userService() {
            return new UserService();
        }

        @Bean
        public ModelMapper modelMapper() {
            return new ModelMapper();
        }
    }

    @Before
    public void init() {
        // Setup fake method calls
        when(userRepository.findAll()).thenReturn(testData);
        when(userRepository.findByEmail(testData.get(0).getEmail())).thenReturn(testData.get(0));
        when(userRepository.findByEmail(testData.get(1).getEmail())).thenReturn(testData.get(1));
        when(userRepository.findByEmail(testData.get(3).getEmail())).thenReturn(testData.get(3));
        when(userRepository.findByEmail(testData.get(4).getEmail())).thenReturn(testData.get(4));
        when(userRepository.save(any(User.class))).then(returnsFirstArg());
        when(userRepository.updateUser(any(User.class))).thenReturn(1);
        doNothing().when(userRepository).deleteById(anyInt());
    }

    // -------------------------------------------------- Get users ----------------------------------------------------

    @Test
    @DisplayName("Test for getting all users successfully")
    public void testGetUsers() {
        // Get all sensors
        List<UserDto> result = userController.getAllUsers();
        assertThat(result).containsExactlyInAnyOrder(assertData.toArray(UserDto[]::new));
    }

    @Test
    @DisplayName("Test for getting an user by its email")
    public void testGetUserByEmail() {
        UserDto result = userController.getUserByEmail(testData.get(0).getEmail());
        assertEquals(assertData.get(0), result);
    }

    @Test
    @DisplayName("Test for getting an user by its email with invalid email")
    public void testGetUserByEmailInvalidEmail() {
        UserDto result = userController.getUserByEmail(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Test for signing in with email and password successfully")
    public void testSignIn() {
        UserDto result = userController.signIn("info@chillibits.com", "87654321");
        assertEquals(getAssertData().get(1), result);
    }

    @Test
    @DisplayName("Test for signing in with email and password failure")
    public void testSignInFailure() {
        // Test with invalid input
        assertNull(userController.signIn(null, "test"));
        assertNull(userController.signIn("", "test"));
        assertNull(userController.signIn("test@test.de", null));
        assertNull(userController.signIn("test@test.de", ""));

        // Test with invalid account states
        Exception exception = assertThrows(UserDataException.class, () ->
                userController.signIn("marc.auberer@chillibits.com", "12345678")
        );
        String expectedMessage = new UserDataException(ErrorCode.USER_EMAIL_CONFIRMATION_PENDING).getMessage();
        assertEquals(expectedMessage, exception.getMessage());

        exception = assertThrows(UserDataException.class, () ->
                userController.signIn("test2@chillibits.com", "WrsRgeF")
        );
        expectedMessage = new UserDataException(ErrorCode.USER_SUSPENDED).getMessage();
        assertEquals(expectedMessage, exception.getMessage());

        exception = assertThrows(UserDataException.class, () ->
                userController.signIn("test1@chillibits.com", "WrsRgeF")
        );
        expectedMessage = new UserDataException(ErrorCode.USER_LOCKED).getMessage();
        assertEquals(expectedMessage, exception.getMessage());
    }

    // -------------------------------------------------- Add user -----------------------------------------------------

    @Test
    @DisplayName("Test for adding an user successfully")
    public void testAddUser() throws UserDataException {
        UserDto result = userController.addUser(insertUpdateTestData.get(2));
        assertEquals(assertData.get(2), result);
    }

    @Test
    @DisplayName("Test for adding an user, triggering a UserAlreadyExists exception")
    public void testAddUserExceptionUserAlreadyExists() {
        // Try with invalid input
        Exception exception = assertThrows(UserDataException.class, () ->
                userController.addUser(insertUpdateTestData.get(0))
        );

        String expectedMessage = new UserDataException(ErrorCode.USER_ALREADY_EXISTS).getMessage();
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test for adding an user, triggering a InvalidUserData exception")
    public void testAddUserExceptionInvalidUserData() {
        // Try with invalid input
        Exception exception = assertThrows(UserDataException.class, () ->
                userController.addUser(insertUpdateTestData.get(5))
        );

        String expectedMessage = new UserDataException(ErrorCode.INVALID_USER_DATA).getMessage();
        assertEquals(expectedMessage, exception.getMessage());
    }

    // ------------------------------------------------ Update users ---------------------------------------------------

    @Test
    @DisplayName("Test for updating an user successfully")
    public void testUpdateUser() throws UserDataException {
        int result = userController.updateUser(insertUpdateTestData.get(0));
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test for updating an user, triggering a InvalidUserData exception")
    public void testUpdateUserExceptionNonExistingUser() {
        // Try with invalid input
        Exception exception = assertThrows(UserDataException.class, () ->
                userController.updateUser(insertUpdateTestData.get(2))
        );

        String expectedMessage = new UserDataException(ErrorCode.USER_NOT_EXISTING).getMessage();
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test for updating an user, triggering a InvalidUserData exception")
    public void testUpdateUserExceptionInvalidUserData() {
        // Try with invalid input
        Exception exception = assertThrows(UserDataException.class, () ->
                userController.updateUser(insertUpdateTestData.get(5))
        );

        String expectedMessage = new UserDataException(ErrorCode.INVALID_USER_DATA).getMessage();
        assertEquals(expectedMessage, exception.getMessage());
    }

    // ------------------------------------------------ Delete users ---------------------------------------------------

    @Test
    @DisplayName("Test for deleting an user")
    public void testDeleteUser() {
        assertDoesNotThrow(() -> userController.deleteUser(testData.get(2).getId()));
    }

    // -------------------------------------------------- Test data ----------------------------------------------------

    private List<User> getTestData() {
        // Create user objects
        long time = System.currentTimeMillis();
        User u1 = new User(1, "Marc", "Auberer", "marc.auberer@chillibits.com", "7TTU1ew7OpNa5XKvv0hc", "12345678", null, User.OPERATOR, User.EMAIL_CONFIRMATION_PENDING, time, time);
        User u2 = new User(2, "Admin", "User", "info@chillibits.com", "iNjwuU2GzCpDqjWLwYc5", "87654321", null, User.ADMINISTRATOR, User.ACTIVE, time, time);
        User u3 = new User(3, "Test", "User", "test@chillibits.com", "AHsY6peje1PyTTonZrZm", "87654321", null, User.USER, User.EMAIL_CONFIRMATION_PENDING, time, time);
        User u4 = new User(4, "Test", "User", "test1@chillibits.com", "Iujl4xkOGeqidOZQEFHT", "WrsRgeF", null, User.USER, User.LOCKED, time, time);
        User u5 = new User(5, "Test", "User", "test2@chillibits.com", "Iujl4xkOGeqidOZQEFHT", "WrsRgeF", null, User.USER, User.SUSPENDED, time, time);
        // Add them to test data
        return Arrays.asList(u1, u2, u3, u4, u5);
    }

    private List<UserInsertUpdateDto> getInsertUpdateTestData() {
        // Create user objects
        UserInsertUpdateDto u1 = new UserInsertUpdateDto(1, "Marc", "Auberer", "marc.auberer@chillibits.com", "12345678", null, User.OPERATOR, User.EMAIL_CONFIRMATION_PENDING);
        UserInsertUpdateDto u2 = new UserInsertUpdateDto(2, "Admin", "User", "info@chillibits.com", "87654321", null, User.ADMINISTRATOR, User.ACTIVE);
        UserInsertUpdateDto u3 = new UserInsertUpdateDto(3, "Test", "User", "test@chillibits.com", "87654321", null, User.USER, User.EMAIL_CONFIRMATION_PENDING);
        UserInsertUpdateDto u4 = new UserInsertUpdateDto(4, "Test", "User", "test1@chillibits.com", "WrsRgeF", null, User.USER, User.LOCKED);
        UserInsertUpdateDto u5 = new UserInsertUpdateDto(5, "Test", "User", "test2@chillibits.com", "WrsRgeF", null, User.USER, User.SUSPENDED);
        UserInsertUpdateDto u6 = new UserInsertUpdateDto(5, "Test", "User", "test3@chillibits.com", "", null, User.USER, User.EMAIL_CONFIRMATION_PENDING);
        // Add them to test data
        return Arrays.asList(u1, u2, u3, u4, u5, u6);
    }

    private List<UserDto> getAssertData() {
        // Create sensor dto objects
        UserDto ud1 = new UserDto(1, "Marc", "Auberer", "marc.auberer@chillibits.com", null, User.OPERATOR, User.EMAIL_CONFIRMATION_PENDING);
        UserDto ud2 = new UserDto(2, "Admin", "User", "info@chillibits.com", null, User.ADMINISTRATOR, User.ACTIVE);
        UserDto ud3 = new UserDto(3, "Test", "User", "test@chillibits.com", null, User.USER, User.EMAIL_CONFIRMATION_PENDING);
        UserDto ud4 = new UserDto(4, "Test", "User", "test1@chillibits.com", null, User.USER, User.LOCKED);
        UserDto ud5 = new UserDto(5, "Test", "User", "test2@chillibits.com", null, User.USER, User.SUSPENDED);

        // Add them to test data
        return Arrays.asList(ud1, ud2, ud3, ud4, ud5);
    }
}