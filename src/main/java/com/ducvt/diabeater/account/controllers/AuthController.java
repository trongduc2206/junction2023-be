package com.ducvt.diabeater.account.controllers;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.ducvt.diabeater.account.payload.request.DecodeRequest;
import com.ducvt.diabeater.account.repository.RoleRepository;
import com.ducvt.diabeater.account.repository.UserRepository;
import com.ducvt.diabeater.account.security.jwt.JwtUtils;
import com.ducvt.diabeater.account.security.services.UserDetailsImpl;
import com.ducvt.diabeater.fw.constant.MessageEnum;
import com.ducvt.diabeater.fw.exceptions.AuthorizationException;
import com.ducvt.diabeater.fw.exceptions.BusinessLogicException;
import com.ducvt.diabeater.fw.utils.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ducvt.diabeater.account.models.ERole;
import com.ducvt.diabeater.account.models.Role;
import com.ducvt.diabeater.account.models.User;
import com.ducvt.diabeater.account.payload.request.LoginRequest;
import com.ducvt.diabeater.account.payload.request.SignupRequest;
import com.ducvt.diabeater.account.payload.response.JwtResponse;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> user = userRepository.findByUsernameAndStatus(loginRequest.getUsername(), 1);
        if (user.isPresent()) {
            Authentication authentication;
            try {
                if (loginRequest.getUsername() != null) {
                    authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
                } else {
                    authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
                }
            } catch (Exception e) {
                throw new AuthorizationException(MessageEnum.WRONG_ACCOUNT.getMessage());
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            JwtResponse jwtResponse = new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles);
            jwtResponse.setDescription(user.get().getDescription());
            jwtResponse.setDiseaseType(user.get().getDiseaseType());
            jwtResponse.setDiseaseStart(user.get().getDiseaseStart());
            return ResponseFactory.success(jwtResponse);
//            return ResponseFactory.success(new JwtResponse(jwt,
//                    userDetails.getId(),
//                    userDetails.getUsername(),
//                    userDetails.getEmail(),
//                    roles));
        } else {
            throw new AuthorizationException(MessageEnum.WRONG_ACCOUNT.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // Create new user's account
        User user;
        if (signUpRequest.getPassword() != null && !signUpRequest.getPassword().isEmpty()) {
            user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));
        } else {
            user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
        }

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            user.setDiseaseType(signUpRequest.getDiseaseType());
            user.setDiseaseStart(signUpRequest.getDiseaseStart());
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "doctor":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                        user.setDiseaseType(signUpRequest.getDiseaseType());
                        user.setDiseaseStart(signUpRequest.getDiseaseStart());
                }
            });
        }

        user.setRoles(roles);
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setFullName(signUpRequest.getFullName());
        user.setGender(signUpRequest.getGender());
        user.setAge(signUpRequest.getAge());
        user.setDescription(signUpRequest.getDescription());
        userRepository.save(user);

        return ResponseFactory.success(user.getId());
    }

    @PostMapping(value="/decode")
    public ResponseEntity decode(@RequestBody DecodeRequest request) {
        return ResponseFactory.success(jwtUtils.decode(request.getJwt()));
    }
}
