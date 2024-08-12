package hope.smarteditor.user.service;

import hope.smarteditor.common.model.dto.TodoListsDTO;
import hope.smarteditor.common.model.entity.TodoLists;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.TodoListsVO;
import hope.smarteditor.common.result.Result;

import java.util.List;

/**
* @author LoveF
* @description 针对表【todo_lists】的数据库操作Service
* @createDate 2024-08-12 14:36:28
*/
public interface TodoListsService extends IService<TodoLists> {

    List<TodoListsVO> getListTodo(Long userId);

    TodoListsVO addTodo(Long userId, String title);

    String deleteTodo(Long userId, Long id);

    TodoListsVO updateTodo(TodoListsDTO todoListsDTO);
}
