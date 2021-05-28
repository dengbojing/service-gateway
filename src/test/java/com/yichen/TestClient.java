package com.yichen;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author dengbojing
 */
public class TestClient {
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    @Test
    public void test() throws InterruptedException {
        for(int i = 0; i < 10; i++){
            executor.execute(new ThreadLocalTest());
        }
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void test1(){
        String i = "1";
        this.change(i);
        System.out.println(i);
    }

    public void change(String i ){
        i = "2";
    }

}

class ThreadLocalTest implements Runnable{

    @Override
    public void run(){
        List<Integer> localList = listThreadLocal.get();
        for(int i = 0; i < 10; i++){
            localList.add(i);
        }
        localList.forEach(integer -> {
            System.out.print(integer + "--");
        });
        System.out.println();
    }
    private static ThreadLocal<List<Integer>> listThreadLocal = ThreadLocal.withInitial(() -> {
        System.out.println("init threadLocal");
        return new ArrayList<>();
    });
}
