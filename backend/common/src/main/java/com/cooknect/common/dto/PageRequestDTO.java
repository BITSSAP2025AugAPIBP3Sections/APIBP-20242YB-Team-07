package com.cooknect.common.dto;

public class PageRequestDTO {

    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String direction = "asc";

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
