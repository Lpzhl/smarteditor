package hope.smarteditor.user.controller;

import hope.smarteditor.common.model.dto.TodoListsAddDTO;
import hope.smarteditor.common.model.dto.TodoListsDTO;
import hope.smarteditor.common.model.entity.TodoLists;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.TodoListsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("todoLists")
@Slf4j
@Api(tags = "清单接口")
public class TodoListsController {

    @Resource
    private TodoListsService todoListsService;

    /**
     * 获取清单列表
     */
    @ApiOperation(value = "获取清单列表")
    @LzhLog
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(todoListsService.getListTodo(userId));
    }

    /**
     * 新增清单
     */
    @ApiOperation(value = "新增清单")
    @LzhLog
    @GetMapping("/add")
    public Result add(HttpServletRequest request, @RequestBody TodoListsAddDTO todoListsDTO) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(todoListsService.addTodo(userId,todoListsDTO.getListName()));
    }

    /**
     * 删除清单
     */
    @ApiOperation(value = "删除清单")
    @LzhLog
    @GetMapping("/delete/{id}")
    public Result delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(todoListsService.deleteTodo(userId, id));
    }


    /**
     * 更新清单
     */
    @ApiOperation(value = "更新清单")
    @LzhLog
    @GetMapping("/update")
    public Result update(@RequestBody TodoListsDTO todoListsDTO) {
        return Result.success(todoListsService.updateTodo(todoListsDTO));
    }
}
