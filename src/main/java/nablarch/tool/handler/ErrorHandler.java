package nablarch.tool.handler;

import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.launcher.CommandLine;

public class ErrorHandler implements Handler<CommandLine, Integer>{

    @Override
    public Integer handle(CommandLine cl, ExecutionContext ctx) {
        try {
            return ctx.handleNext(cl);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return 1;
        }
    }	

}
