package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.BloodSugarRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BloodSugarRecordMapper extends BaseMapper<BloodSugarRecord> {
    @Select("SELECT * FROM blood_sugar_records WHERE user_id = #{userId} ORDER BY measure_time DESC")
    List<BloodSugarRecord> selectByUserId(@Param("userId") Long userId);
}
