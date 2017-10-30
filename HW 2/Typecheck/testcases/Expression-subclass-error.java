class Main {
	public static void main(String[] a) {
		A _a;
		B _b;
		C _c;
		D _d;
		int x;
		_a = new A();
		_b = new B();
		_c = new C();
		_d = new D();
		x = _c.run(_a, _d);
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

class D {
	public int foo() {
		return 10000;
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
