package com.abell.blog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter //필드변수 게터 메서드 적용
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자 접근 제어 protected 설정
public class Article {
    @Id //Id 필드를 기본키로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) //기본키를 자동으로 1 씩 증가
    @Column(name = "id",updatable = false)
    private Long id;

    @Column(name = "title" , nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Builder
    public Article(String title, String content){
        this.title = title;
        this.content = content;
    }

    public void update(String title,String content){
        this.title = title;
        this.content = content;
    }




}































