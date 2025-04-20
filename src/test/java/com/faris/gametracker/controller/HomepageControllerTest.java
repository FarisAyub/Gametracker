package com.faris.gametracker.controller;

import com.faris.gametracker.service.GameApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomepageController.class)
public class HomepageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameApiService gameApiService;

    @Test
    public void home_ShouldReturnIndexPage() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(view().name("index")); // Should redirect to index as the view
    }

}
