class VeryBasic{
	public static void main(String[] a){
		System.out.println(new A().run(1, 2, new Foo()));
		System.out.println(new A().run(1,2,new Foo()));
		System.out.println(
                (new Foo().call(new A()))[0]
        );
	}
}

class A extends Foo {
    int z;
    int p;
    public int foo() { return 2; }
    public int run(int a, int z, Foo c) {
        Foo f;
        int[] y;
        int x;
        int l;
        boolean b;
        f = new A();
        z = 3;
        p = 5;
        System.out.println(f.a(3));
        l = ((y.length) + z) + (this.foo());
        {
            x = 2;
            y[2] = 1;
            b = x < l;
        }
        (new int[5])([1+2]);
        return x;
    }

    public int a(int a){
        return 3;
    }
}

class Foo{
    int a;
    int d;
    public int a(int a){
        return 1;
    }

    public boolean b(){
        return true && false;
    }

    public int[] call(A a){
        int[] y;
        return y;
    }
}
