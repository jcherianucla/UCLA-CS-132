class VeryVeryBasic {
    public static void main(String[] a) {
    }
}

class A {
    int x;
    boolean b;
}

class B extends A{
    public int x(){ return x; }
}

class C extends B{
    public int y(){
        boolean x;
        x = !b;
        return this.x();
    }
}
