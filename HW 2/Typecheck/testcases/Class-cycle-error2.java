class Main {
	public static void main(String[] a) {
		System.out.println(new A().run());
	}

}

class A extends A {
	public int run() {
		return 5;
	}

}
