package com.cc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.common.Result;
import com.cc.dto.DishDto;
import com.cc.pojo.Category;
import com.cc.pojo.Dish;
import com.cc.service.CategoryService;
import com.cc.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜品管理 前端控制器
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;


    @PostMapping()
    public Result<DishDto> addDish(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        return null;
    }



    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        //DishDto是前端要的东西和后端的Dish不一样，要扩展一下
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝，忽略record对象，因为record就是查出来的记录数，也就是pageInfo
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        //将List集合搬入Dto中
        //这里是流式编程的内容，或者用foreach来进行搬运也可以解决
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }


    /**
     * 修改菜品信息
     * @param id 菜品id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> updateDish(@PathVariable Long id){
        //因为是直接查Dto数据嘛，用现成的肯定不行了，在Service层自己写，这是个多表联查的过程
        DishDto dishDto=dishService.getByIdWithFlavor(id);
        return Result.success(dishDto);
    }


}
