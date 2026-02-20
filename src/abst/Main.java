package abst;

abstract class Animal {
    // 模板方法：固定流程框架（用final修饰，防止子类修改流程）
    public final void dailyActivity() {
        wakeUp();       // 固定步骤1：起床
        makeSound();    // 可变步骤1：发声（子类实现）
        move();         // 可变步骤2：移动（子类实现）
        rest();         // 固定步骤2：休息
    }

    // 具体方法：固定步骤的实现
    private void wakeUp() {
        System.out.println("起床了");
    }

    private void rest() {
        System.out.println("休息了");
    }

    // 抽象方法：可变步骤，由子类实现
    public abstract void makeSound();
    public abstract void move();
}

// 子类Dog的日常活动会严格遵循父类的流程框架
class Dog extends Animal {
    @Override
    public void makeSound() { System.out.println("汪汪叫"); }

    @Override
    public void move() { System.out.println("到处跑"); }
}

// 使用时，所有动物的活动流程完全统一
public class Main {
    public static void main(String[] args) {
        Animal dog = new Dog();
        dog.dailyActivity();
        // 输出：
        // 起床了
        // 汪汪叫
        // 到处跑
        // 休息了
    }
}