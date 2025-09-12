package com.faris.gametracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomepageController.class)
public class HomepageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void home_ShouldReturnIndexPage() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(view().name("index")); // Should redirect to index as the view
    }

}
