package hope.smarteditor.common.constant;

/**
 * 错误码
 *
 * @author lzh
 */
public enum ErrorCode {

    SUCCESS(200, "成功"),
    LOGIN_SUCCESS(200, "登录成功"),
    PARAMS_ERROR(400, "请求参数错误"),
    NOT_LOGIN_ERROR(401, "未登录"),
    NO_AUTH_ERROR(403, "无权限"),
    NOT_FOUND_ERROR(404, "请求数据不存在"),
    FORBIDDEN_ERROR(403, "禁止访问"),
    SYSTEM_ERROR(500, "系统内部异常"),
    PASSWORD_ERROR(401, "用户名或者密码错误"),
    SAVE_FILE_ERROR(500, "保存文档失败"),
    UPLOAD_FILE_ERROR(500, "上传文件失败"),
    DOWNLOAD_FILE_ERROR(500, "下载文件失败"),
    DELETE_FILE_ERROR(500, "删除文件失败"),
    UPDATE_FILE_ERROR(500, "更新文件失败"),
    SET_USER_VISBILITY_ERROR(500, "设置用户权限失败"),
    DELETE_FROM_SOURCE_FOLDER_ERROR(500, "无法从源文件夹中删除文档"),
    DOCUMENT_EXISTS_IN_TARGET_FOLDER(500, "目标文件夹中已存在文档"),
    ADD_TO_TARGET_FOLDER_ERROR(500, "未能将文档添加到目标文件夹"),
    NETWORK_ERROR(500, "网络错误"),
    OPERATION_ERROR(500, "操作失败");

    //token已过期

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
