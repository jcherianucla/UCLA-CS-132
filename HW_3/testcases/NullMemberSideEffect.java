class NullPointerMember {
    public static void main(String[] x) {
        A a;
        a = new A();
        System.out.println(a.bar());
        System.out.println(a.foo());
        System.out.println(a.bee());
    }
}

class A{
    A item;
    public int bar(){
        item = new A();
        return 1;
    }
    public int foo(){
        A item;
        return 2;
    }
    public int bee(){
        return item.bar();
    }
}
