package com.faris.gametracker.dto;

import java.util.List;

public class PagedUserGameResponse {
    private List<UserGameResponse> pagedUserGame;
    private boolean hasNext;
    private boolean hasPrevious;

    public PagedUserGameResponse(List<UserGameResponse> pagedUserGame, boolean hasPrevious, boolean hasNext) {
        this.pagedUserGame = pagedUserGame;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public List<UserGameResponse> getPagedUserGame() {
        return pagedUserGame;
    }

    public void setPagedUserGame(List<UserGameResponse> pagedUserGame) {
        this.pagedUserGame = pagedUserGame;
    }
}
