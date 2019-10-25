package nablarch.tool;

/**
 * パラメータの指定方法が誤っていることを表す例外クラス。
 */
public class IllegalInputItemException extends RuntimeException {

    /**
     * コンストラクタ。
     * @param literal パラメータのリテラル表現
     * @param cause 元の例外
     */
    public IllegalInputItemException(String literal, Throwable cause) {
        super("パラメータの指定方法が正しくありません。 [" + literal + "]", cause);
    }
}
