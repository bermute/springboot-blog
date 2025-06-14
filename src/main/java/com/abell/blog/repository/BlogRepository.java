package com.abell.blog.repository;

import com.abell.blog.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Article,Long> {
}
