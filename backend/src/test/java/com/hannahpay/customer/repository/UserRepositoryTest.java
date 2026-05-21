package com.hannahpay.customer.repository;

import com.hannahpay.customer.domain.User;
import com.hannahpay.customer.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsUserByEmailIgnoringCase() {
        User saved = userRepository.save(new User("Test@Example.com", "hash-1", "Test User"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(userRepository.findByEmailIgnoreCase("test@example.com")).isPresent();
        assertThat(userRepository.existsByEmailIgnoreCase("TEST@example.com")).isTrue();
    }

    @Test
    void normalizesEmailBeforePersisting() {
        User saved = userRepository.saveAndFlush(new User("TEST@example.com", "hash-1", "Test User"));

        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(userRepository.findByEmailIgnoreCase("TEST@example.com")).isPresent();
    }
}
