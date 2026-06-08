package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {
    @Select("SELECT * FROM diet_records WHERE user_id = #{userId} ORDER BY record_date DESC, create_time DESC")
    List<DietRecord> selectByUserId(@Param("userId") Long userId);
}
