class Main {
	public static void main(String[] a) {
		A _a;
		B _b;
		C _c;
		int x;
		_a = new A();
		_b = new B();
		_c = new C();
		x = _c.run(_a, _b);
	}
}

class A {
	public int foo() {
		return 5;
	}
}

class B extends A {
	public int foo() {
		return 7;
	}
	public int bar() {
		return 10;
	}
}

class C {
	public int run(A a, A b) {
		int res;
		int _fin;
		res = (a.foo()) + (b.foo());

		if(res < 11) {
			_fin = 10;
		} else {
			_fin = 12;
		}
		return _fin;
	}
}
