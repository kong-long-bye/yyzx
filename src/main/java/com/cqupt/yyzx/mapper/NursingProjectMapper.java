package com.cqupt.yyzx.mapper;

import com.cqupt.yyzx.entity.NursingProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 护理项目数据访问接口
 */
@Mapper
public interface NursingProjectMapper {

    /**
     * 分页查询护理项目列表（包含护理人员信息）
     */
    List<NursingProject> selectProjectListWithDetails(@Param("searchKeyword") String searchKeyword,
                                                      @Param("projectCategory") String projectCategory,
                                                      @Param("status") Integer status,
                                                      @Param("caregiverId") Integer caregiverId,
                                                      @Param("offset") Integer offset,
                                                      @Param("limit") Integer limit);

    /**
     * 查询护理项目总数
     */
    Integer countProjects(@Param("searchKeyword") String searchKeyword,
                          @Param("projectCategory") String projectCategory,
                          @Param("status") Integer status,
                          @Param("caregiverId") Integer caregiverId);

    /**
     * 根据ID查询护理项目详细信息
     */
    NursingProject selectProjectById(@Param("id") Integer id);

    /**
     * 根据项目名称查询护理项目
     */
    NursingProject selectProjectByName(@Param("projectName") String projectName);

    /**
     * 插入新护理项目
     */
    int insertProject(NursingProject project);

    /**
     * 更新护理项目信息
     */
    int updateProject(NursingProject project);

    /**
     * 删除护理项目
     */
    int deleteProject(@Param("id") Integer id);

    /**
     * 批量删除护理项目
     */
    int deleteProjects(@Param("ids") List<Integer> ids);

    /**
     * 更新护理项目状态
     */
    int updateProjectStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 获取所有护理项目分类
     */
    List<String> selectAllCategories();

    /**
     * 获取护理项目统计信息
     */
    List<java.util.Map<String, Object>> getProjectStats();

    /**
     * 根据分类查询护理项目
     */
    List<NursingProject> selectProjectsByCategory(@Param("category") String category);

    /**
     * 根据护理人员ID查询负责的项目
     */
    List<NursingProject> selectProjectsByCaregiverId(@Param("caregiverId") Integer caregiverId);

    /**
     * 检查项目名称是否已存在
     */
    Integer checkProjectNameExists(@Param("projectName") String projectName, @Param("excludeId") Integer excludeId);

    /**
     * 获取启用状态的护理项目
     */
    List<NursingProject> selectActiveProjects();

    /**
     * 获取护理项目的执行频率选项
     */
    List<String> selectFrequencyOptions();

    /**
     * 根据护理等级推荐项目
     */
    List<NursingProject> selectRecommendedProjectsByLevel(@Param("nursingLevel") String nursingLevel);
}