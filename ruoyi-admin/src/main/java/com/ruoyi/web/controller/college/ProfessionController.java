package com.ruoyi.web.controller.college;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ruoyi.college.domain.School;
import com.ruoyi.college.service.ISchoolService;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.college.domain.Profession;
import com.ruoyi.college.service.IProfessionService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import static com.ruoyi.web.controller.constant.Constants.PROFESSION_LIST_KEY_NAME;
import static com.ruoyi.web.controller.constant.Constants.SCHOOL_PROFESSION_DETAIL_PREFIX_KEY;
import static com.ruoyi.web.controller.constant.Constants.SCHOOL_PROFESSION_LIST_PREFIX_KEY;

/**
 * 专业Controller
 *
 * @author ouyangjie
 * @date 2021-01-18
 */
@Controller
@RequestMapping("/college/profession")
public class ProfessionController extends BaseController {
    private String prefix = "college/profession";

    @Autowired
    private IProfessionService professionService;

    @Autowired
    private ISchoolService schoolService;

    //@RequiresPermissions("college:profession:view")
    @GetMapping()
    public String profession(ModelMap mmap) {
        mmap.put("schools", schoolService.selectSchoolList(null));
        return prefix + "/profession";
    }

    /**
     * 查询专业列表
     */
    //@RequiresPermissions("college:profession:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Profession profession) {
        String key;
        if (profession == null) {
            key = PROFESSION_LIST_KEY_NAME;
        } else {
            key = SCHOOL_PROFESSION_LIST_PREFIX_KEY + (!StringUtils.isEmpty(profession.getSchoolId()) ?
                    profession.getSchool() : profession.getSchool().getId());
        }
        Object obj = collageCache.get(key);
        if (obj != null) {
            return getDataTable((List<Profession>) obj);
        } else {
            startPage();
            List<Profession> list = professionService.selectProfessionList(profession);
            collageCache.put(key, list);
            return getDataTable(list);
        }

    }

    /**
     * 新增专业
     */
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        mmap.put("schools", schoolService.selectSchoolList(null));
        return prefix + "/add";
    }

    /**
     * 新增保存专业
     */
    //@RequiresPermissions("college:profession:add")
    @Log(title = "专业", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Profession profession) {
        return toAjax(professionService.insertProfession(profession));
    }

    /**
     * 修改专业
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        Profession profession = professionService.selectProfessionById(id);
        mmap.put("profession", profession);
        mmap.put("schools", schoolService.selectSchoolList(null));
        return prefix + "/edit";
    }

    /**
     * 专业详情
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Object info(@PathVariable("id") Long id) {
        Object obj = collageCache.get(SCHOOL_PROFESSION_DETAIL_PREFIX_KEY + id);
        Profession profession;
        if (obj != null) {
            profession = (Profession) obj;
        } else {
            profession = professionService.selectProfessionById(id);
        }
        return JSON.toJSON(profession);
    }

    /**
     * 修改保存专业
     */
    //@RequiresPermissions("college:profession:edit")
    @Log(title = "专业", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Profession profession) {

        return toAjax(professionService.updateProfession(profession));
    }

    /**
     * 删除专业
     */
    //@RequiresPermissions("college:profession:remove")
    @Log(title = "专业", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(professionService.deleteProfessionByIds(ids));
    }
}
