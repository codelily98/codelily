package com.codelily.backend.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {
    List<Map<String, Object>> findTopPosts(@Param("limit") int limit);
}