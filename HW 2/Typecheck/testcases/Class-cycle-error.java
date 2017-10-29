class Main {
	public static void main(String[] a) {
		System.out.println(new A().run());
	}

}

class A extends B {
	public int run() {
		return 5;
	}

}
class B extends C {
	int x;
}
class C extends A {
}
