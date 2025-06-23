package com.abell.blog.controller;

import com.abell.blog.domain.Article;
import com.abell.blog.domain.User;
import com.abell.blog.dto.AddArticleRequest;
import com.abell.blog.dto.UpdateArticleRequest;
import com.abell.blog.repository.BlogRepository;
import com.abell.blog.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.security.Principal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성 및 자동 구성
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스

    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context) //테스트 실행 전 MockMvc 초기화.
                .build();
        blogRepository.deleteAll(); // DB에서 기존 게시글 모두 삭제.
    }
    @BeforeEach // 테스트용 유저를 생성해 SecurityContext에 직접 인증 정보 설정 (Spring Security 로그인 상태 시뮬레이션).
    void setSecurityContext() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }



    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    @Test
    public void addArticle() throws Exception{
        //given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title,content);

        //객체 JSON 으로 직렬화

        final String requestBody = objectMapper.writeValueAsString(userRequest);
        //이건 "현재 로그인한 사용자는 username이라는 이름을 가진 사용자라고 치자"
        // 라고 테스트용으로 가짜 로그인 정보를 주입하는 거야.
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        //when
        //Spring MVC 테스트에서 MockMvc는 .principal(principal)
        // 메서드를 제공해서 가짜 로그인 사용자 정보(Principal 객체)를 주입할 수 있게 해주는 거야.
        ResultActions result = mockMvc.perform(post(url)       // /api/articles에 POST 요청 보내기.
                .contentType(MediaType.APPLICATION_JSON_VALUE)  // JSON으로 title과 content 포함된 요청 전송.
                .principal(principal)   // Principal 모킹으로 인증된 사용자 정보 제공.
                .content(requestBody));

        //설정한 내용을 바탕으로 요청 전송

        //then
        result.andExpect(status().isCreated());  // 응답 상태가 201 CREATED인지, DB에 글이 저장되었는지 검증.

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);//크기가 1인지 검증
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }
    @DisplayName("findAllArticles:아티클 목록 조회에 성공한다.")
    @Test
    public void findAllArticles() throws Exception {
        // given
        final String url = "/api/articles";
        Article savedArticle = createDefaultArticle();
        // when
        final ResultActions resultActions = mockMvc.perform(get(url) // /api/articles에 GET 요청.
                .accept(MediaType.APPLICATION_JSON));  // 저장된 게시글을 먼저 1개 생성한 후,

        //THEN
        resultActions  //응답 JSON이 해당 제목과 내용 포함하는지 검증.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle()));
    }

    @DisplayName("findArticle: 아티클 단건 조회에 성공한다.")
    @Test
    public void findArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when
        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId())); // /api/articles/{id}로 GET 요청.

        // then
        resultActions // 특정 ID의 게시글 정보를 JSON으로 받는지 확인.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$.title").value(savedArticle.getTitle()));
    }

    @DisplayName("deleteArticle: 아티클 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when
        mockMvc.perform(delete(url, savedArticle.getId())) // /api/articles/{id}로 DELETE 요청.
                .andExpect(status().isOk());

        // then
        List<Article> articles = blogRepository.findAll(); // 요청 성공 후 DB에 게시글이 사라졌는지 확인.

        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 아티클 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        final String newTitle = "new title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        // when
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }

    //테스트용 게시글 생성해서 저장
    private Article createDefaultArticle() {
        return blogRepository.save(Article.builder()
                .title("title")
                .author(user.getUsername())
                .content("content")
                .build());
    }





}

