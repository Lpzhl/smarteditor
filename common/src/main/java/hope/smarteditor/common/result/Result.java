package hope.smarteditor.common.result;

import hope.smarteditor.common.constant.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 200;
        result.msg = ErrorCode.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        result.msg = ErrorCode.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<T>();
        result.msg = ErrorCode.SUCCESS.getMessage();
        result.code = 200;
        return result;
    }
    public static Result error(String msg, int code) {
        Result result = new Result();
        result.msg =msg;
        result.code = code;
        return result;
    }

    public static <T> Result<T> success(T object, int code,String msg) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.msg =msg;
        result.code = code;
        return result;
    }
}
