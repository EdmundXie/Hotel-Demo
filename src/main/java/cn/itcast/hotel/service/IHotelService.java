package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {

    PageResult search(RequestParams requestParams) throws IOException;
    Map<String, List<String>> filters(RequestParams requestParams);

    List<String> suggestions(RequestParams requestParams);
    void insertById(Long id);
    void deleteById(Long id);
}
