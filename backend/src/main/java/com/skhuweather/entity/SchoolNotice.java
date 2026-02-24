package com.skhuweather.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "school_notice")
public class SchoolNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 20)
    private String noticeNum;

    public SchoolNotice() {}

    public SchoolNotice(String title, String noticeNum) {
        this.title = title;
        this.noticeNum = noticeNum;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNoticeNum() { return noticeNum; }
    public void setNoticeNum(String noticeNum) { this.noticeNum = noticeNum; }
}
