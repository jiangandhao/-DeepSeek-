package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.ExerciseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ExerciseRecordMapper extends BaseMapper<ExerciseRecord> {
    @Select("SELECT * FROM exercise_records WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<ExerciseRecord> selectByUserId(@Param("userId") Long userId);
}
