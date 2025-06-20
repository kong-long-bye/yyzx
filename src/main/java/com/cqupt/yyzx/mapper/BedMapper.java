package com.cqupt.yyzx.mapper;


import com.cqupt.yyzx.entity.Bed;
import com.cqupt.yyzx.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 床位数据访问接口
 */
@Mapper
public interface BedMapper {

    /**
     * 分页查询床位列表（包含房间和客户信息）
     */
    List<Bed> selectBedListWithDetails(@Param("searchKeyword") String searchKeyword,
                                       @Param("status") String status,
                                       @Param("floorNumber") Integer floorNumber,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 查询床位总数
     */
    Integer countBeds(@Param("searchKeyword") String searchKeyword,
                      @Param("status") String status,
                      @Param("floorNumber") Integer floorNumber);

    /**
     * 根据ID查询床位详细信息
     */
    Bed selectBedById(@Param("id") Integer id);

    /**
     * 根据床位号查询床位
     */
    Bed selectBedByNumber(@Param("bedNumber") String bedNumber);

    /**
     * 插入新床位
     */
    int insertBed(Bed bed);

    /**
     * 更新床位信息
     */
    int updateBed(Bed bed);

    /**
     * 删除床位
     */
    int deleteBed(@Param("id") Integer id);

    /**
     * 批量删除床位
     */
    int deleteBeds(@Param("ids") List<Integer> ids);

    /**
     * 更新床位状态
     */
    int updateBedStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 查询所有房间信息
     */
    List<Room> selectAllRooms();

    /**
     * 根据房间ID查询床位列表
     */
    List<Bed> selectBedsByRoomId(@Param("roomId") Integer roomId);

    /**
     * 床位状态统计
     */
    List<java.util.Map<String, Object>> getBedStatusStats();

    /**
     * 检查床位号是否已存在
     */
    Integer checkBedNumberExists(@Param("bedNumber") String bedNumber, @Param("excludeId") Integer excludeId);
}