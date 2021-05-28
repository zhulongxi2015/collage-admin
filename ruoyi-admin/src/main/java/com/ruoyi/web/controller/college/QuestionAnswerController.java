package com.ruoyi.web.controller.college;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ruoyi.common.utils.StringUtils;

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
import com.ruoyi.college.domain.QuestionAnswer;
import com.ruoyi.college.service.IQuestionAnswerService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import static com.ruoyi.web.controller.constant.Constants.QUESTION_ANSWER_DETAIL_PREFIX_KEY_NAME;
import static com.ruoyi.web.controller.constant.Constants.QUESTION_ANSWER_LIST_KEY_NAME;

/**
 * 问答Controller
 *
 * @author ouyangjie
 * @date 2021-01-16
 */
@Controller
@RequestMapping("/college/answer")
public class QuestionAnswerController extends BaseController {
    private String prefix = "college/answer";

    @Autowired
    private IQuestionAnswerService questionAnswerService;


    //@RequiresPermissions("college:answer:view")
    @GetMapping()
    public String answer() {
        return prefix + "/answer";
    }

    /**
     * 查询问答列表
     */
    //@RequiresPermissions("college:answer:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(QuestionAnswer questionAnswer) {
        Object aqsObj = collageCache.get(QUESTION_ANSWER_LIST_KEY_NAME);
        if (aqsObj != null) {
            return (getDataTable((List<QuestionAnswer>) aqsObj));
        } else {
            startPage();
            List<QuestionAnswer> list = questionAnswerService.selectQuestionAnswerList(questionAnswer);
            collageCache.put(QUESTION_ANSWER_LIST_KEY_NAME, list);
            return getDataTable(list);
        }

    }

    /*门户展示列表，做一层封装，第一个为问题，后面的为答案*/
    @GetMapping("/web/info/{id}")
    @ResponseBody
    public TableDataInfo info(@PathVariable Long id) {
        Object qaObj = collageCache.get(QUESTION_ANSWER_DETAIL_PREFIX_KEY_NAME+id);
        if(qaObj!=null){
            return getDataTable((List<QuestionAnswer>)qaObj);
        }else{
            startPage();
            List<QuestionAnswer> list = new ArrayList<>();
            QuestionAnswer questionAnswer1 = questionAnswerService.selectQuestionAnswerListByQuestionId(id);
            list.add(questionAnswer1);
            collageCache.put(QUESTION_ANSWER_DETAIL_PREFIX_KEY_NAME+id, list);
            return getDataTable(list);
        }

    }

    /*门户展示列表，做一层封装，第一个为问题，后面的为答案*/
    @PostMapping("/web/list")
    @ResponseBody
    public TableDataInfo webList(QuestionAnswer questionAnswer) {
        startPage();
        List<QuestionAnswer> list = questionAnswerService.selectQuestionAnswerList(questionAnswer);
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        for (QuestionAnswer qa : list) {
            QuestionAnswer questionAnswer1 = questionAnswerService.selectQuestionAnswerListByQuestionId(qa.getId());
            questionAnswers.add(questionAnswer1);
        }
        questionAnswers = questionAnswers.stream().filter(qa -> qa.getStatus() == 0).collect(Collectors.toList());
        return getDataTable(questionAnswers);
    }

    /**
     * 导出问答列表
     */
    //@RequiresPermissions("college:answer:export")
    @Log(title = "问答", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(QuestionAnswer questionAnswer) {
        List<QuestionAnswer> list = questionAnswerService.selectQuestionAnswerList(questionAnswer);
        ExcelUtil<QuestionAnswer> util = new ExcelUtil<QuestionAnswer>(QuestionAnswer.class);
        return util.exportExcel(list, "answer");
    }

    /**
     * 新增问答
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存问答
     */
    //@RequiresPermissions("college:answer:add")
    @Log(title = "问答", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(QuestionAnswer questionAnswer) {
        return toAjax(questionAnswerService.insertQuestionAnswer(questionAnswer));
    }

    @Log(title = "问答", businessType = BusinessType.INSERT)
    @PostMapping("/web/add")
    @ResponseBody
    public AjaxResult addSave1(QuestionAnswer questionAnswer) {
        return toAjax(questionAnswerService.insertSameQuestionAnswer(questionAnswer));
    }

    /**
     * 修改问答
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {

        QuestionAnswer questionAnswer = questionAnswerService.selectQuestionAnswerListByQuestionId(id);
        mmap.put("questionAnswer", questionAnswer);
        return prefix + "/edit";
    }

    /**
     * 修改保存问答
     */
    //@RequiresPermissions("college:answer:edit")
    @Log(title = "问答", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(QuestionAnswer questionAnswer) {
        return toAjax(questionAnswerService.updateQuestionAnswer(questionAnswer));
    }

    @Log(title = "问答", businessType = BusinessType.UPDATE)
    @PostMapping("/web/edit")
    @ResponseBody
    public AjaxResult editSave1(QuestionAnswer questionAnswer) {
        return toAjax(questionAnswerService.updateSameQuestionAnswer(questionAnswer));
    }

    /**
     * 删除问答
     */
    //@RequiresPermissions("college:answer:remove")
    @Log(title = "问答", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(questionAnswerService.deleteQuestionAnswerByIds(ids));
    }
}
