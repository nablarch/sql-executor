package nablarch.tool.handler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.statement.ParameterizedSqlPStatement;
import nablarch.core.db.statement.SqlPStatement;
import nablarch.core.db.statement.SqlResultSet;
import nablarch.core.db.statement.SqlRow;
import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.SystemRepository;
import nablarch.core.util.Builder;
import nablarch.core.util.FileUtil;
import nablarch.core.util.JapaneseCharsetUtil;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.handler.GlobalErrorHandler;
import nablarch.fw.launcher.CommandLine;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.HttpServerFactory;
import nablarch.fw.web.handler.HttpErrorHandler;
import nablarch.fw.web.handler.ResourceMapping;


public class SqlExecutor implements Handler<Object, Object> {

    private PrintStream out = null;
    private PrintStream err = null;

    @Override
    public Object handle(Object input, ExecutionContext ctx) {
        Map<String, String> opts = null;
        List<String> args = null;

        if (input instanceof CommandLine) {
            CommandLine commandline = (CommandLine) input;
            opts   = commandline.getParamMap();
            args   = commandline.getArgs();
            out    = System.out;
            err    = System.err;
            return main(opts, args);
        }

        if (input instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) input;
            HttpResponse response = new HttpResponse();
            args = Arrays.asList(
                       (request.getParam("args") == null) ? new String[0]
                                                          : request.getParam("args")
                   );
            opts = new HashMap<String, String>();
            for (Map.Entry<String, String[]> entry : request.getParamMap().entrySet()) {
                opts.put(entry.getKey(), entry.getValue()[0]);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
            out = new PrintStream(buffer, false, "UTF-8");
            err = new PrintStream(buffer, false, "UTF-8");
            } catch (UnsupportedEncodingException neverHappen) {
                // nothing to do with it.
            }
            Integer resultCode = main(opts, args);
            return response
                  .write(buffer.toByteArray())
                  .setContentType("text/plain; charset=UTF-8")
                  .setStatusCode((resultCode == 0) ? 200 : 400);
        }

        throw new IllegalStateException();
    }

    public Integer main(Map<String, String> opts, List<String> args) {
        Integer result;

        try {
             result = opts.containsKey("l") ? list(opts, args)
                    : opts.containsKey("s") ? show(opts, args)
                    : opts.containsKey("e") ? exec(opts, args)
                    : opts.containsKey("h") ? usage(opts, args)
                    : opts.containsKey("r") ? run(opts, args)
                    : opts.containsKey("g") ? launchGui(opts, args)
                    : (opts.size() == 2)    ? run(opts, args)
                    : usage(opts, args);
        }
        catch (RuntimeException e){
            e.printStackTrace(err);
            result = 2;
        }

        return result;
    }

    /**
     * list サブコマンド。
     * 指定したディレクトリ配下のSQLファイルの一覧と、各SQLファイル内のSQLステートメント名の一覧を表示する。
     * @param opts オプション
     * @param args 引数のリスト
     * @return ステータスコード
     */
    public Integer list(Map<String, String> opts, List<String> args) {
        String targetPath = opts.get("l");
        if (targetPath.length() == 0) {
            targetPath = "./";
        }
        File target = new File(targetPath);
        if (!target.exists()) {
            error("cannnot access [" + targetPath + "]: No such file of directory.");
            return 1;
        }
        listSqlFiles(target);
        return 0;
    }

    /**
     * show サブコマンド。
     * 指定したSQLファイル中のステートメントの一覧、もしくはステートメントの内容を表示する。
     * @param opts オプション
     * @param args 引数のリスト
     * @return ステータスコード
     */
    public Integer show(Map<String, String> opts, List<String> args) {
        String[] target   = opts.get("s").split("\\.");
        String   rootPath = opts.get("r");
        if (target.length == 1) {
            showStatementList(rootPath, target[0]);
        }
        else if (target.length == 2) {
            try {
                showStatement(rootPath, target[0], target[1]);
            }
            catch(IOException e) {
                error("cannnot open: " + target[0] + ".sql");
                e.printStackTrace(err);
                return 1;
            }
        }
        return 0;
    }

    /**
     * exec サブコマンド。
     * SQLファイル内の指定したステートメントを実行する。
     * @param opts オプション
     * @param args 引数のリスト
     * @return ステータスコード
     */
    public Integer exec(Map<String, String> opts, List<String> args) {
        String[] sqlname  = opts.get("e").split("\\.");
        String   rootPath = opts.get("r");
        if (sqlname.length != 2) {
            error("invalid arguments.");
            emit("Usage: nse SQLID.NAME [PARAM1 PARAM2 ...]");
            emit("       nse SQLID.NAME [:NAME1 VALUE1 :NAME2 VALUE2 ...]");
            return 1;
        }

        try {
            String stmt = getStatement(rootPath, sqlname[0], sqlname[1]);
            if (stmt == null) {
                error("cannot find statement [" + sqlname[1] + "] in the SQL file [" + sqlname[0] + "].");
                return 1;
            }
            execute(stmt, args);
        }
        catch (IOException e) {
            error("cannot find or open the sql file: " + sqlname[0] + ".sql");
            e.printStackTrace(err);
            return 1;
        }
        return 0;
    }

    /**
     * run サブコマンド。
     * 任意のSQL文を実行する。
     * @param opts オプション
     * @param args 引数のリスト
     * @return ステータスコード
     */
    public Integer run(Map<String, String> opts, List<String> args) {
        if (args.isEmpty()) return usage(opts, args);
        String sql = Builder.join(args, " ");
        execute(sql, Arrays.asList(new String[0]));
        return 0;
    }


    /**
     * gui サブコマンド
     * WEBベースのフロントエンドを起動する。
     * @param opts オプション
     * @param args 引数のリスト
     * @return ステータスコード
     */
    public Integer launchGui(Map<String, String> opts, List<String> args) {
        emit("");
        emit("Open the page [http://localhost:7979/index.html] in your browser.");
        emit("");

        HttpServerFactory factory = SystemRepository.get("httpServerFactory");
        if (factory == null) {
            throw new IllegalConfigurationException("could not find component. name=[httpServerFactory].");
        }

        factory.create()
        .setServletContextPath("/")
        .setPort(7979)
        .setWarBasePath("classpath://gui/")
        .addHandler(new GlobalErrorHandler())
        .addHandler(new HttpErrorHandler()
                            .setDefaultPage("5..", "servlet:///error.html")
                            .setDefaultPage("4..", "servlet:///error.html"))
        .addHandler(SystemRepository.getObject("dbConnectionManagementHandler"))
        .addHandler(SystemRepository.getObject("transactionManagementHandler"))
        .addHandler("/index.html", new Handler<HttpRequest, HttpResponse>() {
            @Override
            public HttpResponse handle(HttpRequest req, ExecutionContext res) {
                return new HttpResponse(200, "servlet:///index.html")
                        .setContentType("text/html; charset=UTF-8");
            }
        })
        .addHandler("/api", new SqlExecutor())
        .addHandler("/", new ResourceMapping("/", "servlet:///"))
        .start()
        .join();

        return 0;
    }

    /**
     * usage サブコマンド。
     * 簡単な使用方法を表示する。
     * @param opts オプション
     * @param args 引数のリスト
     * @return ステータスコード
     */
    public Integer usage(Map<String, String> opts, List<String> args) {
        emit("Usage: nse [SQL] [-l PATH] [-s SQLID] [-s SQLID.NAME] [-e SQLID.NAME]");
        emit("");
        emit("An SQL script runner supporting Nablarch-SQL syntax extention.");
        emit("");
        emit("Example:");
        emit("  nse 'SELECT * FROM USERS'");
        emit("      # executes an arbitary SQL script and shows its result.");
        emit("  nse -l");
        emit("      # lists all SQL files in current directory (recursive).");
        emit("  nse -s B11Action");
        emit("      # shows all the names of statement in the SQL file having the given SQLID.");
        emit("  nse -s B11Action.SELECT_USER_BY_ID");
        emit("      # shows SQL source code");
        emit("  nse -e B11Action.SELECT_USER_BY_ID user001 active");
        emit("      # executes the SQL statment with binding parameters. (bound by position)");
        emit("  nse -e B11Action.UPDATE_USER_LOCK_STATUS :userId user001 :lockFlg 0");
        emit("      # executes SQL statment with keyword bindings.");
        emit("  nse -g");
        emit("      # launches a GUI client.");
        emit("  nse -h");
        emit("      # shows this message.");
        emit("");
        return 0;
    }


    //------ list サブコマンドの実装 ------------//
    private void listSqlFiles(File target) {
        if (target.isDirectory()) {
          listSqlFilesInDir(target);
        }
        else if (target.isFile() && target.getName().endsWith(".sql")) {
          showSqlFile(target);
        }
    }

    private void listSqlFilesInDir(File dir) {
        for (File child : dir.listFiles()) {
            if (child.getName().startsWith(".")) continue;
            listSqlFiles(child);
        }
    }

    private void showSqlFile(File file) {
        try {
            String path = file.getCanonicalPath();
            String modName = file.getName().replaceAll(".sql$", "");

            emit("");
            emit(path);
            emitLine(path.length());

            String content = readFile(file);
            for (Map.Entry<String, String> entry : parse(content).entrySet()) {
                emit(modName + "." + entry.getKey());
            }
            emit("");
        }
        catch (IOException e) {
            e.printStackTrace(err);
        }
    }


    //------------ show サブコマンドの実装 -------------//
    private Integer showStatement(String rootPath, String fileName, String stmtName) throws IOException {
        String sql = getStatement(rootPath, fileName, stmtName);
        emit("");
        if (sql == null) {
            error("cannot find statement [" + stmtName + "] in the SQL file [" + fileName + "].");
            return 1;
        }
        emit(sql);
        emit("");
        return 0;
    }

    private String getStatement(String rootPath, String fileName, String stmtName) throws IOException {
        File sqlFile = findSqlFileById(rootPath, fileName);
        if (sqlFile == null) {
            return null;
        }
        return parse(readFile(sqlFile)).get(stmtName);
    }

    private void showStatementList(String rootPath, String fileId) {
        File sqlFile = findSqlFileById(rootPath, fileId);
        if (sqlFile != null) {
            showSqlFile(sqlFile);
        }
    }

    // ----- exec サブコマンドの実装 ---- //
    private void execute(String sql, List<String> args) {
        sql = sql.trim();

        if (isQuery(sql)) {
            executeQuery(sql, args);
        }
        else {
            executeDml(sql, args);
        }
    }

    private boolean isQuery(String sql) {
        sql = sql.replaceAll("\\s*--+.*", "").trim();
        return sql.startsWith("SELECT") || sql.startsWith("select");
    }

    private void executeDml(String sql, List<String> args) {
        AppDbConnection conn = DbConnectionContext.getConnection();
        int affectedRowCount = 0;

        emit("");
        emit(sql);
        emit("");
        emit("<<------------------------------------->>");
        emit("");

        if (usesKeywordArgs(sql)) {
            Map<String, Object> kargs = toKeywordArgs(args);
            ParameterizedSqlPStatement stmt = conn.prepareParameterizedSqlStatement(sql, kargs);
            affectedRowCount = stmt.executeUpdateByMap(kargs);
        }
        else {
            SqlPStatement stmt = conn.prepareStatement(sql);
            bindParams(stmt, args);
            affectedRowCount = stmt.executeUpdate();
        }

        emit(String.valueOf(affectedRowCount) + " rows updated.");
        emit("");
    }

    private void executeQuery(String sql, List<String> args) {
        AppDbConnection conn = DbConnectionContext.getConnection();
        SqlResultSet rs = null;

        if (usesKeywordArgs(sql)) {
            Map<String, Object> kargs = toKeywordArgs(args);
            ParameterizedSqlPStatement stmt = conn.prepareParameterizedSqlStatement(sql, kargs);
            rs = stmt.retrieve(kargs);
        }
        else {
            SqlPStatement stmt = conn.prepareStatement(sql);
            bindParams(stmt, args);
            rs = stmt.retrieve();
        }

        emit("");
        emit(sql);
        emit("");
        emit("<<------------------------------------->>");
        emit("");
        emit("> 検索結果: " + rs.size() + "件");
        emit("");

        if (rs.isEmpty()) {
            return;
        }

        Map<String, Integer> colLength = new HashMap<String, Integer>();

        for (SqlRow row : rs) {
            for (Map.Entry<String, Object> col : row.entrySet()) {
                String name = col.getKey();
                String val  = String.valueOf(col.getValue());
                int len = Math.max(dispWidth(val), dispWidth(name));
                Integer cur = colLength.get(name);
                if (cur == null) {
                    cur = 0;
                }
                colLength.put(name, Math.max(cur,len));
            }
        }


        StringBuilder colnames  = new StringBuilder();
        StringBuilder separator = new StringBuilder();
        for (String col : rs.get(0).keySet()) {
            int length = colLength.get(col);
            colnames.append(pad(col, length, ' ')).append(" ");
            separator.append(line(length)).append(" ");
        }
        emit(separator.toString());
        emit(colnames.toString());
        emit(separator.toString());

        for (SqlRow row : rs) {
            StringBuilder values = new StringBuilder();
            for (Map.Entry<String, Object> col : row.entrySet()) {
                String name = col.getKey();
                String val  = String.valueOf(col.getValue());

                values.append(pad(val, colLength.get(name), ' '))
                      .append(" ");
            }
            emit(values.toString());
        }
        emit("");
    }

    private Object evalParam(String literal) {
        if (literal.equals("SYSDATE")) {
           return new java.sql.Date(System.currentTimeMillis());
        }
        Matcher m = DATE_LITERAL.matcher(literal);
        if (m.matches()) {
             int year  = Integer.valueOf(m.group(1));
             int month = Integer.valueOf(m.group(2)) -1;
             int day   = Integer.valueOf(m.group(3));

             if (m.group(4) != null) {
                 int hour = Integer.valueOf(m.group(4));
                 int min  = Integer.valueOf(m.group(5));
                 int sec  = Integer.valueOf(m.group(6));
                 return new java.sql.Date(
                     new GregorianCalendar(year, month, day, hour, min, sec).getTimeInMillis()
                 );
             }
             else {
                 return new java.sql.Date(
                     new GregorianCalendar(year, month, day).getTimeInMillis()
                 );
             }
        }

        if (isArrayLiteral(literal)) {
            return evalArray(literal);
        }
        return literal;
    }

    private void bindParams(SqlPStatement stmt, List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String literal = params.get(i);
            stmt.setObject(i+1, evalParam(literal));
        }
    }

    /**
     * 配列リテラルであるか判定する。
     * @param literal パラメータのリテラル値
     * @return '['で開始し']'で終了する文字列の場合、真
     */
    private boolean isArrayLiteral(String literal) {
        return literal.startsWith("[") && literal.endsWith("]");
    }

    /**
     * 配列リテラルを評価する
     * @param arrayLiteral 配列リテラル
     * @return 配列
     */
    private String[] evalArray(String arrayLiteral) {
        String valuesWithComma = arrayLiteral.substring(1, arrayLiteral.length() - 1).trim();
        if (valuesWithComma.isEmpty()) {
            return null;
        }
        String[] array = valuesWithComma.split(",");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    private static Pattern DATE_LITERAL = Pattern.compile(
        "(\\d{4})-(\\d{1,2})-(\\d{1,2})(?:(?:\\s|T)(\\d{1,2})\\:(\\d{1,2})\\:(\\d{1,2}))?"
    );


    /** 名前付きパラメータのパターン（e.g. :userName）*/
    private static Pattern NAMED_PARAM_PTN = Pattern.compile("\\:[_%0-9a-zA-Z]+");

    /** 特殊記法パラメータのパターン（e.g. $if）*/
    private static Pattern FUNCTION_PTN = Pattern.compile("\\$[a-z]+");

    /** キーワードを使用しているかどうかを判定するパターンの組み合わせ */
    private static Pattern[] KEYWORD_PATTERNS = { NAMED_PARAM_PTN, FUNCTION_PTN};

    private boolean usesKeywordArgs(String sql) {
        // 文字列リテラル中に:とか$がある可能性があるので、文字列リテラルを取り除く。
        String exceptStringLiteral = sql.replaceAll("'[^']*'", "");
        for (Pattern e : KEYWORD_PATTERNS) {
             if (e.matcher(exceptStringLiteral).find()) {
                 return true;
             }
        }
        return false;
    }

    private Map<String, Object> toKeywordArgs(List<String> args) {
        Map<String, Object> kargs = new HashMap<String, Object>();

        String key = null;
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            switch (i % 2) {
            case 0:  // パラメータ名
                key = arg.startsWith(":") ? arg.substring(1) : arg;
                kargs.put(key, null);
                break;
            case 1:  // 値
                kargs.put(key, evalParam(arg));
                break;
            }
        }
        return kargs;
    }


    // ----- 共通関数 ------//
    private File findSqlFileById(String root, String id) {
        if (root == null || root.trim().length() == 0) {
            root = "./";
        }
        return findSqlFileById(new File(root), id);
    }

    private File findSqlFileById(File dir, String id) {
        File found = null;
        for (File curr : dir.listFiles()) {
            if (curr.getName().startsWith(".")) continue;
            if (curr.isFile() && (curr.getName().equals(id + ".sql"))) {
                found = curr;
                break;
            }
            if (curr.isDirectory()) {
                found = findSqlFileById(curr, id);
                if (found != null) {
                    break;
                }
            }
        }
        return found;
    }

    private Map<String, String> parse(String source) {
        Map<String, String> result = new HashMap<String, String>();
        for (String entry : source.split("(\\r?\\n)(\\s*\\r?\\n)+")) {
            Integer separatorAt = entry.indexOf('=');
            if (separatorAt < 0) continue;
            String name = entry.substring(0, separatorAt).replaceAll("--[^\\r\\n]*\\r?\\n", "").trim();
            String stmt = entry.replaceFirst("\\s*[_$a-zA-Z0-9]+\\s*=\\s*", "\n").trim();
            result.put(name, stmt);
        }
        return result;
    }

    private void emitLine(int length) {
        emit(line(length));
    }

    private String line(int length) {
        char[] buff = new char[length];
        Arrays.fill(buff, '=');
        return new String(buff);
    }

    private String pad(String str, int length, char padding) {
        if (dispWidth(str) >= length) return str;
        char[] buff = new char[length - str.length()];
        Arrays.fill(buff, padding);
        return new StringBuilder(str)
                .append(buff)
                .toString();
    }

    private int dispWidth(String str) {
        int result = str.length();
        char[] chars = str.toCharArray();
        for (char c : chars) {
            if (CHARSET_ZENKAKU.get(c)) result++;
        }
        return result;
    }

    static final BitSet CHARSET_ZENKAKU = nablarch.core.util.CharacterCheckerUtil.createCharSet(
            JapaneseCharsetUtil.getZenkakuAlphaChars()
            , JapaneseCharsetUtil.getZenkakuNumChars()
            , JapaneseCharsetUtil.getZenkakuGreekChars()
            , JapaneseCharsetUtil.getZenkakuRussianChars()
            , JapaneseCharsetUtil.getZenkakuHiraganaChars()
            , JapaneseCharsetUtil.getZenkakuKatakanaChars()
            , JapaneseCharsetUtil.getZenkakuKeisenChars()
            , JapaneseCharsetUtil.getLevel1Kanji()
            , JapaneseCharsetUtil.getLevel2Kanji()
    );

    private String readFile(File file) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(
                    new BufferedInputStream(new FileInputStream(file))
                    , "UTF-8"
            );
            StringBuilder builder = new StringBuilder();
            char[] buff = new char[4096];
            while (reader.read(buff) > 0) {
                builder.append(buff);
                buff = new char[4096];
            }
            return builder.toString();

        } finally {
            FileUtil.closeQuietly(reader);
        }
    }

    public void emit(String content) {
        out.println(content);
    }

    public void error(String content) {
        err.println("NSE: " + content);
    }
}
