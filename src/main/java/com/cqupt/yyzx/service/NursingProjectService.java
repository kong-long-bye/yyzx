package com.cqupt.yyzx.service;

import com.cqupt.yyzx.entity.NursingProject;
import com.cqupt.yyzx.mapper.NursingProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护理项目业务逻辑服务
 */
@Service
public class NursingProjectService {

    @Autowired
    private NursingProjectMapper projectMapper;

    /**
     * 分页查询护理项目列表
     */
    public Map<String, Object> getProjectList(String searchKeyword, String projectCategory, Integer status,
                                              Integer caregiverId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        Integer offset = (page - 1) * size;

        List<NursingProject> projects = projectMapper.selectProjectListWithDetails(
                searchKeyword, projectCategory, status, caregiverId, offset, size);
        Integer total = projectMapper.countProjects(searchKeyword, projectCategory, status, caregiverId);

        Map<String, Object> result = new HashMap<>();
        result.put("projects", projects);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    /**
     * 根据ID查询护理项目详细信息
     */
    public NursingProject getProjectById(Integer id) {
        return projectMapper.selectProjectById(id);
    }

    /**
     * 添加护理项目
     */
    @Transactional
    public boolean addProject(NursingProject project) {
        // 检查项目名称是否已存在
        if (projectMapper.checkProjectNameExists(project.getProjectName(), null) > 0) {
            return false;
        }

        // 设置默认值
        if (project.getStatus() == null) {
            project.setStatus(1); // 默认启用
        }
        if (project.getStandardDuration() == null) {
            project.setStandardDuration(30); // 默认30分钟
        }
        if (project.getFrequency() == null || project.getFrequency().trim().isEmpty()) {
            project.setFrequency("每日1次");
        }

        return projectMapper.insertProject(project) > 0;
    }

    /**
     * 更新护理项目信息
     */
    @Transactional
    public boolean updateProject(NursingProject project) {
        // 检查项目名称是否已存在（排除当前项目）
        if (projectMapper.checkProjectNameExists(project.getProjectName(), project.getId()) > 0) {
            return false;
        }

        return projectMapper.updateProject(project) > 0;
    }

    /**
     * 删除护理项目
     */
    @Transactional
    public boolean deleteProject(Integer id) {
        // 这里可以添加检查是否有护理记录关联此项目的逻辑
        return projectMapper.deleteProject(id) > 0;
    }

    /**
     * 批量删除护理项目
     */
    @Transactional
    public boolean deleteProjects(List<Integer> ids) {
        return projectMapper.deleteProjects(ids) > 0;
    }

    /**
     * 更新护理项目状态
     */
    @Transactional
    public boolean updateProjectStatus(Integer id, Integer status) {
        return projectMapper.updateProjectStatus(id, status) > 0;
    }

    /**
     * 获取所有护理项目分类
     */
    public List<String> getAllCategories() {
        return projectMapper.selectAllCategories();
    }

    /**
     * 获取护理项目统计信息
     */
    public Map<String, Object> getProjectStats() {
        List<Map<String, Object>> stats = projectMapper.getProjectStats();

        Map<String, Object> result = new HashMap<>();
        int total = 0;
        int active = 0;
        int inactive = 0;

        for (Map<String, Object> stat : stats) {
            Integer status = (Integer) stat.get("status");
            Long count = (Long) stat.get("count");
            int countInt = count.intValue();

            total += countInt;

            if (status == 1) {
                active = countInt;
            } else if (status == 0) {
                inactive = countInt;
            }
        }

        result.put("total", total);
        result.put("active", active);
        result.put("inactive", inactive);

        return result;
    }

    /**
     * 根据分类查询护理项目
     */
    public List<NursingProject> getProjectsByCategory(String category) {
        return projectMapper.selectProjectsByCategory(category);
    }

    /**
     * 根据护理人员ID查询负责的项目
     */
    public List<NursingProject> getProjectsByCaregiverId(Integer caregiverId) {
        return projectMapper.selectProjectsByCaregiverId(caregiverId);
    }

    /**
     * 检查项目名称是否可用
     */
    public boolean isProjectNameAvailable(String projectName, Integer excludeId) {
        return projectMapper.checkProjectNameExists(projectName, excludeId) == 0;
    }

    /**
     * 获取启用状态的护理项目
     */
    public List<NursingProject> getActiveProjects() {
        return projectMapper.selectActiveProjects();
    }

    /**
     * 获取护理项目的执行频率选项
     */
    public List<String> getFrequencyOptions() {
        return  Arrays.asList("每日1次", "每日2次", "每日3次", "每周2次", "每周3次", "每2小时1次", "按需执行", "按医嘱");
    }

    /**
     * 获取项目分类选项
     */
    public List<String> getCategoryOptions() {
        return  Arrays.asList("健康监护", "生活照料", "护理照料", "功能训练", "医疗护理", "心理关怀", "营养管理", "康复训练");
    }

    /**
     * 根据护理等级推荐项目
     */
    public List<NursingProject> getRecommendedProjectsByLevel(String nursingLevel) {
        return projectMapper.selectRecommendedProjectsByLevel(nursingLevel);
    }

    /**
     * 批量启用/禁用项目
     */
    @Transactional
    public boolean batchUpdateProjectStatus(List<Integer> ids, Integer status) {
        try {
            for (Integer id : ids) {
                updateProjectStatus(id, status);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败：" + e.getMessage());
        }
    }
}