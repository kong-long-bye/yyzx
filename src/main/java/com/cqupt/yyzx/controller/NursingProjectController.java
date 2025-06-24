package com.cqupt.yyzx.controller;

import com.cqupt.yyzx.common.Result;
import com.cqupt.yyzx.entity.NursingProject;
import com.cqupt.yyzx.service.NursingProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 护理项目控制器
 */
@RestController
@RequestMapping("/api/nursing/project")
@CrossOrigin
public class NursingProjectController {

    @Autowired
    private NursingProjectService projectService;

    /**
     * 分页查询护理项目列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getProjectList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String projectCategory,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer caregiverId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Map<String, Object> result = projectService.getProjectList(
                    searchKeyword, projectCategory, status, caregiverId, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询护理项目列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询护理项目详细信息
     */
    @GetMapping("/{id}")
    public Result<NursingProject> getProjectById(@PathVariable Integer id) {
        try {
            NursingProject project = projectService.getProjectById(id);
            if (project != null) {
                return Result.success(project);
            } else {
                return Result.error("护理项目不存在");
            }
        } catch (Exception e) {
            return Result.error("查询护理项目信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加护理项目
     */
    @PostMapping("/add")
    public Result<String> addProject(@RequestBody NursingProject project) {
        try {
            // 基本验证
            if (project.getProjectName() == null || project.getProjectName().trim().isEmpty()) {
                return Result.error("项目名称不能为空");
            }
            if (project.getProjectCategory() == null || project.getProjectCategory().trim().isEmpty()) {
                return Result.error("项目分类不能为空");
            }

            boolean success = projectService.addProject(project);
            if (success) {
                return Result.success("添加护理项目成功");
            } else {
                return Result.error("项目名称已存在");
            }
        } catch (Exception e) {
            return Result.error("添加护理项目失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理项目信息
     */
    @PutMapping("/update")
    public Result<String> updateProject(@RequestBody NursingProject project) {
        try {
            // 基本验证
            if (project.getId() == null) {
                return Result.error("项目ID不能为空");
            }
            if (project.getProjectName() == null || project.getProjectName().trim().isEmpty()) {
                return Result.error("项目名称不能为空");
            }
            if (project.getProjectCategory() == null || project.getProjectCategory().trim().isEmpty()) {
                return Result.error("项目分类不能为空");
            }

            boolean success = projectService.updateProject(project);
            if (success) {
                return Result.success("更新护理项目成功");
            } else {
                return Result.error("项目名称已存在");
            }
        } catch (Exception e) {
            return Result.error("更新护理项目失败：" + e.getMessage());
        }
    }

    /**
     * 删除护理项目
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProject(@PathVariable Integer id) {
        try {
            boolean success = projectService.deleteProject(id);
            if (success) {
                return Result.success("删除护理项目成功");
            } else {
                return Result.error("删除护理项目失败");
            }
        } catch (Exception e) {
            return Result.error("删除护理项目失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除护理项目
     */
    @DeleteMapping("/batch")
    public Result<String> deleteProjects(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的护理项目");
            }

            boolean success = projectService.deleteProjects(ids);
            if (success) {
                return Result.success("批量删除护理项目成功");
            } else {
                return Result.error("批量删除护理项目失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除护理项目失败：" + e.getMessage());
        }
    }

    /**
     * 更新护理项目状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateProjectStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            boolean success = projectService.updateProjectStatus(id, status);
            if (success) {
                return Result.success("更新项目状态成功");
            } else {
                return Result.error("更新项目状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新项目状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有护理项目分类
     */
    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        try {
            List<String> categories = projectService.getCategoryOptions();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("查询项目分类失败：" + e.getMessage());
        }
    }

    /**
     * 获取护理项目统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getProjectStats() {
        try {
            Map<String, Object> stats = projectService.getProjectStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取项目统计失败：" + e.getMessage());
        }
    }

    /**
     * 根据分类查询护理项目
     */
    @GetMapping("/category/{category}")
    public Result<List<NursingProject>> getProjectsByCategory(@PathVariable String category) {
        try {
            List<NursingProject> projects = projectService.getProjectsByCategory(category);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error("查询分类项目失败：" + e.getMessage());
        }
    }

    /**
     * 根据护理人员ID查询负责的项目
     */
    @GetMapping("/caregiver/{caregiverId}")
    public Result<List<NursingProject>> getProjectsByCaregiverId(@PathVariable Integer caregiverId) {
        try {
            List<NursingProject> projects = projectService.getProjectsByCaregiverId(caregiverId);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error("查询护理人员项目失败：" + e.getMessage());
        }
    }

    /**
     * 检查项目名称是否可用
     */
    @GetMapping("/check-name")
    public Result<Boolean> checkProjectName(@RequestParam String projectName,
                                            @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = projectService.isProjectNameAvailable(projectName, excludeId);
            return Result.success(available);
        } catch (Exception e) {
            return Result.error("检查项目名称失败：" + e.getMessage());
        }
    }

    /**
     * 获取启用状态的护理项目
     */
    @GetMapping("/active")
    public Result<List<NursingProject>> getActiveProjects() {
        try {
            List<NursingProject> projects = projectService.getActiveProjects();
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error("查询启用项目失败：" + e.getMessage());
        }
    }

    /**
     * 获取执行频率选项
     */
    @GetMapping("/frequency-options")
    public Result<List<String>> getFrequencyOptions() {
        try {
            List<String> options = projectService.getFrequencyOptions();
            return Result.success(options);
        } catch (Exception e) {
            return Result.error("查询频率选项失败：" + e.getMessage());
        }
    }

    /**
     * 根据护理等级推荐项目
     */
    @GetMapping("/recommend")
    public Result<List<NursingProject>> getRecommendedProjects(@RequestParam String nursingLevel) {
        try {
            List<NursingProject> projects = projectService.getRecommendedProjectsByLevel(nursingLevel);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error("查询推荐项目失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新项目状态
     */
    @PutMapping("/batch-status")
    public Result<String> batchUpdateProjectStatus(@RequestBody Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) data.get("ids");
            Integer status = (Integer) data.get("status");

            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的项目");
            }

            boolean success = projectService.batchUpdateProjectStatus(ids, status);
            if (success) {
                return Result.success("批量更新状态成功");
            } else {
                return Result.error("批量更新状态失败");
            }
        } catch (Exception e) {
            return Result.error("批量更新状态失败：" + e.getMessage());
        }
    }
}