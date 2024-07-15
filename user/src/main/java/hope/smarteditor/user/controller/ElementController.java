package hope.smarteditor.user.controller;


import hope.smarteditor.common.model.dto.ElementDTO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.ElementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/element")
@Api(tags = "素材相关")
public class ElementController {

    @Autowired
    private ElementService elementService;


    /**
     *首页展示素材
     */
    @GetMapping("index")
    @LzhLog
    @ApiOperation(value = "首页展示素材")
    public Result getIndexElement(){
        return Result.success(elementService.getIndexElement());
    }

    /**
     * 根据素材Id删除素材
     */
    @GetMapping("delete/{id}")
    @LzhLog
    @ApiOperation(value = "根据素材Id删除素材")
    public Result deleteElement(@PathVariable("id") String id){
        return Result.success(elementService.deleteElement(id));
    }

    /**
     * 获取某个用户的所有素材
     */
    @GetMapping("user/{userId}")
    @LzhLog
    @ApiOperation(value = "获取某个用户的所有素材")

    public Result getUserElement(@PathVariable("userId") Long userId){
        return Result.success(elementService.getUserElement(userId));
    }

    /**
     * 用户上传素材
     */
    @PostMapping("/upload")
    @LzhLog
    @ApiOperation(value = "用户上传素材")
    public Result uploadElement(@RequestBody ElementDTO elementDTO){
        return Result.success(elementService.uploadElement(elementDTO));
    }


    /**
     * 用户编辑素材
     */
    @PostMapping("/edit")
    @LzhLog
    @ApiOperation(value = "用户编辑素材")
    public Result editElement(@RequestBody ElementDTO elementDTO){
        return Result.success(elementService.editElement(elementDTO));
    }

    /**
     * 将素材添加为自己的素材
     */
    @PostMapping("add/{id}")
     @LzhLog
     @ApiOperation(value = "将素材添加为自己的素材")
     public Result addElement(@PathVariable ("id") Long id, HttpServletRequest request){
         Long userId = (Long) request.getSession().getAttribute("userId");
         return Result.success(elementService.addElement(id,userId));
     }

}
