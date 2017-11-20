class NullPointer {
    public static void main(String[] x) {
        A a;
        System.out.println(a.foo());
    }
}

class A{
    public int foo(){
        return 1;
    }
}
