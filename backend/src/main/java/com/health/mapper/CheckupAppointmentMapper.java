package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.CheckupAppointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CheckupAppointmentMapper extends BaseMapper<CheckupAppointment> {

    @Select("SELECT * FROM checkup_appointments WHERE user_id = #{userId} ORDER BY appointment_date DESC")
    List<CheckupAppointment> selectByUserId(@Param("userId") Long userId);
}
