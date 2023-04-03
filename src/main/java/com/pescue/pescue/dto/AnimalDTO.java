package com.pescue.pescue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnimalDTO {
    private String shelterID;
    private String animalName;
    private Integer animalAge;
    private boolean animalGender;
    private Integer animalWeight;
    private String animalBreed;
    private String animalColor;
    private String animalImg;
    private boolean isVaccinated;
    private boolean isDeWormed;
    private boolean isSterilized;
    private boolean isFriendly;
    private boolean isDeleted = false;
    private List<String> onlineAdaptors;
}
