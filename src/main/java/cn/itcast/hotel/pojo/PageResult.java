package cn.itcast.hotel.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Projectname: hotel-demo
 * @Filename: PageResult
 * @Author: EdmundXie
 * @Data:2023/3/21 20:45
 * @Email: 609031809@qq.com
 * @Description:
 */
@Data
public class PageResult {
    private Long total;//总条数
    private List<HotelDoc> hotels;//酒店数据
}
