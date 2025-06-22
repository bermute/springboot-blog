package com.abell.blog.dto;

public record CustomErrorResponse(String code, String message) {}

//🔍 이 코드가 자동으로 만들어주는 것들:
//이 한 줄로 아래 기능이 모두 자동 생성됩니다:
//
//private final String code;
//
//private final String message;
//
//생성자: new CustomErrorResponse("NOT_FOUND", "리소스를 찾을 수 없습니다.")
//
//getCode(), getMessage() (사실 record는 그냥 customErrorResponse.code() 식으로 접근)
//
//equals(), hashCode(), toString()