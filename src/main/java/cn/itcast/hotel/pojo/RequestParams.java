package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * @Projectname: hotel-demo
 * @Filename: RequestParams
 * @Author: EdmundXie
 * @Data:2023/3/21 18:27
 * @Email: 609031809@qq.com
 * @Description:
 */
@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
}
