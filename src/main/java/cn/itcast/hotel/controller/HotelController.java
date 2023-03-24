package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Projectname: hotel-demo
 * @Filename: HotelController
 * @Author: EdmundXie
 * @Data:2023/3/21 20:42
 * @Email: 609031809@qq.com
 * @Description:
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {
    @Autowired
    private IHotelService hotelService;

    //查询list
    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams) throws IOException {
        return hotelService.search(requestParams);
    }

    //回显城市，星级，品牌名
    @PostMapping("/filters")
    public Map<String, List<String>> filter(@RequestBody RequestParams requestParams) throws IOException {
        return hotelService.filters(requestParams);
    }
}
