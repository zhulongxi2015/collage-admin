package com.ruoyi.web.controller.college;

import com.alibaba.fastjson.JSON;
import com.ruoyi.college.domain.School;
import com.ruoyi.college.service.IProfessionService;
import com.ruoyi.college.service.ISchoolService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.ruoyi.web.controller.constant.Constants.SCHOOL_DETAIL_PREFIX_KEY;
import static com.ruoyi.web.controller.constant.Constants.SCHOOL_LIST_KEY_NAME;
import static com.ruoyi.web.controller.constant.Constants.SCHOOL_LIST_WITH_PROFESSION_KEY_NAME;

/**
 * 学校表Controller
 *
 * @author ouyangjie
 * @date 2021-01-19
 */
@Controller
@RequestMapping("/college/school")
public class SchoolController extends BaseController {
    private String prefix = "college/school";

    @Autowired
    private ISchoolService schoolService;

    @Autowired
    private IProfessionService professionService;

    //@RequiresPermissions("college:school:view")
    @GetMapping()
    public String school() {
        return prefix + "/school";
    }

    /**
     * 查询学校表列表
     */
    //@RequiresPermissions("college:school:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(School school) {
        Object schoolsObj = collageCache.get(SCHOOL_LIST_KEY_NAME);
        if (schoolsObj != null && ((List<School>) schoolsObj).size() > 0) {
            System.out.println("=======list from cache======");
            return (getDataTable((List<School>) schoolsObj));
        } else {
            startPage();
            List<School> list = schoolService.selectSchoolList(school);
            collageCache.put(SCHOOL_LIST_KEY_NAME, list);
            return getDataTable(list);
        }
    }

    //带专业
    @PostMapping("/list1")
    @ResponseBody
    public TableDataInfo list1(School school) {
        Object schoolsObj = collageCache.get(SCHOOL_LIST_WITH_PROFESSION_KEY_NAME);
        if (schoolsObj != null && ((List<School>) schoolsObj).size() > 0) {
            return (getDataTable((List<School>) schoolsObj));
        } else {
            startPage();
            List<School> list = schoolService.selectSchoolListWithProfession(school);
            collageCache.put(SCHOOL_LIST_WITH_PROFESSION_KEY_NAME, list);
            return getDataTable(list);
        }
    }

    /**
     * 导出学校表列表
     */
    //@RequiresPermissions("college:school:export")
    @Log(title = "学校表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(School school) {
        List<School> list = schoolService.selectSchoolList(school);
        ExcelUtil<School> util = new ExcelUtil<School>(School.class);
        return util.exportExcel(list, "school");
    }

    /**
     * 新增学校表
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存学校表
     */
    //@RequiresPermissions("college:school:add")
    @Log(title = "学校表", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(School school) {
        Object schoolsObj = collageCache.get(SCHOOL_LIST_KEY_NAME);
        int i = schoolService.insertSchool(school);
        if (schoolsObj != null && ((List<School>) schoolsObj).size() > 0) {
            List<School> schoolList = (List<School>) schoolsObj;
            schoolList.add(school);
            collageCache.put(SCHOOL_LIST_KEY_NAME, schoolList);
        }
        return toAjax(i);
    }

    /**
     * 修改学校表
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, ModelMap mmap) {
        School school = schoolService.selectSchoolById(id);
        mmap.put("school", school);
        return prefix + "/edit";
    }

    /**
     * 学校详情
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Object info(@PathVariable("id") String id, ModelMap mmap) {
        School school;
        Object schoolObj = collageCache.get(SCHOOL_DETAIL_PREFIX_KEY + id);
        if (schoolObj != null) {
            school = (School) schoolObj;
        } else {
            school = schoolService.selectSchoolById(id);
            collageCache.put(SCHOOL_DETAIL_PREFIX_KEY + id, school);
        }
        mmap.put("school", school);
        mmap.addAttribute("school", school);
        return JSON.toJSON(school);
    }

    /**
     * 修改保存学校表
     */
    //@RequiresPermissions("college:school:edit")
    @Log(title = "学校表", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(School school) {
        int i = schoolService.updateSchool(school);
        School updatedSchool = schoolService.selectSchoolById(school.getId());
        collageCache.put(SCHOOL_DETAIL_PREFIX_KEY + school.getId(), updatedSchool);

        Object schoolsObj = collageCache.get(SCHOOL_LIST_KEY_NAME);
        if(schoolsObj != null){
            List<School> schoolList = (List<School>)schoolsObj;
            schoolList.removeIf(s->s.getId().equalsIgnoreCase(school.getId()));
            schoolList.add(school);
            collageCache.put(SCHOOL_LIST_KEY_NAME, schoolList);
        }
        return toAjax(i);
    }

    /**
     * 删除学校表
     */
    //@RequiresPermissions("college:school:remove")
    @Log(title = "学校表", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        int i = schoolService.deleteSchoolByIds(ids);

        Object schoolsObj = collageCache.get(SCHOOL_LIST_KEY_NAME);

        for (String id : Convert.toStrArray(ids)) {
            collageCache.remove(SCHOOL_DETAIL_PREFIX_KEY + id);

            if(schoolsObj != null){
                List<School> schoolList = (List<School>)schoolsObj;
                schoolList.removeIf(s->s.getId().equalsIgnoreCase(id));
                collageCache.put(SCHOOL_LIST_KEY_NAME, schoolList);
            }
        }
        return toAjax(i);
    }
}
