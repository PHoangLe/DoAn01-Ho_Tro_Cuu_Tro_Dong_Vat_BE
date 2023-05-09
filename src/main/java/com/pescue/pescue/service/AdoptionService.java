package com.pescue.pescue.service;

import com.pescue.pescue.dto.AdoptionApplicationDTO;
import com.pescue.pescue.dto.AdoptionApplicationRequestDTO;
import com.pescue.pescue.exception.AnimalNotFoundException;
import com.pescue.pescue.exception.ShelterNotFoundException;
import com.pescue.pescue.exception.UserNotFoundException;
import com.pescue.pescue.model.*;
import com.pescue.pescue.repository.AdoptionApplicationRepository;
import com.pescue.pescue.repository.OnlineAdoptionApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptionService {
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final OnlineAdoptionApplicationRepository onlineAdoptionApplicationRepository;
    private final AnimalService animalService;
    private final UserService userService;
    private final ShelterService shelterService;


    //Offline Adoption
    public void createAdoptionRequest(AdoptionApplicationRequestDTO dto){
        AdoptionApplication application = new AdoptionApplication(dto);

        User user = userService.findUserByID(dto.getUserID());
        Animal animal = animalService.findAnimalByAnimalID(dto.getAnimalID());
        Shelter shelter = shelterService.findShelterByShelterID(dto.getShelterID());

        if (user == null) {
            log.trace("User not found ID: " + dto.getUserID());
            throw new UserNotFoundException();
        }
        if (animal == null) {
            log.trace("Animal not found ID: " + dto.getAnimalID());
            throw new AnimalNotFoundException();
        }
        if (shelter == null) {
            log.trace("Shelter not found ID: " + dto.getShelterID());
            throw new ShelterNotFoundException();
        }
        adoptionApplicationRepository.insert(application);
        log.trace("Added adoption application for user: " + application.getUserID() + " pet: " + application.getAnimalID());
    }
    public AdoptionApplication findApplicationByApplicationID(String applicationID){
        log.trace("Finding adoption application with ID: " + applicationID);
        return adoptionApplicationRepository.findByApplicationID(applicationID).orElse(null);
    }

    public boolean confirmAdoptionRequest(String applicationID) {
        AdoptionApplication application = findApplicationByApplicationID(applicationID);

        if (application == null)
            return false;

        Animal animal = animalService.findAnimalByAnimalID(application.getAnimalID());

        application.setApplicationStatus(ApplicationStatus.COMPLETED);
        animal.setAdopted(true);

        try {
            adoptionApplicationRepository.save(application);
            animalService.updateAnimal(animal);

            log.trace("Approved application with ID: " + applicationID);
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

        return true;
    }
    public boolean declineAdoptionRequest(String applicationID) {
        AdoptionApplication application = findApplicationByApplicationID(applicationID);

        if (application == null)
            return false;

        application.setApplicationStatus(ApplicationStatus.REJECTED);

        try {
            adoptionApplicationRepository.save(application);

            log.trace("Declined application with ID: " + applicationID);
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

        return true;
    }
    public List<AdoptionApplicationDTO> getApplicationByShelterID(String shelterID) {
        List<AdoptionApplication> adoptionApplicationList = adoptionApplicationRepository.findAllByShelterID(shelterID);

        List<AdoptionApplicationDTO> applicationDTOS = new ArrayList<>();
        adoptionApplicationList.forEach(application -> {
            User user = userService.findUserByID(application.getUserID());
            Animal animal = animalService.findAnimalByAnimalID(application.getAnimalID());
            Shelter shelter = shelterService.findShelterByShelterID(application.getShelterID());

            applicationDTOS.add(new AdoptionApplicationDTO(application, user, animal, shelter));
        });

        return applicationDTOS;
    }


    //Online Adoption
    public boolean createOnlineAdoptionRequest(AdoptionApplicationRequestDTO dto){
        OnlineAdoptionApplication application = new OnlineAdoptionApplication(dto);

        try{
            onlineAdoptionApplicationRepository.insert(application);
            log.trace("added online adoption application for user: " + application.getUserID() + " pet: " + application.getAnimalID());
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }
    public OnlineAdoptionApplication findOnlineApplicationByApplicationID(String applicationID){
        log.trace("Finding adoption application with ID: " + applicationID);
        return onlineAdoptionApplicationRepository.findByApplicationID(applicationID).orElse(null);
    }
    public boolean confirmOnlineAdoptionRequest(String applicationID) {
        OnlineAdoptionApplication application = findOnlineApplicationByApplicationID(applicationID);

        if (application == null)
            return false;

        Animal animal = animalService.findAnimalByAnimalID(application.getAnimalID());
        User user = userService.findUserByID(application.getUserID());

        application.setApplicationStatus(ApplicationStatus.COMPLETED);

        try {
            animalService.addOnlineAdopters(animal, user);
            onlineAdoptionApplicationRepository.save(application);

            log.trace("Approved online application with ID: " + applicationID);
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

        return true;
    }
    public boolean declineOnlineAdoptionRequest(String applicationID) {
        OnlineAdoptionApplication application = findOnlineApplicationByApplicationID(applicationID);

        if (application == null)
            return false;

        application.setApplicationStatus(ApplicationStatus.REJECTED);

        try {
            onlineAdoptionApplicationRepository.save(application);

            log.trace("Declined online application with ID: " + applicationID);
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }

        return true;
    }
    public List<OnlineAdoptionApplication> getAllOnlineApplication(){
        return onlineAdoptionApplicationRepository.findAll();
    }
}
