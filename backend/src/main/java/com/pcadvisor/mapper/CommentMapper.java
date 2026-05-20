package com.pcadvisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pcadvisor.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
