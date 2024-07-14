package hope.smarteditor.document.controller;

import hope.smarteditor.common.model.entity.Docu;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.service.DocuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doc")
@Api("文献搜索")
public class DocuControler {
    @Autowired
    private DocuService docuService;

    @GetMapping("/getCategories")
    @LzhLog
    @ApiOperation(value = "获取分类")
    public Map<String, List<String>> getCategories() {
        List<String> professions = docuService.getProfessionCategories();
        List<String> subjects = docuService.getSubjectCategories();

        Map<String, List<String>> categories = new HashMap<>();
        categories.put("professions", professions);
        categories.put("subjects", subjects);
        System.out.println("categories = " + categories);
        return categories;
    }

    @GetMapping("/getArticles")
    @ApiOperation(value = "获取文献")
    @LzhLog
    public List<Docu> getArticles() {
        return docuService.getList();
    }

    /**
     * 全局模糊搜索
     */
    @GetMapping("/search")
    @ApiOperation(value = "全局模糊搜索")
    @LzhLog
    public List<Docu> search(@RequestParam String keyword) {
        System.out.println("keyword = " + keyword);
        return docuService.search(keyword);
    }

}
