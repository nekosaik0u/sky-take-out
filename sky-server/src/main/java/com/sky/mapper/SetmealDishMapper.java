package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据 dishId 查询 setmealId
     * @param dishIds 菜品id集合
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 添加套餐中菜品信息
     * @param setmealDishes 套餐菜品信息集合
     */
    void insert(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐 id 批量删除 套餐餐品关联信息
     * @param setmealIds 套餐 id 集合
     */
    void deleteBatch(List<Long> setmealIds);

    /**
     * 根据套餐 id 删除 套餐菜品关联信息
     * @param setmealId 套餐 id
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);


    /**
     * 根据套餐 id 获取关联菜品 id 集合
     * @param dishId 套餐 id
     * @return
     */
    @Select("select dish_id from setmeal_dish where setmeal_id = #{dishId}")
    List<Long> getDishIdsBySetmealId(Long dishId);

    /**
     * 根据套餐 id 获取 套餐菜品关联信息
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
