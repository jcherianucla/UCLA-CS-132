class Polymorphism {
    public static void main(String[] x) {
        System.out.println(new A().foo(new B(), new C(), new D(), new E()));
    }
}

class A{
    public int print(){
        System.out.println(1);
        return 1;
    }
    public int foo(A a1, A a2, A a3, A a4){
        System.out.println(a1.print());
        System.out.println(a2.print());
        System.out.println(a3.print());
        System.out.println(a4.print());
        return 0;
    }
}

class B extends A{
    public int print(){
        System.out.println(1);
        return 1;
    }
}

class C extends B{
    public int print(){
        System.out.println(2);
        return 2;
    }

}

class D extends C{
}

class E extends D{
    public int print(){
        System.out.println(4);
        return 4;
    }
}
