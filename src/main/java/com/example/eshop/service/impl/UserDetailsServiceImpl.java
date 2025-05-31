package com.example.eshop.service.impl;

import com.example.eshop.model.User;
import com.example.eshop.model.UserStatus;
import com.example.eshop.repository.UserRepository;
import jakarta.persistence.DiscriminatorValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional(readOnly = true) // Good practice for read operations
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
    // Assuming username for login is the email address
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

    // Check if the user account is active
    if (user.getStatus() != UserStatus.ACTIVE) {
      String reason = "User account is not active. Status: " + user.getStatus();
      // Use DisabledException or AccountStatusException as appropriate
      throw new DisabledException(reason);
    }

    // Get authorities - use the discriminator value directly from the database
    Collection<? extends GrantedAuthority> authorities = Collections
        .singletonList(new SimpleGrantedAuthority(user.getClass().getAnnotation(DiscriminatorValue.class).value()));

    // Create and return Spring Security UserDetails object
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), // Use email as the username principal
        user.getPassword(),
        true, // enabled (we checked status above)
        true, // accountNonExpired
        true, // credentialsNonExpired
        true, // accountNonLocked
        authorities);
  }
}