package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 新增套餐
        setmealMapper.insert(setmeal);

        // 新增套餐菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insert(setmealDishes);
        }
    }

    /**
     * 根据主键 id 批量删除套餐
     *
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        // 判断套餐是否能被删除-- 是否在售中的套餐？
        // 根据主键集合查询套餐起售 id 集合
        List<Long> setmealStartIds = setmealMapper.getStatusByIds(ids);

        // 如果全部为停售，执行批量删除操作
        if (setmealStartIds.isEmpty()) {
            // 批量删除套餐信息
            setmealMapper.deleteBatch(ids);
            // 批量删除套餐菜品关联信息
            setmealDishMapper.deleteBatch(ids);
        } else {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
    }

    /**
     * 修改陶安
     *
     * @param setmealDTO 修改套餐信息
     */
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 修改套餐信息
        setmealMapper.update(setmeal);

        // 修改套餐菜品关联信息
        // 删除原先菜品关联信息
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        // 判断新增修改菜品关联信息是否为空
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (!setmealDishes.isEmpty()) {
            // 不为空，新增修改菜品关联信息
            // 添加字段 setmealId
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insert(setmealDishes);
        }
    }

    /**
     * 套餐起售、停售
     *
     * @param status 状态
     * @param id     套餐 id
     */
    public void startOrStop(Integer status, Long id) {
        // 如果改为起售，判断该套餐中是否含有未起售餐品
        if (status.equals(StatusConstant.ENABLE)) {
            // 获取关联菜品信息
            List<Long> dishIds = setmealDishMapper.getDishIdsBySetmealId(id);
            // 遍历菜品，判断是否含有未起售菜品
            for (Long dishId : dishIds) {
                if (dishMapper.getById(dishId).getStatus().equals(StatusConstant.DISABLE)) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        // 修改套餐状态
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    public SetmealVO getById(Long id) {
        // 根据主键 id 获取套餐信息, 并添加 category_id 字段
        SetmealVO setmealVO = setmealMapper.getSetmealVOById(id);

        // 根据套餐 id 获取关联菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);


        return setmealVO;
    }

    /**
     * 条件查询套餐信息
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {

        return setmealMapper.list(setmeal);
    }

    /**
     * 根据套餐 id 查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {

        return setmealMapper.getDishItemById(id);
    }
}
