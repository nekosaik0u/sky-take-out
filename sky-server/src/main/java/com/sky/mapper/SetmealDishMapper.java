package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据 dishId 查询 setmealId
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
