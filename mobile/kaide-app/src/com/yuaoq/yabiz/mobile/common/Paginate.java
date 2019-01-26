package com.yuaoq.yabiz.mobile.common;

import java.io.Serializable;

/**
 * next:下页
 * page:
 * Created by changsy on 2017/7/25.
 */
public class Paginate implements Serializable{
    public Paginate() {
    }
    
    private boolean hasNext;
    private boolean hasPrev;
    private Integer next;  //下一页index
    private Integer page;   //当前pageindex
    private Integer pages;  //总页数
    private Integer pageSize; //
    private Integer prev;      //上一页index
    private Integer total; //总记录数

    public Paginate(boolean hasNext, boolean hasPrev, Integer next, Integer page, Integer pages, Integer pageSize, Integer prev, Integer total) {
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
        this.next = next;
        this.page = page;
        this.pages = pages;
        this.pageSize = pageSize;
        this.prev = prev;
        this.total = total;
    }

    public Paginate (int page,int pageSize,int total){
        int lowIndex = page * pageSize + 1;
        int highIndex = (pageSize + 1) * pageSize;
        boolean hasNext = true;
        boolean hasPrev = true;
        int next = page + 1;
        int pages = 1;
        //分页
        if (highIndex >= total) {
            hasNext = false;
        }
        int prev = 0;
        pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        if (lowIndex == 1) {
            hasPrev = false;
        }
        if (page == 0) {
            prev = 0;
        } else {
            prev = page - 1;
        }

        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
        this.next = next;
        this.page = page;
        this.pages = pages;
        this.pageSize = pageSize;
        this.prev = prev;
        this.total = total;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrev() {
        return hasPrev;
    }
    
    public void setHasPrev(boolean hasPrev) {
        this.hasPrev = hasPrev;
    }
    
    public Integer getNext() {
        return next;
    }
    
    public void setNext(Integer next) {
        this.next = next;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getPages() {
        return pages;
    }
    
    public void setPages(Integer pages) {
        this.pages = pages;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getPrev() {
        return prev;
    }
    
    public void setPrev(Integer prev) {
        this.prev = prev;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }

}
