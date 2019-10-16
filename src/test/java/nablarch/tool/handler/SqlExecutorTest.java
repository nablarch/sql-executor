package nablarch.tool.handler;

import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.DateUtil;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(DatabaseTestRunner.class)
public class SqlExecutorTest {

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
        VariousDbTestHelper.createTable(Users.class);
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
     * $ifの条件=IN句の条件のとき、正常に検索できること。
     */
    @Test
    public void test$ifInCondEquals() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("userId", "[2]", "userId", "[2]");
        SqlExecutor sqlExecutor = new SqlExecutor();
        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(userId){USER_ID in (:userId[])}", args);

        assertThat(rs.get(0).getLong("USER_ID"), is(2L));
        assertThat(rs.get(0).getString("NAME"), is("name_2"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(99L));
        assertThat(rs.get(0).getBoolean("active"), is(true));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * $ifの条件!=IN句の条件のとき、正常に検索できること。
     */
    @Test
    public void test$ifInCondNotEquals() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "[2]");
        SqlExecutor sqlExecutor = new SqlExecutor();
        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);

        assertThat(rs.get(0).getLong("USER_ID"), is(2L));
        assertThat(rs.get(0).getString("NAME"), is("name_2"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(99L));
        assertThat(rs.get(0).getBoolean("active"), is(true));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスことト。
     * <p/>
     * $ifの条件が存在しないとき、正常に検索できること。
     */
    @Test
    public void test$ifIsNotExists() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("userId", "[2]");
        SqlExecutor sqlExecutor = new SqlExecutor();
        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where USER_ID in (:userId[])", args);

        assertThat(rs.get(0).getLong("USER_ID"), is(2L));
        assertThat(rs.get(0).getString("NAME"), is("name_2"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(99L));
        assertThat(rs.get(0).getBoolean("active"), is(true));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * IN句の条件に[]が含まれていないとき、IllegalArgumentExceptionが排出され、正しいエラーメッセージが取得されること。
     */
    @Test
    public void testArrNotContainsBrackets() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "2");
        SqlExecutor sqlExecutor = new SqlExecutor();

        try {
            sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
            fail("ここはとおらない");
        } catch (IllegalArgumentException e) {
            assertEquals("object type in field is invalid. valid object type is Collection or Array. field name = [userId].", e.getMessage());
        }
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * $ifの条件の[]が閉じられていないとき、IllegalArgumentExceptionが排出され、正しいエラーメッセージが取得されること。
     */
    @Test
    public void testBracketsNotClosed() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "[2");
        SqlExecutor sqlExecutor = new SqlExecutor();

        try {
            sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
            fail("ここはとおらない");
        } catch (IllegalArgumentException e) {
            assertEquals("object type in field is invalid. valid object type is Collection or Array. field name = [userId].", e.getMessage());
        }
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * IN句の条件内に[]が含まれているとき、正常に検索できること。
     */
    @Test
    public void testCondContainsBrackets() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "[1name]", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "[2name]", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "name", "[[1name],[2name]]");
        SqlExecutor sqlExecutor = new SqlExecutor();
        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){NAME in (:name[])}", args);

        assertThat(rs.get(0).getLong("USER_ID"), is(1L));
        assertThat(rs.get(0).getString("NAME"), is("[1name]"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140101")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150401123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(9L));
        assertThat(rs.get(0).getBoolean("active"), is(false));

        assertThat(rs.get(1).getLong("USER_ID"), is(2L));
        assertThat(rs.get(1).getString("NAME"), is("[2name]"));
        assertThat(rs.get(1).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(1).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(1).getLong("VERSION"), is(99L));
        assertThat(rs.get(1).getBoolean("active"), is(true));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * IN句の条件が空のとき、IllegalArgumentExceptionが排出され、正しいエラーメッセージが取得されること。
     */
    @Test
    public void testCondIsEmpty() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "");
        SqlExecutor sqlExecutor = new SqlExecutor();

        try {
            sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
            fail("ここはとおらない");
        } catch (IllegalArgumentException e) {
            assertEquals("object type in field is invalid. valid object type is Collection or Array. field name = [userId].", e.getMessage());
        }
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * IN句の条件が空のとき、正常に検索できること（検索結果0件）。
     */
    @Test
    public void testCondArrIsEmpty() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "[]");
        SqlExecutor sqlExecutor = new SqlExecutor();

        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
        assertThat(rs.size(), is(0));

    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 空が入力された配列がIN句の条件のとき、正常に検索できること(検索結果0件)。
     */
    @Test
    public void testCondArrHasNoItem() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "[,]");
        SqlExecutor sqlExecutor = new SqlExecutor();

        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
        assertThat(rs.size(), is(0));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * IN句の条件が数値のとき、正常に検索できること。
     */
    @Test
    public void testCondIsNumber() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "２番", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "[2]");
        SqlExecutor sqlExecutor = new SqlExecutor();

        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
        assertThat(rs.get(0).getLong("USER_ID"), is(2L));
        assertThat(rs.get(0).getString("NAME"), is("２番"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(99L));
        assertThat(rs.get(0).getBoolean("active"), is(true));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * IN句の条件が文字列のとき、正常に検索できること。
     */
    @Test
    public void testCondIsString() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "name", "[name_2]");
        SqlExecutor sqlExecutor = new SqlExecutor();

        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){Name in (:name[])}", args);
        assertThat(rs.get(0).getLong("USER_ID"), is(2L));
        assertThat(rs.get(0).getString("NAME"), is("name_2"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(99L));
        assertThat(rs.get(0).getBoolean("active"), is(true));
    }

    /**
     * {@link SqlExecutor#executeQuery(String, List)}のテスト。
     * <p/>
     * 条件に合致する検索結果が存在しないとき、正常に検索できること(検索結果0件)。
     */
    @Test
    public void testHasNoResults() {
        VariousDbTestHelper.setUpTable(
                new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        List<String> args = Arrays.asList("flag", "true", "userId", "[4,5,6,7]");
        SqlExecutor sqlExecutor = new SqlExecutor();

        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where $if(flag){USER_ID in (:userId[])}", args);
        assertThat(rs.size(), is(0));
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static Date getDate(String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            // NOP
        }
        return null;
    }
}