package com.ruoyi.web.controller.college;

import com.alibaba.fastjson.JSON;
import com.ruoyi.college.domain.Profession;
import com.ruoyi.college.service.IProfessionService;
import com.ruoyi.college.service.ISchoolService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.ruoyi.web.controller.constant.Constants.PROFESSION_LIST_KEY_NAME;
import static com.ruoyi.web.controller.constant.Constants.PROFESSION_RECOMMAND_LIST_KEY_NAME;
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

    @PostMapping("/recommands")
    @ResponseBody
    public TableDataInfo getRecommandPros(){
        Object obj = collageCache.get(PROFESSION_RECOMMAND_LIST_KEY_NAME);
        if(obj != null && ((List<Profession>) obj).size()>0){
            return getDataTable((List<Profession>) obj);
        }else{
            List<Profession> professions = professionService.selectRecommandedProfession();
            collageCache.put(PROFESSION_RECOMMAND_LIST_KEY_NAME, professions);
            return getDataTable(professions);
        }
    }
    /**
     * 查询专业列表
     */
    //@RequiresPermissions("college:profession:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Profession profession) {
        String key ;
        if (profession == null || StringUtils.isEmpty(profession.getSchoolId())) {
            System.out.println("=====list cache===");
            key = PROFESSION_LIST_KEY_NAME;
        } else {
            key = SCHOOL_PROFESSION_LIST_PREFIX_KEY + profession.getSchoolId();
            System.out.println("=====school list cache===");
        }
        Object obj = collageCache.get(key);
        if (obj != null && ((List<Profession>) obj).size()>0) {
            System.out.println("=====cache result ===");
            System.out.println((List<Profession>) obj);
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
        int i = professionService.insertProfession(profession);
        addProfessionToListCache(profession, PROFESSION_LIST_KEY_NAME);
        System.out.println("===addSave===");
        System.out.println(profession);
        if(profession.getSchoolId()!=null){
           addProfessionToListCache(profession, SCHOOL_PROFESSION_LIST_PREFIX_KEY+profession.getSchoolId());
        }
        return toAjax(i);
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
        int i = professionService.updateProfession(profession);
        Profession updatedProfession = professionService.selectProfessionById(profession.getId());
        collageCache.put(SCHOOL_PROFESSION_DETAIL_PREFIX_KEY + profession.getSchoolId(), updatedProfession);
        updateProfessionListCache(updatedProfession, PROFESSION_LIST_KEY_NAME);
        updateProfessionListCache(updatedProfession, SCHOOL_PROFESSION_LIST_PREFIX_KEY + updatedProfession.getSchoolId());
        return toAjax(i);
    }
    /**
     * 删除专业
     */
    //@RequiresPermissions("college:profession:remove")
    @Log(title = "专业", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        int i=0;
        for (String id : Convert.toStrArray(ids)){
            Profession profession = professionService.selectProfessionById(Long.valueOf(id));
            professionService.deleteProfessionByIds(id);
            if(collageCache.get(SCHOOL_PROFESSION_DETAIL_PREFIX_KEY+id) !=null){
                collageCache.remove(SCHOOL_PROFESSION_DETAIL_PREFIX_KEY+id);
            }
            removeProfessionFromListCache(id, PROFESSION_LIST_KEY_NAME);
            removeProfessionFromListCache(id, SCHOOL_PROFESSION_LIST_PREFIX_KEY+profession.getSchoolId());
            i++;
        }
        return toAjax(i);
    }

    private void addProfessionToListCache(Profession profession, String key){
        Object obj = collageCache.get(key);
        if(obj!=null){
            List<Profession> professionList = (List<Profession>)obj;
            professionList.add(profession);
            collageCache.put(key, professionList);
        }
    }
    private void updateProfessionListCache(Profession profession, String key){
        Object obj = collageCache.get(key);
        if(obj!=null){
            List<Profession> professionList = (List<Profession>)obj;
            professionList.removeIf(p->p.getId().equals(profession.getId()));
            professionList.add(profession);
            collageCache.put(key , professionList);
        }
    }
    private void removeProfessionFromListCache(String professionId, String key){
        Object obj = collageCache.get(key);
        if(obj!=null){
            List<Profession> professionList = (List<Profession>)obj;
            System.out.println("profession_key");
            System.out.println(professionList);
            professionList.removeIf(p->p.getId().toString().equals(professionId));
            collageCache.remove(key);
            collageCache.put(key, professionList);;
            System.out.println("=====cache==="+key);
            System.out.println(professionList);
        }
    }
}
