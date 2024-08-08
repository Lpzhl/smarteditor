package hope.smarteditor.common.exception;


import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.result.Result;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLSyntaxErrorException;


/**
 * 全局异常处理器
 *
 * @author
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionHandler(BusinessException e) {
        log.error("业务异常: " + e.getMessage(), e);
        return Result.error("业务异常");
    }

    @ExceptionHandler(SQLSyntaxErrorException.class)
    public Result sqlSyntaxErrorExceptionHandler(SQLSyntaxErrorException e) {
        log.error("SQL 语法错误: " + e.getMessage(), e);
        return Result.error("数据库查询语法错误 " );
    }

    @ExceptionHandler(Exception.class)
    public Result generalExceptionHandler(Exception e) {
        log.error("运行时异常: " + e.getMessage(), e);
        return Result.error("系统异常，请联系管理员。");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常: " + e.getMessage(), e);
        return Result.error("系统异常，请联系管理员。");
    }
    @ExceptionHandler(NullPointerException.class)
    public Result nullPointerExceptionHandler(NullPointerException e) {
        log.error("空指针异常: " + e.getMessage(), e);
        return Result.error("系统错误，请联系管理员");
    }

    @ExceptionHandler(MinioException.class)
    public Result minioExceptionHandler(MinioException e) {
        log.error("Minio异常: " + e.getMessage(), e);
        return Result.error("Minio异常");
    }

    @ExceptionHandler(IOException.class)
    public Result ioExceptionHandler(IOException e) {
        log.error("IO异常: " + e.getMessage(), e);
        return Result.error("系统错误，请联系管理员");
    }

    @ExceptionHandler(NoSuchAlgorithmException.class)
    public Result noSuchAlgorithmExceptionHandler(NoSuchAlgorithmException e) {
        log.error("NoSuchAlgorithmException: " + e.getMessage(), e);
        return Result.error("系统错误，请联系管理员");
    }

    @ExceptionHandler(InvalidKeyException.class)
    public Result invalidKeyExceptionHandler(InvalidKeyException e) {
        log.error("无效密钥异常: " + e.getMessage(), e);
        return Result.error("系统错误，请联系管理员");
    }

}

