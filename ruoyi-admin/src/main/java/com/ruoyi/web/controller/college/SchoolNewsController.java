package com.ruoyi.web.controller.college;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ruoyi.college.domain.Profession;
import com.ruoyi.college.domain.School;
import com.ruoyi.college.domain.SchoolNews;
import com.ruoyi.college.service.ISchoolNewsService;

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
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import static com.ruoyi.web.controller.constant.Constants.SCHOOL_NEWS_DETAIL_PREFIX_KEY;
import static com.ruoyi.web.controller.constant.Constants.SCHOOL_NEWS_KEY_NAME;
import static com.ruoyi.web.controller.constant.Constants.SCHOOL_NEWS_LIST_PREFIX_KEY;

/**
 * 学校新闻表Controller
 *
 * @author ouyangjie
 * @date 2021-01-19
 */
@Controller
@RequestMapping("/college/news")
public class SchoolNewsController extends BaseController {
    private String prefix = "college/news";

    @Autowired
    private ISchoolNewsService schoolNewsService;

    //@RequiresPermissions("college:news:view")
    @GetMapping()
    public String news() {
        return prefix + "/news";
    }

    /**
     * 查询学校新闻表列表
     */
    //@RequiresPermissions("college:news:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SchoolNews schoolNews) {
        String key = SCHOOL_NEWS_KEY_NAME;
        Object obj = collageCache.get(key);
        if (obj != null && ((List<SchoolNews>) obj).size()>0) {
            return getDataTable((List<SchoolNews>) obj);
        } else {
            startPage();
            List<SchoolNews> list = schoolNewsService.selectSchoolNewsList(schoolNews);
            collageCache.put(key, list);
            return getDataTable(list);
        }

    }

    @GetMapping("/{id}")
    @ResponseBody
    public Object info(@PathVariable("id") Integer id, ModelMap mmap) {
        Object obj = collageCache.get(SCHOOL_NEWS_DETAIL_PREFIX_KEY + id);
        SchoolNews news;
        if (obj != null) {
            news = (SchoolNews) obj;
        } else {
            news = schoolNewsService.selectSchoolNewsById(id);
            collageCache.put(SCHOOL_NEWS_DETAIL_PREFIX_KEY + id, news);
        }
        mmap.put("news", news);
        mmap.addAttribute("news", news);
        return JSON.toJSON(news);
    }

    /**
     * 导出学校新闻表列表
     */
    //@RequiresPermissions("college:news:export")
    @Log(title = "学校新闻表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SchoolNews schoolNews) {
        List<SchoolNews> list = schoolNewsService.selectSchoolNewsList(schoolNews);
        ExcelUtil<SchoolNews> util = new ExcelUtil<SchoolNews>(SchoolNews.class);
        return util.exportExcel(list, "news");
    }

    /**
     * 新增学校新闻表
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存学校新闻表
     */
    //@RequiresPermissions("college:news:add")
    @Log(title = "学校新闻表", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(SchoolNews schoolNews) {
        int i = schoolNewsService.insertSchoolNews(schoolNews);
        addSchoolNewsToListCache(schoolNews, SCHOOL_NEWS_KEY_NAME);
        //addSchoolNewsToListCache(schoolNews, SCHOOL_NEWS_LIST_PREFIX_KEY+schoolNews.getId());
        return toAjax(i);
    }


    /**
     * 修改学校新闻表
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, ModelMap mmap) {
        SchoolNews schoolNews = schoolNewsService.selectSchoolNewsById(id);
        mmap.put("schoolNews", schoolNews);
        return prefix + "/edit";
    }

    /**
     * 修改保存学校新闻表
     */
    //@RequiresPermissions("college:news:edit")
    @Log(title = "学校新闻表", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(SchoolNews schoolNews) {
        int i = schoolNewsService.updateSchoolNews(schoolNews);
        SchoolNews updatedSchoolNews = schoolNewsService.selectSchoolNewsById(schoolNews.getId());
        collageCache.put(SCHOOL_NEWS_DETAIL_PREFIX_KEY + schoolNews.getId(), updatedSchoolNews);
        updateSchoolNewsListCache(updatedSchoolNews, SCHOOL_NEWS_KEY_NAME);
        //updateSchoolNewsListCache(updatedSchoolNews, SCHOOL_NEWS_LIST_PREFIX_KEY + schoolNews.getId());
        return toAjax(i);
    }

    /**
     * 删除学校新闻表
     */
    //@RequiresPermissions("college:news:remove")
    @Log(title = "学校新闻表", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id: Convert.toStrArray(ids)){
            collageCache.remove(SCHOOL_NEWS_DETAIL_PREFIX_KEY+ id);
            removeSchoolNewsFromListCache(id, SCHOOL_NEWS_KEY_NAME);
            removeSchoolNewsFromListCache(id, SCHOOL_NEWS_LIST_PREFIX_KEY + id);
        }

        return toAjax(schoolNewsService.deleteSchoolNewsByIds(ids));
    }

    private void addSchoolNewsToListCache(SchoolNews schoolNews, String key){
        Object obj = collageCache.get(key);
        if(obj!=null){
            List<SchoolNews> schoolNewsList = (List<SchoolNews>)obj;
            schoolNewsList.add(schoolNews);
            collageCache.put(key, schoolNewsList);
        }
    }

    private void updateSchoolNewsListCache(SchoolNews schoolNews, String key){
        Object obj = collageCache.get(key);
        if(obj!=null){
            List<SchoolNews> schoolNewsList = (List<SchoolNews>)obj;
            schoolNewsList.removeIf(p->p.getId().equals(schoolNews.getId()));
            schoolNewsList.add(schoolNews);
            collageCache.put(key , schoolNewsList);
        }
    }

    private void removeSchoolNewsFromListCache(String schoolNewsId, String key){
        Object obj = collageCache.get(key);
        if(obj!=null){
            List<SchoolNews> schoolNewsList = (List<SchoolNews>)obj;
            schoolNewsList.removeIf(p->p.getId().toString().equals(schoolNewsId));
            collageCache.put(key, schoolNewsList);;
        }
    }
}
