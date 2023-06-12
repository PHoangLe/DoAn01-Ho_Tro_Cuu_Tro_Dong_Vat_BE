package com.pescue.pescue.service;

import com.pescue.pescue.dto.ChangePasswordDTO;
import com.pescue.pescue.dto.UserProfileDTO;
import com.pescue.pescue.exception.UserNotFoundException;
import com.pescue.pescue.model.constant.Role;
import com.pescue.pescue.model.User;
import com.pescue.pescue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUser(){return userRepository.findAll();}

    public boolean updateUser(User user){
        try {
            userRepository.save(user);
            logger.trace("Updated user with Id: " + user.getUserID());
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public User findUserByUserEmail(String emailAddress){
        if(userRepository.findUserByUserEmail(emailAddress).isPresent())
            return userRepository.findUserByUserEmail(emailAddress).get();
        return null;
    }

    public User getUserByID(String userID){
        log.trace("Finding user with ID: " + userID);
        return userRepository.findUserByUserID(userID).orElse(null);
    }

    public boolean unlockUser(String emailAddress){
        Optional<User> user = userRepository.findUserByUserEmail(emailAddress);

        if (user.isEmpty())
            return false;

        user.get().setLocked(false);
        userRepository.save(user.get());
        return true;
    }

    public boolean addUser(User user) {
        try {
            userRepository.insert(user);
        }
        catch (Exception e){
            logger.error("There is an error occur while inserting user to database: " + user);
            return false;
        }
        logger.trace("User information has been added to database: " + user);
        return true;
    }

    public boolean addRoleForUser(String userID, Role role){
        User user = getUserByID(userID);
        List<Role> currentRole = user.getUserRoles();
        currentRole.add(role);
        user.setUserRoles(currentRole);

        if(!updateUser(user)) {
            logger.error("There is an error occur while inserting role " + role + " for user: " + userID);
            return false;
        }
        logger.trace("Succeed to add role " + role + " to user: " + userID);
        return true;
    }

    public boolean changePassword(ChangePasswordDTO dto, User user) {
        user.setUserPassword(passwordEncoder.encode(dto.getUserNewPassword()));
        try {
            userRepository.save(user);
            logger.trace("Changed password of User: " + dto.getUserEmail());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public User updateUserProfile(UserProfileDTO userProfileDTO) throws Exception {
        User user = getUserByID(userProfileDTO.getUserID());

        if (user == null) {
            log.error("Can't find any user with Id: " + userProfileDTO.getUserID());
            throw new UserNotFoundException();
        }

        user.setUserFirstName(userProfileDTO.getUserFirstName());
        user.setUserLastName(userProfileDTO.getUserLastName());
        user.setPhoneNo(userProfileDTO.getPhoneNo());
        user.setDob(stringToDate(userProfileDTO.getDob()));
        user.setUserGender(userProfileDTO.getUserGender());
        user.setUserAvatar(userProfileDTO.getUserAvatar());

        if(!updateUser(user)) {
            throw new Exception();
        }

        return user;
    }


    private Date stringToDate(String dob) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy").parse(dob);
    }
}
