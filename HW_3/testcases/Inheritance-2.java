class Inheritance {
    public static void main(String[] x) {
        System.out.println((new B()).foo(5));
    }
}

class B extends A {
	int x;
	int[] number;

	public int foo(int sz) {
		x = 1;
		number = new int[sz];
		number[x] = 10;
		System.out.println(number[x]);
		x = (x * (number[x]));
		if(x < 5) {
			System.out.println(number.length);
		} else {
			System.out.println(number[(x-9)]);
		}
		System.out.println(this.bar());
		return number[0];
	}
}

class A {
	int x;
	int y;

	public int foo(int sz) {
		x = 10;
		y = 12;
		x = ((x*y)*sz);
		return x;
	}
	public int bar() {
		System.out.println(2);
		System.out.println(this.bee());
		return 99;
	}

	public int bee() {
		return 1000000;
	}
}
