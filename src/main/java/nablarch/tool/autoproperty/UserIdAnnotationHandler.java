package nablarch.tool.autoproperty;

import java.util.List;

import nablarch.core.ThreadContext;
import nablarch.core.db.statement.autoproperty.FieldAnnotationHandlerSupport;
import nablarch.core.db.statement.autoproperty.UserId;

/**
 * UserIdアノテーションが設定されているフィールドにユーザIDを設定するクラス。<br>
 * ユーザIDは、{@link nablarch.core.ThreadContext}から取得した値を設定する。
 *
 * @author Hisaaki Sioiri
 * @see nablarch.core.ThreadContext#getUserId()
 */
public class UserIdAnnotationHandler extends FieldAnnotationHandlerSupport {

    /**
     * 指定されたオブジェクトにユーザIDを設定する。<br>
     *
     * @param obj オブジェクト
     */
    public void handle(Object obj) {
        List<FieldHolder<UserId>> fieldHolders = getFieldList(obj, UserId.class);

        if (fieldHolders.isEmpty()) {
            return;
        }

        final String userId = ThreadContext.getUserId();
        try {
            for (FieldHolder<UserId> fieldHolder : fieldHolders) {
                fieldHolder.getField().set(obj, userId);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("field access error.", e);
        }
    }
}