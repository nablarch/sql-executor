package nablarch.tool.autoproperty;

import java.util.List;

import nablarch.core.ThreadContext;
import nablarch.core.db.statement.autoproperty.FieldAnnotationHandlerSupport;
import nablarch.core.db.statement.autoproperty.RequestId;

/**
 * RequestIdアノテーションが設定されているフィールドにリクエストIDを設定するクラス。<br>
 * リクエストIDは、{@link nablarch.core.ThreadContext}から取得した値を設定する。
 *
 * @author Kiyohito Itoh
 * @see nablarch.core.ThreadContext#getRequestId()
 */
public class RequestIdAnnotationHandler extends FieldAnnotationHandlerSupport {

    /**
     * 指定されたオブジェクトにリクエストIDを設定する。
     * @param obj オブジェクト
     */
    public void handle(Object obj) {
        final List<FieldHolder<RequestId>> fieldHolders = getFieldList(obj, RequestId.class);

        if (fieldHolders.isEmpty()) {
            return;
        }

        final String requestId = ThreadContext.getRequestId();
        try {
            for (FieldHolder<RequestId> fieldHolder : fieldHolders) {
                fieldHolder.getField().set(obj, requestId);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("field access error.", e);
        }
    }
}
