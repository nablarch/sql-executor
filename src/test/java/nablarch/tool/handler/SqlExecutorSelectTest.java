package nablarch.tool.handler;

import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.DateUtil;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.TargetDb;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(DatabaseTestRunner.class)
public class SqlExecutorSelectTest {

    private SqlExecutor sqlExecutor = new SqlExecutor();
    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("db-default.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * テスト用データベース接続
     */
    private TransactionManagerConnection connection;

    @BeforeClass
    public static void setUpClass() throws Exception {
        VariousDbTestHelper.createTable(Members.class);
    }

    @Before
    public void setUp() {
        ConnectionFactory connectionFactory = repositoryResource.getComponent("connectionFactory");
        connection = connectionFactory.getConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY);
        DbConnectionContext.setConnection(connection);
    }

    @After
    public void tearDown() {
        DbConnectionContext.removeConnection();
        try {
            connection.terminate();
        } catch (Exception ignored) {
        }
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 全ての型のデータがカラムにマッピングできること。(数値はすべて整数)
     */
    @Test
    public void testSelectInteger() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal(1.1111111111), 12, (float) 13.3, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "string2", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal(2), 22, (float) 23, 244, (short) 52, true, false, true, false),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal(3.1111111111), 32, (float) 33.3, 344.4, (short) 53, true, true, true, true)
        );

        SqlResultSet rs = sqlExecutor.executeQuery(
                "select * from DAO_MEMBERS " +
                        "where MEMBER_ID = :id " +
                        "and STRING_COL = :stringCol " +
                        "and DATE_COL = :dateCol " +
                        "and BIG_DECIMAL_COL = :bigDecimalCol " +
                        "and INTEGER_COL = :integerCol " +
                        "and FLOAT_COL = :floatCol " +
                        "and DOUBLE_COL = :doubleCol " +
                        "and SHORT_COL = :shortCol " +
                        "and bool1 = :bool1 " +
                        "and bool2 = :bool2 " +
                        "and bool3 = :bool3 " +
                        "and bool4 = :bool4 "
                , Arrays.asList(
                        "id", "2",
                        "stringCol", "'string2'",
                        "dateCol", "2014-02-02",
                        "bigDecimalCol", "2",
                        "integerCol", "22",
                        "floatCol", "23",
                        "doubleCol", "244",
                        "shortCol", "52",
                        "bool1", "true",
                        "bool2", "false",
                        "bool3", "TRUE",
                        "bool4", "FaLsE"
                ));

        assertThat(rs.get(0).getLong("MEMBER_ID"), is(2L));
        assertThat(rs.get(0).getString("STRING_COL"), is("string2"));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 数値型のカラムに少数が入力されていた時に正常に検索できる。
     * 'true'という文字列が条件の場合、正常に検索できる。
     */
    @Test
    public void testSelectDecimal() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal("1.11111"), 12, (float) 13.5, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "true", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal("2.1111111111"), 22, (float) 23.5, 244.4, (short) 52, true, true, true, true),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal("3.1111111111"), 32, (float) 33.5, 344.4, (short) 53, true, true, true, true)
        );
        SqlResultSet rs = sqlExecutor.executeQuery(
                "select * from DAO_MEMBERS " +
                        "where MEMBER_ID = :id " +
                        "and STRING_COL = :stringCol " +
                        "and BIG_DECIMAL_COL = :bigDecimalCol " +
                        "and FLOAT_COL = :floatCol " +
                        "and DOUBLE_COL = :doubleCol "
                , Arrays.asList("id", "2",
                        "stringCol", "'true'",
                        "bigDecimalCol", "2.1111111111",
                        "floatCol", "23.5",
                        "doubleCol", "244.4"
                ));

        assertThat(rs.get(0).getLong("MEMBER_ID"), is(2L));
        assertThat(rs.get(0).getString("STRING_COL"), is("true"));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * TimeStamp型の検索条件で検索された時に正常に検索できる。
     */
    @Test
    @TargetDb(exclude = TargetDb.Db.POSTGRE_SQL)
    public void testSelectByTimestamp() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal(1.1111111111), 12, (float) 13.3, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "string2", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal(2), 22, (float) 23, 244, (short) 52, true, false, true, false),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal(3.1111111111), 32, (float) 33.3, 344.4, (short) 53, true, true, true, true)
        );
        List<String> args = Arrays.asList("id", "2", "timestampCol", "2015-04-01 22:22:56");
        SqlExecutor sqlExecutor = new SqlExecutor();
        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_MEMBERS " +
                "where MEMBER_ID = :id and TIMESTAMP_COL = :timestampCol ", args);

        assertThat(rs.get(0).getLong("MEMBER_ID"), is(2L));
        assertThat(rs.get(0).getString("STRING_COL"), is("string2"));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 検索文字列に'（クォーテーション）が含まれているとき、正常に検索できる。
     */
    @Test
    public void testStringIncludeQuotation() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal(1.1111111111), 12, (float) 13.3, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "strin'g'2", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal(2.1111111111), 22, (float) 23.3, 244.4, (short) 52, true, true, true, true),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal(3.1111111111), 32, (float) 33.3, 344.4, (short) 53, true, true, true, true)
        );
        List<String> args = Arrays.asList("id", "2", "stringCol", "'strin'g'2'");
        SqlExecutor sqlExecutor = new SqlExecutor();

        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_MEMBERS where MEMBER_ID = :id and STRING_COL = :stringCol", args);
        assertThat(rs.get(0).getLong("MEMBER_ID"), is(2L));
        assertThat(rs.get(0).getString("STRING_COL"), is("strin'g'2"));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 文字列の入力に'（クォーテーション）が無いとき、NumberFormatExceptionが送出されること。
     */
    @Test
    public void testNoQuotation() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal(1.1111111111), 12, (float) 13.3, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "string2", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal(2.1111111111), 22, (float) 23.3, 244.4, (short) 52, true, true, true, true),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal(3.1111111111), 32, (float) 33.3, 344.4, (short) 53, true, true, true, true)
        );
        List<String> args = Arrays.asList("id", "2", "stringCol", "string2");
        SqlExecutor sqlExecutor = new SqlExecutor();
        try {
            sqlExecutor.executeQuery("select * from DAO_MEMBERS where MEMBER_ID = :id and STRING_COL = :stringCol", args);
            fail("ここはとおらない");
        } catch (NumberFormatException e) {
            assertEquals(NumberFormatException.class, e.getClass());
        }
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 文字列の入力の'（クォーテーション）が一部しか付与されていないとき、NumberFormatExceptionが送出されること。
     */
    @Test
    public void testNotEnoughQuotation() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal(1.1111111111), 12, (float) 13.3, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "string2", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal(2.1111111111), 22, (float) 23.3, 244.4, (short) 52, true, true, true, true),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal(3.1111111111), 32, (float) 33.3, 344.4, (short) 53, true, true, true, true)
        );
        List<String> args = Arrays.asList("id", "2", "stringCol", "'string2");
        SqlExecutor sqlExecutor = new SqlExecutor();

        try {
            sqlExecutor.executeQuery("select * from DAO_MEMBERS where MEMBER_ID = :id and STRING_COL = :stringCol", args);
            fail("ここはとおらない");
        } catch (NumberFormatException e) {
            assertEquals(NumberFormatException.class, e.getClass());
        }
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 文字列の入力が無いとき、NumberFormatExceptionが送出されること。
     */
    @Test
    public void testStringHasNoContent() {
        VariousDbTestHelper.setUpTable(
                new Members(1L, "string1", DateUtil.getDate("20140101"), getDate("20150401121156"), new BigDecimal(1.1111111111), 12, (float) 13.3, 144.4, (short) 51, true, true, true, true),
                new Members(2L, "", DateUtil.getDate("20140202"), getDate("20150401222256"), new BigDecimal(2.1111111111), 22, (float) 23.3, 244.4, (short) 52, true, true, true, true),
                new Members(3L, "string3", DateUtil.getDate("20140303"), getDate("20150401123356"), new BigDecimal(3.1111111111), 32, (float) 33.3, 344.4, (short) 53, true, true, true, true)
        );
        List<String> args = Arrays.asList("id", "2", "stringCol", "");
        SqlExecutor sqlExecutor = new SqlExecutor();

        try {
            sqlExecutor.executeQuery("select * from DAO_MEMBERS where MEMBER_ID = :id and STRING_COL = :stringCol", args);
            fail("ここはとおらない");
        } catch (NumberFormatException e) {
            assertEquals(NumberFormatException.class, e.getClass());
        }
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

    private static Date getDate(String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void assertDateEquals(Date expected, Date actual) {
        if (actual instanceof Timestamp) {
            expected = new Timestamp(expected.getTime());
        }
        assertThat(actual, is(expected));

    }
}