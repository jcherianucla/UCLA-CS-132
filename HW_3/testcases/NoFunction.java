class NullPointerMember {
    public static void main(String[] x) {
        A a;
        a = new A();
        System.out.println(a.foo());
    }
}

class A{
    public boolean foo(){
        return true;
    }
}
