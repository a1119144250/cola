package com.xiaowang.cola.user;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.xiaowang.cola.user.domain.entity.Student;
import com.xiaowang.cola.user.domain.entity.Teacher;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @description:
 * @author: wb_wangjin11@meituan.com
 * @create: 2025年09月30日 10:34
 **/
@Slf4j
public class TestDemo extends UserBaseTest {

    /**
     * 对象序列化JSON 与 JSON反序列化对象
     */
    @Test
    public void test4() {
        Student student = new Student("李四");

        // 使用hutool工具序列化JSON字符串
        String jsonStr = JSONUtil.toJsonStr(student);
        log.info("序列化后的JSON字符串：{}", jsonStr);

        // 反序列化
        Student stu = JSONUtil.toBean(jsonStr, Student.class);
        log.info("反序列化后的对象：{}", stu);

        // 序列化：对象转JSON字符串
        String json = JSON.toJSONString(student);
        System.out.println("FastJson序列化结果：" + json);

        // 反序列化：JSON字符串转对象
        Student student2 = JSON.parseObject(json, Student.class);
        System.out.println("FastJson反序列化结果：" + student2);

        // 创建Gson实例
        Gson gson = new Gson();

        // 序列化：对象转JSON字符串
        String j = gson.toJson(student);
        System.out.println("Gson序列化结果：" + j);

        // 反序列化：JSON字符串转对象
        Student student3 = gson.fromJson(json, Student.class);
        System.out.println("Gson反序列化结果：" + student3);
    }

    /**
     * 多线程查询两个接口
     */
    @Test
    public void test3() {
        // 创建一个固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 定义两个任务
        Callable<List<Student>> studentTask = this::queryStudents;
        Callable<List<Teacher>> teacherTask = this::queryTeachers;

        // 提交任务，获取Future对象
        Future<List<Student>> studentFuture = executor.submit(studentTask);
        Future<List<Teacher>> teacherFuture = executor.submit(teacherTask);

        try {
            // 等待并获取结果
            List<Student> students = studentFuture.get(); // 阻塞直到查询完成
            List<Teacher> teachers = teacherFuture.get();

            // 处理结果
            System.out.println("学生列表：");
            students.forEach(s -> System.out.println(s.getName()));

            System.out.println("老师列表：");
            teachers.forEach(t -> System.out.println(t.getName()));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            executor.shutdown();
        }
    }

    /**
     * 并发查询两个接口
     */
    @Test
    public void test2() {
        // 并发查询两个接口
        CompletableFuture<List<Student>> studentFuture = CompletableFuture.supplyAsync(this::queryStudents);
        CompletableFuture<List<Teacher>> teacherFuture = CompletableFuture.supplyAsync(this::queryTeachers);

        // 等待两个接口都查完
        CompletableFuture<Void> allDone = CompletableFuture.allOf(studentFuture, teacherFuture);

        // 处理结果
        allDone.thenRun(() -> {
            try {
                List<Student> students = studentFuture.get();
                List<Teacher> teachers = teacherFuture.get();

                System.out.println("学生列表：");
                students.forEach(s -> System.out.println(s.getName()));

                System.out.println("老师列表：");
                teachers.forEach(t -> System.out.println(t.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).join(); // 等待处理完成
    }

    public List<Student> queryStudents() {
        // 模拟耗时
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        return Arrays.asList(new Student("张三"), new Student("李四"));
    }

    public List<Teacher> queryTeachers() {
        try { Thread.sleep(700); } catch (InterruptedException e) {}
        return Arrays.asList(new Teacher("王老师"), new Teacher("赵老师"));
    }

    @Test
    public void test1() {

        BigDecimal b1 = new BigDecimal("1");
        BigDecimal b2 = new BigDecimal("1.000");

        // (只判断值是否相等)比较结果为0，表示两个BigDecimal对象相等。
        System.out.println(b1.compareTo(b2));

    }

}
