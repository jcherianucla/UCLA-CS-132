class NullPointerMember {
    public static void main(String[] x) {
        A a;
        a = new A();
        System.out.println(a.foo());
    }
}

class A{
    A item;
    public int bar(){ return 1; }
    public int foo(){
        return item.bar();
    }
}
