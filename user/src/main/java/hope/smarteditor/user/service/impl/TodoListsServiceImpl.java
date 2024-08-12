package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.TodoListsDTO;
import hope.smarteditor.common.model.entity.TodoLists;
import hope.smarteditor.common.model.vo.TodoListsVO;
import hope.smarteditor.user.service.TodoListsService;
import hope.smarteditor.user.mapper.TodoListsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author LoveF
* @description 针对表【todo_lists】的数据库操作Service实现
* @createDate 2024-08-12 14:36:28
*/
@Service
public class TodoListsServiceImpl extends ServiceImpl<TodoListsMapper, TodoLists>
    implements TodoListsService{

    @Resource
    private TodoListsMapper todoListsMapper;

    @Override
    public List<TodoListsVO> getListTodo(Long userId) {
        LambdaQueryWrapper<TodoLists> todoListsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        todoListsLambdaQueryWrapper.eq(TodoLists::getUserId, userId);

        // 查询待办列表
        List<TodoLists> todoLists = todoListsMapper.selectList(todoListsLambdaQueryWrapper);

        // 使用stream将List<TodoLists>转化为List<TodoListsVO>
        List<TodoListsVO> todoListsVOList = todoLists.stream().map(todoList -> {
            TodoListsVO todoListsVO = new TodoListsVO();
            BeanUtils.copyProperties(todoList, todoListsVO);
            return todoListsVO;
        }).collect(Collectors.toList());

        return todoListsVOList;
    }

    @Override
    public TodoListsVO addTodo(Long userId, String name) {
        TodoLists todoLists = new TodoLists();
        todoLists.setUserId(userId);
        todoLists.setListName(name);
        todoListsMapper.insert(todoLists);
        TodoListsVO todoListsVO = new TodoListsVO();
        BeanUtils.copyProperties(todoLists, todoListsVO);
        return todoListsVO;
    }

    @Override
    public String deleteTodo(Long userId, Long id) {
        LambdaQueryWrapper<TodoLists> todoListsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        todoListsLambdaQueryWrapper.eq(TodoLists::getUserId, userId).eq(TodoLists::getListId, id);
        todoListsMapper.delete(todoListsLambdaQueryWrapper);
        return "删除成功";
    }

    @Override
    public TodoListsVO updateTodo(TodoListsDTO todoListsDTO) {
        TodoLists todoLists = new TodoLists();
        BeanUtils.copyProperties(todoListsDTO, todoLists);
        todoListsMapper.updateById(todoLists);

        // 再次查询获取最新的完整数据
        TodoLists updatedTodoLists = todoListsMapper.selectById(todoListsDTO.getListId());

        TodoListsVO todoListsVO = new TodoListsVO();
        BeanUtils.copyProperties(updatedTodoLists, todoListsVO);

        return todoListsVO;
    }


}




