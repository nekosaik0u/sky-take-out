package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.save(dishDTO);

        // 清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询");

        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除: {}", ids);
        dishService.deleteBatch(ids);

        // 将所有的菜品缓存数据清理掉，所有以 dish_ 开头的 key
        cleanCache("dish_*");

        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.update(dishDTO);

        // 将所有的菜品缓存数据清理掉，所有以 dish_ 开头的 key
        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品： {}", id);
        DishVO dishVO = dishService.getById(id);

        return Result.success(dishVO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        log.info("根据分类 id 查询菜品: {}", categoryId);
        List<Dish> dishes =dishService.getByCategoryId(categoryId);

        return Result.success(dishes);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("菜品起售、停售: {}", status);
        dishService.startOrStrop(status, id);


        Set keys = redisTemplate.keys("dish_*");
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        // 将所有的缓存数据清理掉
        Set keys = redisTemplate.keys(pattern);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }
}
