package com.faris.gametracker.controller;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.service.UserGameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserGameController.class)
public class UserGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserGameService userGameService;

    private UserGameRequest invalidRequest;

    @Autowired
    private ObjectMapper objectMapper; // To convert java to JSON for http requests like post

    @BeforeEach
    public void setup() {
        invalidRequest = new UserGameRequest();
        invalidRequest.setGameId(1L);
        invalidRequest.setRating(6); // invalid rating
        invalidRequest.setNote("A".repeat(256)); // invalid note
    }

    @Test
    public void getAllUserGames_ShouldReturnOkAndModelAttributes() throws Exception {
        // Mock response
        UserGameResponse response = new UserGameResponse(
                1L,
                "url",
                "The Witcher 3",
                "CDPR",
                "CD Projekt",
                LocalDate.now(),
                5,
                "note"
        );
        // Mock that there's no next or previous page
        PageResponse<UserGameResponse> pageOfGames = new PageResponse<>(Collections.singletonList(response), false, false);

        // When called with page 0 size 9, should have no previous or next page
        when(userGameService.getUserGameResponse(null, null, null, 0, 9)).thenReturn(pageOfGames);

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("hasPrevious", false))
                .andExpect(model().attribute("hasNext", false));
    }


    @Test
    public void deleteUserGame_EndpointExists_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/user-games/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    public void updateUserGame_InvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/user-games/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));
    }

    @Test
    public void addUserGame_InvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/user-games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error: must be less than or equal to 5")));
    }
}
