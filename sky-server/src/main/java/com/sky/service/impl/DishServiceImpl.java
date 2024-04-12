package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入 1 条数据
        dishMapper.insert(dish);

        // 获取 insert 语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if (dishFlavors != null && !dishFlavors.isEmpty()) {
            dishFlavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 向口味表中加入 n 条数据
            dishFlavorMapper.insert(dishFlavors);
        }

    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> pageResult = dishMapper.pageQuery(dishPageQueryDTO);
        long total = pageResult.getTotal();
        List<DishVO> result = pageResult.getResult();

        return new PageResult(total, result);
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否能够删除 --- 是否在起售中的菜品？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断当前菜品是否能够删除 --- 是否被套餐关联？
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        /*// 删除菜品表中的菜品数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }*/

        // 根据菜品 id 集合批量删除菜品数据
        dishMapper.deleteByIds(ids);

        // 根据菜品 id 集合批量删除关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dish.getId());
        }
        // 修改关联菜品口味值
        dishFlavorMapper.deleteByDishId(dish.getId());

        if (!flavors.isEmpty()) {
            dishFlavorMapper.insert(flavors);
        }
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.getDishVOById(id);

        // 根据 dishId 查询关联的口味
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(id);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 根据分类 id 查询菜品
     *
     * @param categoryId
     * @return
     */
    public List<Dish> getByCategoryId(Long categoryId) {

        return dishMapper.getByCategoryId(categoryId);
    }

    /**
     * 菜品起售、停售
     *
     * @param status
     * @param id
     */
    public void startOrStrop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);

        dishMapper.update(dish);
    }
}
