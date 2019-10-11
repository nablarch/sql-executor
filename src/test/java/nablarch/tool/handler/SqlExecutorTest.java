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
import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(DatabaseTestRunner.class)
public class SqlExecutorTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("db-default.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /** テスト用データベース接続 */
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

    @Test
    public void test() {
        VariousDbTestHelper.setUpTable(
        new Users(1L, "name_1", DateUtil.getDate("20140101"), getDate("20150401123456"), 9L, false),
                new Users(2L, "name_2", DateUtil.getDate("20140102"), getDate("20150402123456"), 99L, true),
                new Users(3L, "name_3", DateUtil.getDate("20140103"), getDate("20150403123456"), 999L, false)
        );
        ArrayList<String> arr= new ArrayList<String>();
        arr.add("userId");
        arr.add("2");
        SqlExecutor sqlExecutor = new SqlExecutor();
        SqlResultSet rs = sqlExecutor.executeQuery("select * from DAO_USERS where USER_ID = :userId", arr);

        assertThat(rs.get(0).getLong("USER_ID"), is(2L));
        assertThat(rs.get(0).getString("NAME"), is("name_2"));
        assertThat(rs.get(0).getDate("BIRTHDAY"), is(DateUtil.getDate("20140102")));
        assertThat(rs.get(0).getDate("INSERT_DATE"), is(getDate("20150402123456")));
        assertThat(rs.get(0).getLong("VERSION"), is(99L));
        assertThat(rs.get(0).getBoolean("active"), is(true));
    }

//    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
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