package com.eazytec.middleware.apm.plugin.db;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.eazytec.middleware.apm.AgentDebug;
import com.eazytec.middleware.apm.advice.CatAroundAdvice;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.PreparedStatement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MysqlClientAroundAdvice extends CatAroundAdvice {

    private volatile static boolean effective = true;

    private volatile static Field originalSqlField = null;

    public MysqlClientAroundAdvice() {
        try {
            originalSqlField = PreparedStatement.class.getDeclaredField("originalSql");
            if(null != originalSqlField){
                originalSqlField.setAccessible(true);
            }else{
                effective = false;
            }
        } catch (Exception e) {
            effective = false;
        }
    }

    @Override
    public Object invoke(String originMethodName, Object obj, Method method, Object[] args) throws Throwable {
        if (effective && obj instanceof PreparedStatement) {
            PreparedStatement ps = (PreparedStatement)obj;

            String type = "db.mysql ";
            String name = (String)originalSqlField.get(ps);

            AgentDebug.info("invoke -> %s",originMethodName);

            Transaction t = Cat.newTransaction(type,name);
            if (ps.getConnection() instanceof ConnectionImpl){
                Cat.logEvent("Sql.DataBase", ((ConnectionImpl)ps.getConnection()).getURL());
            }
            //          t.addData("FullSQL", ps.toString().split(":")[1]);
            Object o = null;
            try{
                o = method.invoke(obj,args);
                t.setStatus(Transaction.SUCCESS);
            }catch (Exception e){
                t.setStatus(e);
                Cat.logError(e);
                throw new Exception(e);
            }finally {
                t.complete();
            }
            return o;
        }else{
            return method.invoke(obj,args);
        }
    }


}
