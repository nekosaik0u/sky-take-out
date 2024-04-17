package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    @Select("select count(*) from setmeal where category_id = #{categoryById}")
    Integer countByCategoryId(Long categoryById);

    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    List<Long> getStatusByIds(List<Long> ids);

    void deleteBatch(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select s.*, c.name category_name from setmeal s left outer join category c on s.category_id = c.id where s.id = #{id}")
    SetmealVO getSetmealVOById(Long id);

    List<Setmeal> list(Setmeal setmeal);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemById(Long setmealId);

    @Select("select * from setmeal where id = #{Id}")
    Setmeal getById(Long Id);
}
