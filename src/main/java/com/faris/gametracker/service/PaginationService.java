package com.faris.gametracker.service;

import com.faris.gametracker.dto.PageResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PaginationService {

    /**
     *  Uses generics to take in a list, and generates a page using current page and size of each page
     *
     * @param list List of objects, can take List of Games or UserGames
     * @param page Current page number, used to calculate start point for next page
     * @param size Size of each page, used to calculate end point for next page
     * @return Returns PageResponse of generic, which is the list that was passed in (Game or UserGame)
     */
    public <T> PageResponse<T> paginate(List<T> list, Integer page, Integer size) {
        // Create pointers for pagination
        int start = page * size; // Take current page multiplied by games per page to find the start index for this page
        int end = Math.min(start + size, list.size()); // Set end index to start index + amount per page, returning lower if there's not enough left in list

        List<T> paged;
        if (start >= list.size()) {
            paged = Collections.emptyList();
        } else {
            paged = list.subList(start, end);
        }

        // Booleans to be used by the page buttons, if there's no more games to left/right, disable button for moving page
        boolean hasNext = end < list.size();
        boolean hasPrevious = page > 0;

        return new PageResponse<>(paged, hasPrevious, hasNext);
    }
}
