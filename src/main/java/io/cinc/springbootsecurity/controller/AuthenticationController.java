package io.cinc.springbootsecurity.controller;


import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.cinc.springbootsecurity.dto.UserRequestDTO;
import io.cinc.springbootsecurity.exception.ResourceConflictException;
import io.cinc.springbootsecurity.model.PasswordResetToken;
import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.dto.UserTokenState;
import io.cinc.springbootsecurity.model.VerificationToken;
import io.cinc.springbootsecurity.security.TokenUtils;
import io.cinc.springbootsecurity.security.auth.JwtAuthenticationRequest;
import io.cinc.springbootsecurity.service.IEmailService;
import io.cinc.springbootsecurity.service.IPasswordResetTokenService;
import io.cinc.springbootsecurity.service.IUserService;
import io.cinc.springbootsecurity.service.IVerificationTokenService;
import io.cinc.springbootsecurity.service.impl.CustomUserDetailsService;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

//Kontroler zaduzen za autentifikaciju korisnika
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private IVerificationTokenService verificationTokenService;

    @Autowired
    private IPasswordResetTokenService passwordResetTokenService;

    @Autowired
    DozerBeanMapper mapper;

    // Prvi endpoint koji pogadja korisnik kada se loguje.
    // Tada zna samo svoje korisnicko ime i lozinku i to prosledjuje na backend.
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
                                                       HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()));

            // Ubaci korisnika u trenutni security kontekst
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Kreiraj token za tog korisnika
            User user = (User) authentication.getPrincipal();

            String jwt = tokenUtils.generateToken(user.getUsername());
            int expiresIn = tokenUtils.getExpiredIn();

            // Vrati token kao odgovor na uspesnu autentifikaciju
            return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
        } catch (DisabledException disabledException) {
            return ResponseEntity.ok("Your account is not activated");

        }

    }

    // Endpoint za registraciju novog korisnika
    @RequestMapping(value = "/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> addUser(@RequestBody UserRequestDTO userRequestDTO, UriComponentsBuilder ucBuilder) {

        User existUser = this.userService.findByUsername(userRequestDTO.getUsername());
        if (existUser != null) {
            throw new ResourceConflictException(userRequestDTO.getId(), "Username already exists");
        }

        User user = this.userService.save(userRequestDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/api/user/{userId}").buildAndExpand(user.getId()).toUri());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // U slucaju isteka vazenja JWT tokena, endpoint koji se poziva da se token osvezi
    @PostMapping(value = "/refresh")
    public ResponseEntity<UserTokenState> refreshAuthenticationToken(HttpServletRequest request) {

        String token = tokenUtils.getToken(request);
        String username = this.tokenUtils.getUsernameFromToken(token);
        User user = (User) this.userDetailsService.loadUserByUsername(username);

        if (this.tokenUtils.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = tokenUtils.refreshToken(token);
            int expiresIn = tokenUtils.getExpiredIn();

            return ResponseEntity.ok(new UserTokenState(refreshedToken, expiresIn));
        } else {
            UserTokenState userTokenState = new UserTokenState();
            return ResponseEntity.badRequest().body(userTokenState);
        }
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChanger passwordChanger) {
        userDetailsService.changePassword(passwordChanger.oldPassword, passwordChanger.newPassword);

        Map<String, String> result = new HashMap<>();
        result.put("result", "success");
        return ResponseEntity.accepted().body(result);
    }

    static class PasswordChanger {
        public String oldPassword;
        public String newPassword;
    }

    @RequestMapping(value = "/activation", method = RequestMethod.GET)
    public ResponseEntity<String> activation(@RequestParam("token") String token) {

        VerificationToken verificationToken = verificationTokenService.findByToken(token);

        if (verificationToken == null) {
            return new ResponseEntity<String>("Your verification token is invalid", HttpStatus.OK);
        } else {
            User user = verificationToken.getUser();

            if (!user.isEnabled()) {

                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

                if (verificationToken.getExpiryDate().before(currentTimestamp)) {
                    return new ResponseEntity<String>("Your verification token has expired", HttpStatus.OK);

                } else {
                    user.setEnabled(true);
                    userService.update(user);
                    return new ResponseEntity<String>("Your account is successfully activated", HttpStatus.OK);

                }
            } else {
                return new ResponseEntity<String>("Your account is already activated", HttpStatus.OK);
            }
        }
    }

    // Process form submission from forgotPassword page
    @RequestMapping(value = "/forgot", method = RequestMethod.POST)
    public ResponseEntity<String> processForgotPasswordForm(@RequestBody String userEmail) {

        // Lookup user in database by e-mail
        Optional<User> optional = userService.findByEmail(userEmail);

        if (!optional.isPresent()) {
            return new ResponseEntity<String>("We didn't find an account for that e-mail address.", HttpStatus.BAD_REQUEST);
        } else {

            User user = optional.get();

            if (user.isEnabled()) {
                // Generate random 36-character string token for reset password
                String token = UUID.randomUUID().toString();

                // Save token to database
                passwordResetTokenService.save(user, token);

                try {
                    emailService.sendPasswordMail(user);
                    return new ResponseEntity<String>("Email sent", HttpStatus.OK);

                } catch (MessagingException e) {
                    e.printStackTrace();
                    return new ResponseEntity<String>("Error sending email", HttpStatus.BAD_REQUEST);

                }
            } else {
                return new ResponseEntity<String>("Your account is not activated", HttpStatus.BAD_REQUEST);
            }
        }
    }

    // Display form to reset password
    @RequestMapping(value = "/forgotten", method = RequestMethod.GET)
    public ResponseEntity<String> displayResetPasswordPage(@RequestParam("token") String token) {

        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token);

        if (passwordResetToken == null) {
            return new ResponseEntity<String>("Your password reset token is invalid", HttpStatus.BAD_REQUEST);
        } else {

            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            if (passwordResetToken.getExpiryDate().before(currentTimestamp)) {
                return new ResponseEntity<String>("Your password reset token has expired", HttpStatus.BAD_REQUEST);

            } else {
                return new ResponseEntity<String>("Your can reset password", HttpStatus.OK);

            }

        }
    }


}
