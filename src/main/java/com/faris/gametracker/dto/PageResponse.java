package com.faris.gametracker.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> pagedList; // Paged list, uses generic and is used for: Game, UserGame and UserGameResponse lists
    private boolean hasNext; // Whether there's a next page for current pagination
    private boolean hasPrevious; // Whether there's a previous page for current pagination

    public PageResponse(List<T> pagedList, boolean hasPrevious, boolean hasNext) {
        this.pagedList = pagedList;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    public List<T> getPagedList() {
        return pagedList;
    }

    public void setPagedList(List<T> pagedList) {
        this.pagedList = pagedList;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
