class Main{
    public static void main(String[] a){
    }
}

class A extends B {
    int foo;
    public int Print() {
        int x; 
        x = this.foo();
        System.out.println(x);
        System.out.println(foo);
        return 0;
    }
}

class B {
    public int foo() {
        return 1;
    }
}
