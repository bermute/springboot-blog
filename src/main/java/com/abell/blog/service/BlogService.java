package com.abell.blog.service;

import com.abell.blog.domain.Article;
import com.abell.blog.dto.AddArticleRequest;
import com.abell.blog.dto.UpdateArticleRequest;
import com.abell.blog.repository.BlogRepository;
import jakarta.servlet.http.PushBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor // final이 붙거나 @NotNull 이 붙은 필드의 생성자 추가 생성자 주입 간편 어노테이션
@Service //빈으로 등록
public class BlogService {

    private final BlogRepository blogRepository;

    // 블로그 글 추가 메서드
    public Article save(AddArticleRequest request){
        return blogRepository.save(request.toEntity());
    }
    //코드 미쳣다 간단 그자체  ..

    public List<Article> findAll(){
        return blogRepository.findAll();
    }

    public Article findById(long id){
        return blogRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("not found: " + id));
    }

    public void delete(long id){
        blogRepository.deleteById(id);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        article.update(request.getTitle(), request.getContent());
        return article;
    }



}
