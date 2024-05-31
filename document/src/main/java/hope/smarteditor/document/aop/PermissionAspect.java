package hope.smarteditor.document.aop;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.model.entity.Permissions;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.PermissionCheck;
import hope.smarteditor.document.service.DocumentpermissionsService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private DocumentpermissionsService documentpermissionsService;

    @Around("@annotation(permissionCheck)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, PermissionCheck permissionCheck) throws Throwable {
        // 1. 获取当前请求的HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 2. 从请求头中获取userId和documentId
        Long userId = null;
        Long documentId = null;
        try {
            userId = Long.valueOf(request.getHeader("userId"));
            documentId = Long.valueOf(request.getHeader("documentId"));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 获取方法上注解的权限
        String[] requiredPermissions = permissionCheck.value();

        // 4. 从数据库中查询当前用户对文档的权限，加上异常处理
        List<Permissions> userPermissions;
        try {
            userPermissions = documentpermissionsService.getPermissionsForUserAndDocument(userId, documentId);
        } catch (Exception e) {
            return Result.error(ErrorCode.NO_AUTH_ERROR.getMessage());
        }

        // 5. 校验用户是否具有所需权限，只要userPermissions包含在requiredPermissions即可
        boolean hasRequiredPermission = Arrays.stream(requiredPermissions)
                .anyMatch(requiredPermission -> userPermissions.stream()
                        .anyMatch(userPermission -> userPermission.getPermissionName().equals(requiredPermission)));

        // 6. 如果用户没有所需的权限，不给通过，否则放行
        if (!hasRequiredPermission) {
            return Result.error(ErrorCode.NO_AUTH_ERROR.getMessage());
        }

        // 如果有权限，继续执行被拦截的方法
        return joinPoint.proceed();
    }
}
