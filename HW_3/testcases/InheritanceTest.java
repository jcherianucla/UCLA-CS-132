class Inheritance {
    public static void main(String[] x) {
        System.out.println((new C().goo(new B())));
    }
}

class C extends B{
    public int bar(){
        return 3;
    }
    public int goo(A a){
        int x;
        System.out.println(2);
        x = this.eek();
        System.out.println(a.boo());
        return x;
    }
}

class B extends A{
    public int eek(){
        return this.bee();
    }
    public int boo(){
        return 4;
    }
}

class A {
    public int bee() {
        return this.far();
    }
    public int bar(){
        return 2;
    }
    public int far() {
        return this.bar();
    }
    public int boo(){
        return 2;
    }
}
