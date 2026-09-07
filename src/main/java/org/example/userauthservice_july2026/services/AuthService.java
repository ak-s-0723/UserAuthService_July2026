package org.example.userauthservice_july2026.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.example.userauthservice_july2026.exceptions.PasswordMismatchException;
import org.example.userauthservice_july2026.exceptions.UserAlreadyExistsException;
import org.example.userauthservice_july2026.exceptions.UserNotRegisteredException;
import org.example.userauthservice_july2026.models.Role;
import org.example.userauthservice_july2026.models.Status;
import org.example.userauthservice_july2026.models.User;
import org.example.userauthservice_july2026.models.UserSession;
import org.example.userauthservice_july2026.repos.RoleRepo;
import org.example.userauthservice_july2026.repos.SessionRepo;
import org.example.userauthservice_july2026.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo  userRepo;


    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SecretKey secretKey;

    @Override
    public User signup(String email, String password, String name, String phoneNumber) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isPresent()) {
           throw new UserAlreadyExistsException("Please try different emailId");
        }

        userOptional = userRepo.findByPhoneNumber(phoneNumber);
        if (userOptional.isPresent()) {
            throw new UserAlreadyExistsException("Please try different phoneNumber");
        }

        User user = new User();
        user.setEmail(email);
        //user.setPassword(password);  //this should not be passed as raw password
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setName(name);

        //ToDo : We can optimize creation of role
        Role role;
        Optional<Role> optionalRole = roleRepo.findByValue("NON_ADMIN");
        if (optionalRole.isEmpty()) {
           role = new Role();
           role.setValue("NON_ADMIN");
           roleRepo.save(role);
        } else {
            role = optionalRole.get();
        }

        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepo.save(user);
    }

    @Override
    public Pair<User,String> login(String email, String password) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
           throw new UserNotRegisteredException("Please signup first");
        }

        User user = userOptional.get();
        //if(!user.getPassword().equals(password)) {
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
          throw new PasswordMismatchException("Please use correct credentials");
        }

        //Generating JWT

        //Payload
        Map<String,Object> claims = new HashMap<>();
        claims.put("user_id",user.getId());
        claims.put("issuer","scaler");
        Long currentTime = System.currentTimeMillis();
        claims.put("iat",currentTime);      //iat = issued at
        claims.put("exp",currentTime+20000);

        List<Role> roles = user.getRoles();
        List<String> roleValues  = new ArrayList<>();
        for(Role role : roles) {
            roleValues.add(role.getValue());
        }

        claims.put("user_access",roleValues);

//        MacAlgorithm algorithm  = Jwts.SIG.HS256;
//        SecretKey secretKey  = algorithm.key().build();

        String token = Jwts.builder().claims(claims).signWith(secretKey).compact();

        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setToken(token);
        userSession.setStatus(Status.ACTIVE);
        sessionRepo.save(userSession);

        return new Pair<>(user,token);
    }


    public Boolean validateToken(String token) {
        Optional<UserSession> userSessionOptional = sessionRepo.findByToken(token);
        if(userSessionOptional.isEmpty()) return false;

        UserSession userSession = userSessionOptional.get();

        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        Long expiry = (Long)claims.get("exp");
        Long currentTime = System.currentTimeMillis();
        System.out.println("expiry = "+expiry);
        System.out.println("currentTime = "+currentTime);
        if(expiry < currentTime) {
            userSession.setStatus(Status.INACTIVE);
            sessionRepo.save(userSession);
            System.out.println("Token has expired");

            return false;
        }

        return true;
    }
}


//password = "anurag"
//bcryptPasswordEncoder.encode("anurag") -> "djsgegifgweifgeigfiwgfeiwu"
//        bcryptPasswordEncoder.encode("anurag") -> "dheiwhio9393939393hfioejoejfe"