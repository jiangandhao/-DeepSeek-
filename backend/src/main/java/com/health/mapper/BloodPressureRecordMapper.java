package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.BloodPressureRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BloodPressureRecordMapper extends BaseMapper<BloodPressureRecord> {

    @Select("SELECT * FROM blood_pressure_records WHERE user_id = #{userId} ORDER BY measure_time DESC")
    List<BloodPressureRecord> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM blood_pressure_records WHERE user_id = #{userId} AND measure_time >= #{startTime} ORDER BY measure_time ASC")
    List<BloodPressureRecord> selectByUserIdAndTimeRange(@Param("userId") Long userId, @Param("startTime") String startTime);
}
